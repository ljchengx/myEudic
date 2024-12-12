package com.ljchengx.eudic.data.repository

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ljchengx.eudic.WordData
import com.ljchengx.eudic.data.api.ApiService
import com.ljchengx.eudic.data.http.KtorClient
import com.ljchengx.eudic.data.model.ApiResponse
import com.ljchengx.eudic.data.model.WordItem
import com.ljchengx.eudic.data.db.WordEntity
import com.ljchengx.eudic.data.db.WordDatabase
import com.ljchengx.eudic.data.db.toWordData
import com.ljchengx.eudic.word
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WordRepository(private val context: Context) {
    private val wordDao = WordDatabase.getDatabase(context).wordDao()
    private val mutex = Mutex()

    suspend fun getWords(): List<WordData> {
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    val lastUpdateTime = wordDao.getLastUpdateTime() ?: 0
                    val currentTime = System.currentTimeMillis()
                    val shouldUpdate = currentTime - lastUpdateTime > 24 * 60 * 60 * 1000
                    
                    Log.d("WordRepository", "Last update: ${lastUpdateTime}, Should update: $shouldUpdate")
                    
                    val words = if (shouldUpdate) {
                        Log.d("WordRepository", "Fetching new data from network")
                        getWordsFromNetwork()
                    } else {
                        Log.d("WordRepository", "Using cached data")
                        wordDao.getAllWords().map { it.toWordData() }
                    }

                    // 确保小组件数据更新
                    updateWidgetData(words)

                    Log.d("WordRepository", "Returning ${words.size} words")
                    words
                } catch (e: Exception) {
                    Log.e("WordRepository", "Error getting words", e)
                    throw e
                }
            }
        }
    }

    suspend fun getWordsFromNetwork(): List<WordData> {
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                var retryCount = 0
                val maxRetries = 50  // 最大重试次数
                
                while (retryCount < maxRetries) {
                    try {
                        Log.d("WordRepository", "尝试获取网络数据，第 ${retryCount + 1} 次")
                        val response = KtorClient.client.get("${ApiService.BASE_URL}${ApiService.GET_WORD_URL}") {
                            headers {
                                append("Authorization", ApiService.AUTH_TOKEN)
                                append("Accept", "application/json")
                                append("Content-Type", "application/json")
                                append("User-Agent", "Mozilla/5.0")
                                append("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                            }
                        }
                        
                        val apiResponse = response.body<ApiResponse<List<WordItem>>>()
                        if (apiResponse.data == null) {
                            Log.e("WordRepository", "API返回的数据为空")
                            retryCount++
                            delay(1000)
                            continue
                        }
                        
                        Log.d("WordRepository", "获取到 ${apiResponse.data.size} 个单词")
                        
                        // 检查空释义的单词数量
                        val emptyExpCount = apiResponse.data.count { it.exp.isNullOrBlank() }
                        Log.d("WordRepository", "空释义单词数量：$emptyExpCount")
                        
                        if (emptyExpCount > 10) {
                            Log.w("WordRepository", "空释义单词过多，准备重试")
                            retryCount++
                            delay(1000) // 延迟1秒后重试
                            continue
                        }
                        
                        val processedWords = processWordItems(apiResponse.data)
                        if (processedWords.isEmpty()) {
                            Log.w("WordRepository", "处理后的单词列表为空，准备重试")
                            retryCount++
                            delay(1000)
                            continue
                        }
                        
                        // 保存到数据库
                        val entities = processedWords.map { wordData -> 
                            WordEntity.fromWordData(wordData)
                        }
                        if (entities.isEmpty()) {
                            Log.e("WordRepository", "转换为实体后的列表为空")
                            retryCount++
                            delay(1000)
                            continue
                        }
                        
                        Log.d("WordRepository", "保存 ${entities.size} 个单词到数据库")
                        wordDao.deleteAllWords()
                        wordDao.insertWords(entities)
                        Log.d("WordRepository", "数据库更新成功")
                        
                        Log.d("WordRepository", "准备返回处理后的单词列表，大小：${processedWords.size}")
                        return@withContext processedWords
                    } catch (e: Exception) {
                        Log.e("WordRepository", "获取网络数据失败: ${e.message}", e)
                        e.printStackTrace()
                        if (retryCount >= maxRetries - 1) {
                            Log.e("WordRepository", "达到最大重试次数，抛出异常")
                            throw e
                        }
                        retryCount++
                        delay(1000) // 延迟1秒后重试
                    }
                }
                
                Log.e("WordRepository", "所有重试都失败了")
                throw Exception("获取网络数据失败，已重试 $maxRetries 次")
            }
        }
    }

    private fun processWordItems(items: List<WordItem>): List<WordData> {
        Log.d("WordRepository", "开始处理 ${items.size} 个单词")
        if (items.isEmpty()) {
            Log.w("WordRepository", "输入的单词列表为空")
            return emptyList()
        }

        val threeDaysAgo = LocalDateTime.now().minusDays(3)
        Log.d("WordRepository", "过滤3天前的数据，基准时间：$threeDaysAgo")

        val processedWords = items
            .filter {
                // 过滤掉空释义的单词
                !it.exp.isNullOrBlank() && 
                LocalDateTime.parse(
                    it.add_time,
                    DateTimeFormatter.ISO_DATE_TIME
                ).isAfter(threeDaysAgo)
            }
            .sortedByDescending { it.add_time }
            .map {
                Log.d("WordRepository", "处理单词：${it.word}")
                val processedExp = it.exp
                    .replace("<br>", "\n")
                    .lines()
                    .take(3)
                    .joinToString("\n")
                    .let { exp ->
                        if (exp.length > 110) {
                            exp.take(107) + "..."
                        } else {
                            exp
                        }
                    }
                    .replace(Regex("\\s+"), " ")
                    .trim()
                WordData(it.word, processedExp)
            }
        Log.d("WordRepository", "处理完成，过滤后剩余 ${processedWords.size} 个单词")
        if (processedWords.isEmpty()) {
            Log.w("WordRepository", "处理后没有剩余单词")
        }
        return processedWords
    }

    private fun updateWidgetData(words: List<WordData>) {
        if (words.isEmpty()) {
            Log.w("WordRepository", "No words to update widget")
            return
        }

        Log.d("WordRepository", "开始更新小组件数据，单词数量：${words.size}")
        // 更新小组件的单词列表
        word.updateWordList(words)
        
        // 重置索引
        context.getSharedPreferences(word.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(word.PREF_WORD_INDEX, 0)
            .apply()
        Log.d("WordRepository", "索引已重置为0")
        
        // 获取所有小组件ID并更新
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, word::class.java)
        )
        
        if (appWidgetIds.isNotEmpty()) {
            // 使用现有的更新逻辑更新所有小组件
            word.updateAllWidgets(context, appWidgetManager, appWidgetIds)
            Log.d("WordRepository", "Updated ${appWidgetIds.size} widgets with ${words.size} words")
        } else {
            Log.d("WordRepository", "No widgets found to update")
        }
        Log.d("WordRepository", "小组件数据更新完成")
    }
} 