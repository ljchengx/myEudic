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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
        @Header("Content-Type") contentType: String = "application/json",
        @Header("User-Agent") userAgent: String = "Mozilla/5.0",
        @Header("Accept-Language") acceptLanguage: String = "zh-CN,zh;q=0.9,en;q=0.8"
    ): ApiResponse
}

class word : AppWidgetProvider() {
    companion object {
        const val PREFS_NAME = "WordWidgetPrefs"
        const val PREF_WORD_INDEX = "wordIndex"
        const val ACTION_NEXT_WORD = "com.ljchengx.eudic.ACTION_NEXT_WORD"
        const val ACTION_REFRESH = "com.ljchengx.eudic.ACTION_REFRESH"
        private const val AUTH_TOKEN =
            "NIS kCIT/cTUpouzWdOYkx+RZZ16aUHgyteYF9ZoFVWZe6EUQKkb6vGjmw=="

        // 添加请求状态标志
        @Volatile  // 确保多线程可见性
        private var isRequesting = false

        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                val newRequest = request.newBuilder()
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.frdic.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val wordApi = retrofit.create(WordApi::class.java)
        var wordList = mutableListOf<WordData>()

        // 添加本地缓存
        private const val PREF_CACHED_WORDS = "cachedWords"
        private const val PREF_LAST_UPDATE = "lastUpdate"

        private fun loadCachedWords(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cachedJson = prefs.getString(PREF_CACHED_WORDS, null)
            if (cachedJson != null) {
                try {
                    // 从缓存加载数据
                    val type = object : TypeToken<List<WordData>>() {}.type
                    wordList = Gson().fromJson(cachedJson, type)
                    Log.d("WordWidget", "从缓存加载了 ${wordList.size} 个单词")
                } catch (e: Exception) {
                    Log.e("WordWidget", "加载缓存失败", e)
                }
            }
        }

        private fun saveWordsToCache(context: Context, words: List<WordData>) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = Gson().toJson(words)
            prefs.edit()
                .putString(PREF_CACHED_WORDS, json)
                .putLong(PREF_LAST_UPDATE, System.currentTimeMillis())
                .apply()
        }

        private fun fetchWords(context: Context) {
            if (isRequesting) {
                Log.d("WordWidget", "已有请求正在进行中，跳过本次请求")
                return
            }

            isRequesting = true
            Log.d("WordWidget", "开始请求，设置 isRequesting = true")

            // 添加延时启动
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000) // 延时1秒
                Log.d("WordWidget", "延时1秒后开始请求")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d("WordWidget", "开始网络请求协程")
                        withTimeout(3000) {
                            try {
                                Log.d("WordWidget", "发起API请求")
                                val response = wordApi.getWords(
                                    auth = AUTH_TOKEN,
                                    accept = "application/json",
                                    contentType = "application/json",
                                    userAgent = "Mozilla/5.0",
                                    acceptLanguage = "zh-CN,zh;q=0.9,en;q=0.8"
                                )
                                Log.d("WordWidget", "API请求成功，开始处理数据")
                                processResponse(response, context)

                            } catch (e: Exception) {
                                Log.e("WordWidget", "API请求失败: ${e.message}")
                                throw e
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("WordWidget", "整体请求失败: ${e.message}")
                        withContext(Dispatchers.Main) {
                            updateWidgetsWithMessage(
                                context,
                                "获取失败",
                                if (e is kotlinx.coroutines.TimeoutCancellationException)
                                    "请求超时，请稍后重试"
                                else
                                    "网络错误：${e.message}"
                            )
                        }
                    } finally {
                        Log.d("WordWidget", "请求结束，重置 isRequesting = false")
                        isRequesting = false
                    }
                }
            }
        }

        // 将响应处理逻辑提取到单独的方法
        private suspend fun processResponse(response: ApiResponse, context: Context) {
            if (response.data.isEmpty()) {
                withContext(Dispatchers.Main) {
                    updateWidgetsWithMessage(context, "无数据", "API返回数据为空")
                }
                return
            }

            val threeDaysAgo = LocalDateTime.now().minusDays(3)
            val filteredWords = response.data
                .filter {
                    val wordDate = LocalDateTime.parse(
                        it.add_time,
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                    wordDate.isAfter(threeDaysAgo)
                }
                .sortedByDescending { it.add_time }
                .map {
                    val processedExp = it.exp
                        .replace("<br>", "\n")
                        .lines()
                        .take(3)
                        .joinToString("\n")
                        .let { exp ->
                            Log.d("WordWidget", "length = " + exp.length.toString())
                            if (exp.length > 70) {
                                exp.take(67) + "..."
                            } else {
                                exp
                            }
                        }
                        .replace(Regex("\\s+"), " ")
                        .trim()
                    WordData(it.word, processedExp)
                }

            withContext(Dispatchers.Main) {
                wordList.clear()
                wordList.addAll(filteredWords)

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, word::class.java)
                )
                updateAllWidgets(context, appWidgetManager, appWidgetIds)
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
            // 直接更新有数据
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
                    val nextIndex =
                        if (wordList.isEmpty()) 0 else (currentIndex + 1) % wordList.size

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
            // 显示加载的状态
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