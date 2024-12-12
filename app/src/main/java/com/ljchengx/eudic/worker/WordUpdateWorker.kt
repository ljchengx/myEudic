package com.ljchengx.eudic.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ljchengx.eudic.data.repository.WordRepository
import com.ljchengx.eudic.word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

class WordUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val wordRepository = WordRepository(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            Log.d("WordUpdateWorker", "开始更新单词数据")
            
            val forceUpdate = inputData.getBoolean("force_update", false)
            Log.d("WordUpdateWorker", "Force update: $forceUpdate")
            
            val words = try {
                withTimeout(60_000) {
                    if (forceUpdate) {
                        Log.d("WordUpdateWorker", "开始强制从网络获取数据")
                        wordRepository.getWordsFromNetwork()
                    } else {
                        Log.d("WordUpdateWorker", "开始获取数据")
                        wordRepository.getWords()
                    }
                }
            } catch (e: Exception) {
                Log.e("WordUpdateWorker", "获取数据失败: ${e.message}", e)
                when (e) {
                    is kotlinx.coroutines.CancellationException -> {
                        Log.d("WordUpdateWorker", "任务被取消，准备重试")
                        return@withContext Result.retry()
                    }
                    else -> {
                        Log.e("WordUpdateWorker", "发生其他错误: ${e.javaClass.simpleName}")
                        return@withContext Result.failure()
                    }
                }
            }
            
            Log.d("WordUpdateWorker", "获取到 ${words.size} 个单词")
            
            if (words.isEmpty()) {
                Log.w("WordUpdateWorker", "获取到的单词列表为空")
                return@withContext Result.retry()
            }
            
            try {
                word.updateWordList(words)
                Log.d("WordUpdateWorker", "单词列表更新完成")
                
                withContext(Dispatchers.Main) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(
                        ComponentName(context, word::class.java)
                    )
                    Log.d("WordUpdateWorker", "准备更新 ${appWidgetIds.size} 个小部件")
                    
                    if (appWidgetIds.isNotEmpty()) {
                        word.updateAllWidgets(context, appWidgetManager, appWidgetIds)
                        Log.d("WordUpdateWorker", "小部件更新完成")
                    } else {
                        Log.w("WordUpdateWorker", "没有找到需要更新的小部件")
                    }
                }
                
                Log.d("WordUpdateWorker", "单词数据更新完成")
                Result.success()
            } catch (e: Exception) {
                Log.e("WordUpdateWorker", "更新小部件失败: ${e.message}", e)
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("WordUpdateWorker", "更新失败: ${e.javaClass.simpleName}", e)
            e.printStackTrace()
            when (e) {
                is kotlinx.coroutines.CancellationException -> {
                    Log.d("WordUpdateWorker", "任务被取消，准备重试")
                    Result.retry()
                }
                else -> {
                    Log.e("WordUpdateWorker", "发生其他错误: ${e.javaClass.simpleName}")
                    Result.failure()
                }
            }
        }
    }
} 