package com.ljchengx.eudic

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
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
        updateWidgetsWithMessage(
            context,
            "加载中...",
            "正在获取单词数据"
        )
        // 加载数据并设置每日更新任务
        loadDataAndScheduleUpdate(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
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
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val currentIndex = prefs.getInt(PREF_WORD_INDEX, 0)
                    val nextIndex = (currentIndex + 1) % (wordList.size.coerceAtLeast(1))
                    prefs.edit().putInt(PREF_WORD_INDEX, nextIndex).apply()
                    
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
            ACTION_REFRESH -> {
                // 启动 MainActivity 来触发数据刷新
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
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
        private val scope = CoroutineScope(Dispatchers.Main + Job())

        fun getWordList(): List<WordData> = wordList
        fun getWordAt(index: Int): WordData? = wordList.getOrNull(index)
        fun getWordCount(): Int = wordList.size
        fun isWordListEmpty(): Boolean = wordList.isEmpty()

        fun updateWordList(words: List<WordData>) {
            Log.d("WordWidget", "开始更新单词列表，新数据大小：${words.size}")
            wordList.clear()
            wordList.addAll(words)
            Log.d("WordWidget", "单词列表更新完成，当前大小：${wordList.size}")
        }

        private fun scheduleUpdate(context: Context) {
            val workManager = WorkManager.getInstance(context)
            
            // 取消所有旧的任务
            workManager.cancelUniqueWork(WORK_NAME)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // 只创建每日更新任务
            val dailyRequest = PeriodicWorkRequestBuilder<WordUpdateWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            // 启动每日任务
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyRequest
            )
        }

        private fun calculateInitialDelay(): Long {
            val now = Calendar.getInstance()
            val nextRun = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return nextRun.timeInMillis - now.timeInMillis
        }

        internal fun updateAllWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            Log.d("WordWidget", "开始更新所有小部件，数量：${appWidgetIds.size}")
            if (appWidgetIds.isEmpty()) {
                Log.w("WordWidget", "没有找到需要更新的小部件")
                return
            }
            for (appWidgetId in appWidgetIds) {
                Log.d("WordWidget", "正在更新小部件 ID: $appWidgetId")
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

                // 设置下一个按钮点击事件
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

                // 设置刷新按钮点击事件
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
//                views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

                // 设置按钮点击事件
                val settingsIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val settingsPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId + 2,
                    settingsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.settings_button, settingsPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("WordWidget", "小部件 $appWidgetId 更新完成")

            } catch (e: Exception) {
                Log.e("WordWidget", "更新小部件失败: ${e.message}", e)
                e.printStackTrace()
            }
        }

        private fun loadDataAndScheduleUpdate(context: Context) {
            scope.launch {
                try {
                    // 从数据库加载数据
                    val repository = WordRepository(context)
                    val words = withContext(Dispatchers.IO) {
                        repository.getWords()
                    }
                    
                    // 更新列表和小部件
                    updateWordList(words)
                    
                    // 更新所有小部件
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(
                        ComponentName(context, word::class.java)
                    )
                    if (appWidgetIds.isNotEmpty()) {
                        updateAllWidgets(context, appWidgetManager, appWidgetIds)
                    }
                    
                    // 设置每日更新任务
                    scheduleUpdate(context)
                } catch (e: Exception) {
                    Log.e("WordWidget", "加载数据失败: ${e.message}", e)
                }
            }
        }

        // 清理协程作用域
        fun cleanup() {
            scope.cancel()
        }
    }
}