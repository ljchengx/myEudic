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
            Log.d("WordUpdateWorker", "开始每日更新")
            
            val words = try {
                withTimeout(60_000) {
                    wordRepository.getWordsFromNetwork()
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
            
            // 更新小部件显示
            word.updateWordList(words)
            
            // 更新所有小部件
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, word::class.java)
            )
            if (appWidgetIds.isNotEmpty()) {
                word.updateAllWidgets(context, appWidgetManager, appWidgetIds)
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("WordUpdateWorker", "更新失败: ${e.javaClass.simpleName}", e)
            e.printStackTrace()
            when (e) {
                is kotlinx.coroutines.CancellationException -> Result.retry()
                else -> Result.failure()
            }
        }
    }
} 