package com.ljchengx.eudic.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ljchengx.eudic.App
import com.ljchengx.eudic.MainActivity
import com.ljchengx.eudic.R
import com.ljchengx.eudic.data.entity.WordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WordWidget : AppWidgetProvider() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    companion object {
        const val ACTION_NEXT_WORD = "com.ljchengx.eudic.NEXT_WORD"
        private var currentWordIndex = 0
        private var words = listOf<WordEntity>()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            // 从数据库获取单词
            val wordDao = (context.applicationContext as App).database.wordDao()
            words = wordDao.getAllWords().firstOrNull() ?: emptyList()
            
            // 更新所有小组件
            appWidgetIds.forEach { appWidgetId ->
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_NEXT_WORD -> {
                // 显示下一个单词
                if (words.isNotEmpty()) {
                    currentWordIndex = (currentWordIndex + 1) % words.size
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(
                        intent.component
                    )
                    appWidgetIds.forEach { appWidgetId ->
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.word)
        
        if (words.isNotEmpty()) {
            val currentWord = words[currentWordIndex]
            
            // 更新UI
            views.setTextViewText(R.id.word_text, currentWord.word)
            views.setTextViewText(R.id.meaning_text, currentWord.explanation)
            views.setTextViewText(
                R.id.word_index,
                "${currentWordIndex + 1}/${words.size}"
            )
        } else {
            // 没有单词时显示提示
            views.setTextViewText(R.id.word_text, "No Words")
            views.setTextViewText(R.id.meaning_text, "Please open app to load words")
            views.setTextViewText(R.id.word_index, "0/0")
        }

        // 设置按钮点击事件
        views.setOnClickPendingIntent(
            R.id.next_button,
            getPendingSelfIntent(context, ACTION_NEXT_WORD)
        )
        
        // 设置设置按钮点击跳转到 MainActivity
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainActivityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.settings_button, mainActivityPendingIntent)

        // 更新小组件
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, WordWidget::class.java)
        intent.action = action
        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        job.cancel()
    }
} 