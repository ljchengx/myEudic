package com.ljchengx.eudic

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class word : AppWidgetProvider() {
    companion object {
        const val PREFS_NAME = "WordWidgetPrefs"
        const val PREF_WORD_INDEX = "wordIndex"
        const val ACTION_NEXT_WORD = "com.ljchengx.eudic.ACTION_NEXT_WORD"
        
        val wordList = listOf(
            WordData("action", "n. 行动；活动；功能；情节；战斗"),
            WordData("beautiful", "adj. 美丽的；漂亮的；迷人的"),
            WordData("computer", "n. 计算机；电脑"),
            WordData("digital", "adj. 数字的；数码的"),
            WordData("example", "n. 例子；榜样；样本"),
            WordData("future", "n. 未来；前途 adj. 未来的"),
            WordData("global", "adj. 全球的；总体的"),
            WordData("happy", "adj. 快乐的；幸福的"),
            WordData("internet", "n. 互联网；因特网"),
            WordData("journey", "n. 旅程；旅行")
        )
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("WordWidget", "onUpdate called with ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId)
                Log.d("WordWidget", "Update completed for widget $appWidgetId")
            } catch (e: Exception) {
                Log.e("WordWidget", "Error in onUpdate", e)
                e.printStackTrace()
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("WordWidget", "onEnabled called")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(PREF_WORD_INDEX, 0)
            .apply()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("WordWidget", "onReceive called with action: ${intent.action}")
        if (intent.action == ACTION_NEXT_WORD) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentIndex = prefs.getInt(PREF_WORD_INDEX, 0)
                val nextIndex = (currentIndex + 1) % wordList.size
                prefs.edit().putInt(PREF_WORD_INDEX, nextIndex).apply()
                
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    try {
        Log.d("WordWidget", "updateAppWidget called for widget $appWidgetId")
        
        val prefs = context.getSharedPreferences(word.PREFS_NAME, Context.MODE_PRIVATE)
        val currentIndex = prefs.getInt(word.PREF_WORD_INDEX, 0)
        val currentWord = word.wordList[currentIndex]

        val views = RemoteViews(context.packageName, R.layout.word)

        // 更新文本
        views.setTextViewText(R.id.word_text, currentWord.word)
        views.setTextViewText(R.id.meaning_text, currentWord.exp)
        views.setTextViewText(R.id.word_index, "${currentIndex + 1}/${word.wordList.size}")

        // 设置按钮点击事件
        val intent = Intent(context, word::class.java).apply {
            action = word.ACTION_NEXT_WORD
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.next_button, pendingIntent)

        // 更新小部件
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d("WordWidget", "Widget update completed")
        
    } catch (e: Exception) {
        Log.e("WordWidget", "Error updating widget", e)
        e.printStackTrace()
    }
}