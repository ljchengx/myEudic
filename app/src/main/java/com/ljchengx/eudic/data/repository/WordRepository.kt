package com.ljchengx.eudic.data.repository

import android.content.Context
import android.util.Log
import com.ljchengx.eudic.WordData
import com.ljchengx.eudic.data.api.ApiService
import com.ljchengx.eudic.data.http.KtorClient
import com.ljchengx.eudic.data.model.ApiResponse
import com.ljchengx.eudic.data.model.WordItem
import com.ljchengx.eudic.data.db.WordEntity
import com.ljchengx.eudic.data.db.WordDatabase
import com.ljchengx.eudic.data.db.toWordData
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
                try {
                    Log.d("WordRepository", "Forcing network update")
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
                    Log.d("WordRepository", "Got ${apiResponse.data.size} words from network")
                    val processedWords = processWordItems(apiResponse.data)
                    
                    // 保存到数据库
                    val entities = processedWords.map { wordData -> 
                        WordEntity.fromWordData(wordData)
                    }
                    Log.d("WordRepository", "Saving ${entities.size} words to database")
                    wordDao.deleteAllWords()
                    wordDao.insertWords(entities)
                    Log.d("WordRepository", "Database updated successfully")
                    
                    processedWords
                } catch (e: Exception) {
                    Log.e("WordRepository", "Error getting words from network", e)
                    throw e
                }
            }
        }
    }

    private fun processWordItems(items: List<WordItem>): List<WordData> {
        Log.d("WordRepository", "开始处理 ${items.size} 个单词")
        val threeDaysAgo = LocalDateTime.now().minusDays(3)
        val processedWords = items
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
        return processedWords
    }
} 