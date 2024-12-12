package com.ljchengx.eudic

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.work.*
import com.ljchengx.eudic.data.repository.WordRepository
import com.ljchengx.eudic.worker.WordUpdateWorker
import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException
import java.util.*

class word : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // 当第一个小部件被添加时调用
        scheduleUpdate(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        // 如果列表为空，触发更新
        if (wordList.isEmpty()) {
            scheduleUpdate(context)
        }
        
        // 更新所有小部件显示
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_NEXT_WORD -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    // 如果列表为空，先触发更新
                    if (wordList.isEmpty()) {
                        scheduleUpdate(context)
                    } else {
                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val currentIndex = prefs.getInt(PREF_WORD_INDEX, 0)
                        val nextIndex = (currentIndex + 1) % (wordList.size.coerceAtLeast(1))
                        prefs.edit().putInt(PREF_WORD_INDEX, nextIndex).apply()
                        
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }
            }
            ACTION_REFRESH -> scheduleUpdate(context)
        }
    }

    companion object {
        const val PREFS_NAME = "WordWidgetPrefs"
        const val PREF_WORD_INDEX = "wordIndex"
        const val ACTION_NEXT_WORD = "com.ljchengx.eudic.ACTION_NEXT_WORD"
        const val ACTION_REFRESH = "com.ljchengx.eudic.ACTION_REFRESH"
        private const val WORK_NAME = "word_update_work"
        private const val MIN_BACKOFF_MILLIS = WorkRequest.MIN_BACKOFF_MILLIS

        private var wordList = mutableListOf<WordData>()

        fun getWordList(): List<WordData> = wordList
        fun getWordAt(index: Int): WordData? = wordList.getOrNull(index)
        fun getWordCount(): Int = wordList.size
        fun isWordListEmpty(): Boolean = wordList.isEmpty()

        fun updateWordList(words: List<WordData>) {
            wordList.clear()
            wordList.addAll(words)
        }

        private fun scheduleUpdate(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(WORK_NAME)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // 创建立即执行的任务，添加强制更新标记
            val data = workDataOf("force_update" to true)
            val immediateRequest = OneTimeWorkRequestBuilder<WordUpdateWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .addTag(WORK_NAME)
                .build()

            // 计算下一个凌晨的时间
            val now = Calendar.getInstance()
            val nextRun = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val initialDelay = nextRun.timeInMillis - now.timeInMillis

            // 创建每日更新任务
            val dailyWorkRequest = PeriodicWorkRequestBuilder<WordUpdateWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            // 启动任务
            workManager.enqueue(immediateRequest)
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest
            )

            // 显示加载状态
            updateWidgetsWithMessage(
                context,
                "加载中...",
                "正在获取单词数据"
            )
        }

        internal fun updateAllWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            Log.d("WordWidget", "开始更新所有小部件，数量：${appWidgetIds.size}")
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
            Log.d("WordWidget", "所有小部件更新完成")
        }

        internal fun updateWidgetsWithMessage(context: Context, title: String, message: String) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, word::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.word)
                views.setTextViewText(R.id.word_text, title)
                views.setTextViewText(R.id.meaning_text, message)
                views.setTextViewText(R.id.word_index, "0/0")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                Log.d("WordWidget", "更新小部件 $appWidgetId，当前单词列表大小：${wordList.size}")
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentIndex = prefs.getInt(PREF_WORD_INDEX, 0)
                Log.d("WordWidget", "当前索引：$currentIndex")

                if (wordList.isEmpty()) {
                    Log.d("WordWidget", "单词列表为空，显示加载状态")
                    val views = RemoteViews(context.packageName, R.layout.word)
                    views.setTextViewText(R.id.word_text, "加载中...")
                    views.setTextViewText(R.id.meaning_text, "正在获取单词数据")
                    views.setTextViewText(R.id.word_index, "0/0")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    return
                }

                val currentWord = wordList.getOrNull(currentIndex)
                if (currentWord == null) {
                    Log.e("WordWidget", "获取当前单词失败：index=$currentIndex")
                    return
                }

                Log.d("WordWidget", "显示单词：${currentWord.word}")
                val views = RemoteViews(context.packageName, R.layout.word)
                views.setTextViewText(R.id.word_text, currentWord.word)
                views.setTextViewText(R.id.meaning_text, currentWord.exp)
                views.setTextViewText(R.id.word_index, "${currentIndex + 1}/${wordList.size}")

                val intent = Intent(context, word::class.java).apply {
                    action = ACTION_NEXT_WORD
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                views.setOnClickPendingIntent(R.id.next_button, pendingIntent)

                val refreshIntent = Intent(context, word::class.java).apply {
                    action = ACTION_REFRESH
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId + 1,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

                // 添加整个小部件的点击事件
                val mainIntent = Intent(context, MainActivity::class.java)
                val mainPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId + 2,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_layout, mainPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("WordWidget", "小部件 $appWidgetId 更新完成")

            } catch (e: Exception) {
                Log.e("WordWidget", "更新小部件失败: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
}