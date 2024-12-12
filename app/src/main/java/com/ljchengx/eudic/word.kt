package com.ljchengx.eudic

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// API响应数据类
data class ApiResponse(
    val data: List<WordItem>,
    val message: String
)

data class WordItem(
    val word: String,
    val exp: String,
    val add_time: String
)

// API接口定义
interface WordApi {
    @GET("api/open/v1/studylist/words/133784439026055309?language=en")
    suspend fun getWords(
        @Header("Authorization") auth: String,
        @Header("Accept") accept: String = "application/json",
        @Header("Content-Type") contentType: String = "application/json"
    ): ApiResponse
}

class word : AppWidgetProvider() {
    companion object {
        const val PREFS_NAME = "WordWidgetPrefs"
        const val PREF_WORD_INDEX = "wordIndex"
        const val ACTION_NEXT_WORD = "com.ljchengx.eudic.ACTION_NEXT_WORD"
        const val ACTION_REFRESH = "com.ljchengx.eudic.ACTION_REFRESH"
        private const val AUTH_TOKEN = "NIS kCIT/cTUpouzWdOYkx+RZZ16aUHgyteYF9ZoFVWZe6EUQKkb6vGjmw=="
        
        private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.frdic.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        private val wordApi = retrofit.create(WordApi::class.java)
        var wordList = mutableListOf<WordData>()
        
        // 添加一个标志位来防止重复请求
        private var isRequesting = false
        
        private fun fetchWords(context: Context) {
            if (isRequesting) {
                Log.d("WordWidget", "已有请求正在进行中，跳过本次请求")
                return
            }
            
            isRequesting = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("WordWidget", "开始获取单��数据")
                    
                    // 打印请求信息
                    Log.d("WordWidget", "请求URL: https://api.frdic.com/api/open/v1/studylist/words/133784439026055309?language=en")
                    Log.d("WordWidget", "Authorization: $AUTH_TOKEN")
                    
                    try {
                        val response = wordApi.getWords(
                            auth = AUTH_TOKEN,
                            accept = "application/json",
                            contentType = "application/json"
                        )
                        Log.d("WordWidget", "API请求成功")
                        Log.d("WordWidget", "API响应message: ${response.message}")
                        Log.d("WordWidget", "API响应数据大小: ${response.data.size}")
                        
                        // 详细打印响应数据
                        response.data.forEach { word ->
                            Log.d("WordWidget", "单词详情: word=${word.word}, exp=${word.exp}, time=${word.add_time}")
                        }
                        
                        if (response.data.isEmpty()) {
                            Log.d("WordWidget", "API返回数据为空")
                            withContext(Dispatchers.Main) {
                                updateWidgetsWithMessage(context, "无数据", "API返回数据为空")
                            }
                            return@launch
                        }
                        
                        val threeDaysAgo = LocalDateTime.now().minusDays(3)
                        Log.d("WordWidget", "三天前时间: $threeDaysAgo")
                        
                        val filteredWords = response.data
                            .filter {
                                val wordDate = LocalDateTime.parse(
                                    it.add_time,
                                    DateTimeFormatter.ISO_DATE_TIME
                                )
                                val isAfter = wordDate.isAfter(threeDaysAgo)
                                Log.d("WordWidget", "单词: ${it.word}, 时间: $wordDate, 是否在三天内: $isAfter")
                                isAfter
                            }
                            .sortedByDescending { it.add_time }
                            .map { WordData(it.word, it.exp.replace("<br>", "\n")) }
                        
                        Log.d("WordWidget", "过滤后的单词列表: ${filteredWords.map { it.word }}")
                        
                        withContext(Dispatchers.Main) {
                            wordList.clear()
                            wordList.addAll(filteredWords)
                            
                            // 更新所有小部件
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                                ComponentName(context, word::class.java)
                            )
                            updateAllWidgets(context, appWidgetManager, appWidgetIds)
                        }
                        
                    } catch (e: Exception) {
                        Log.e("WordWidget", "API请求失败", e)
                        throw e
                    }
                    
                } catch (e: Exception) {
                    Log.e("WordWidget", "获取单词数据失败: ${e.message}", e)
                    Log.e("WordWidget", "详细错误: ", e)
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        updateWidgetsWithMessage(
                            context,
                            "获取失败",
                            "错误: ${e.message}"
                        )
                    }
                } finally {
                    Log.d("WordWidget", "请求完成，重置请求状态")
                    isRequesting = false
                }
            }
        }
        
        private fun updateAllWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            Log.d("WordWidget", "开始更新所有小部件")
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
        
        // 添加辅助方法来更新错误消息
        private fun updateWidgetsWithMessage(context: Context, title: String, message: String) {
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
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("WordWidget", "onUpdate called with ${appWidgetIds.size} widgets")
        // 只在列表为空时获取数据
        if (wordList.isEmpty()) {
            fetchWords(context)
        } else {
            // 直接更新现有数据
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
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
        // 在 onEnabled 中获取数据
        fetchWords(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("WordWidget", "onReceive: ${intent.action}")
        
        when (intent.action) {
            ACTION_NEXT_WORD -> {
                Log.d("WordWidget", "下一个按钮被点击")
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val currentIndex = prefs.getInt(PREF_WORD_INDEX, 0)
                    val nextIndex = if (wordList.isEmpty()) 0 else (currentIndex + 1) % wordList.size
                    
                    prefs.edit().putInt(PREF_WORD_INDEX, nextIndex).apply()
                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                }
            }
            ACTION_REFRESH -> {
                Log.d("WordWidget", "刷新按钮被点击")
                // 重置请求状态，确保可以重新请求
                isRequesting = false
                // 清空现有数据，强制重新获取
                wordList.clear()
                fetchWords(context)
            }
            else -> {
                Log.d("WordWidget", "收到其他action: ${intent.action}")
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
        Log.d("WordWidget", "更新小部件 $appWidgetId")
        val prefs = context.getSharedPreferences(word.PREFS_NAME, Context.MODE_PRIVATE)
        val currentIndex = prefs.getInt(word.PREF_WORD_INDEX, 0)
        
        if (word.wordList.isEmpty()) {
            Log.d("WordWidget", "单词列表为空")
            // 显示加载中的状态
            val views = RemoteViews(context.packageName, R.layout.word)
            views.setTextViewText(R.id.word_text, "加载中...")
            views.setTextViewText(R.id.meaning_text, "正在获取单词数据")
            views.setTextViewText(R.id.word_index, "0/0")
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }
        
        val currentWord = word.wordList[currentIndex]
        val views = RemoteViews(context.packageName, R.layout.word)
        
        views.setTextViewText(R.id.word_text, currentWord.word)
        views.setTextViewText(R.id.meaning_text, currentWord.exp)
        views.setTextViewText(R.id.word_index, "${currentIndex + 1}/${word.wordList.size}")
        
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
        
        // 添加刷新按钮的点击事件
        val refreshIntent = Intent(context, word::class.java).apply {
            action = word.ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId + 1, // 使用不同的请求码，避免与下一个按钮冲突
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d("WordWidget", "小部件 $appWidgetId 更新完成")
        
    } catch (e: Exception) {
        Log.e("WordWidget", "更新小部件失败: ${e.message}", e)
        e.printStackTrace()
    }
}