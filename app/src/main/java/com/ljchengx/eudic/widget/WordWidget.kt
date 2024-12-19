package com.ljchengx.eudic.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.elvishew.xlog.XLog
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
        const val ACTION_TOGGLE_EXPLANATION = "com.ljchengx.eudic.TOGGLE_EXPLANATION"
        private var currentWordIndex = 0
        private var words = listOf<WordEntity>()
        private var isExplanationVisible = false
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        XLog.d("小组件 onUpdate 被调用")
        updateWidgetData(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        XLog.d("小组件 onReceive 被调用: ${intent.action}")
        
        when (intent.action) {
            ACTION_NEXT_WORD -> {
                if (words.isNotEmpty()) {
                    currentWordIndex = (currentWordIndex + 1) % words.size
                    isExplanationVisible = false
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(
                        intent.component
                    )
                    updateWidgetViews(context, appWidgetManager, appWidgetIds)
                }
            }
            ACTION_TOGGLE_EXPLANATION -> {
                isExplanationVisible = !isExplanationVisible
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    intent.component
                )
                updateWidgetViews(context, appWidgetManager, appWidgetIds)
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetIds = intent.getIntArrayExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_IDS
                ) ?: return
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateWidgetData(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    private fun updateWidgetData(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            try {
                XLog.d("开始更新小组件数据")
                // 获取设置
                val settings = WordWidgetManager.getSettings(context)
                XLog.d("获取到小组件设置: $settings")
                
                // 从数据库获取单词
                words = WordWidgetManager.getAllWords(context)
                XLog.d("获取到单词列表，数量: ${words.size}")
                
                // 重置索引和解析显示状态
                if (currentWordIndex >= words.size) {
                    currentWordIndex = 0
                }
                isExplanationVisible = false
                
                // 更新所有小组件
                updateWidgetViews(context, appWidgetManager, appWidgetIds)
            } catch (e: Exception) {
                XLog.e("更新小组件数据失败", e)
            }
        }
    }

    private fun updateWidgetViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            try {
                XLog.d("开始更新小组件视图")
                val settings = WordWidgetManager.getSettings(context)
                
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId, settings)
                }
            } catch (e: Exception) {
                XLog.e("更新小组件视图失败", e)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        settings: com.ljchengx.eudic.data.model.WidgetSettings?
    ) {
        val views = RemoteViews(context.packageName, R.layout.word)
        
        if (words.isNotEmpty()) {
            XLog.d("更新小组件UI，当前单词索引: $currentWordIndex")
            val currentWord = words[currentWordIndex]
            
            // 更新UI
            views.setTextViewText(R.id.word_text, currentWord.word)
            
            // 根据设置和状态显示解析
            if (settings?.hideExplanation == true) {
                // 如果开启了隐藏解析功能
                views.setViewVisibility(R.id.show_explanation_button, View.VISIBLE)
                views.setTextViewText(R.id.meaning_text, 
                    if (isExplanationVisible) currentWord.explanation else "")
            } else {
                // 如果没有开启隐藏解析功能
                views.setViewVisibility(R.id.show_explanation_button, View.GONE)
                views.setTextViewText(R.id.meaning_text, currentWord.explanation)
            }
            
            views.setTextViewText(
                R.id.word_index,
                "${currentWordIndex + 1}/${words.size}"
            )
        } else {
            XLog.d("没有单词数据，显示提示信息")
            // 没有单词时显示提示
            views.setTextViewText(R.id.word_text, "No Words")
            views.setTextViewText(R.id.meaning_text, "Please open app to load words")
            views.setTextViewText(R.id.word_index, "0/0")
            views.setViewVisibility(R.id.show_explanation_button, View.GONE)
        }

        // 设置按钮点击事件
        views.setOnClickPendingIntent(
            R.id.next_button,
            getPendingSelfIntent(context, ACTION_NEXT_WORD)
        )
        
        // 设置显示/隐藏解析按钮点击事件
        views.setOnClickPendingIntent(
            R.id.show_explanation_button,
            getPendingSelfIntent(context, ACTION_TOGGLE_EXPLANATION)
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