package com.ljchengx.eudic.widget

import android.content.Context
import com.elvishew.xlog.XLog
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.data.repository.WordRepository
import com.ljchengx.eudic.ui.widget.WidgetSettingsRepository
import com.ljchengx.eudic.data.model.WidgetSettings
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object WordWidgetManager {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WordWidgetDependencies {
        fun repository(): WordRepository
        fun widgetSettingsRepository(): WidgetSettingsRepository
    }

    private fun getHiltEntryPoint(context: Context): WordWidgetDependencies {
        val appContext = context.applicationContext
        return EntryPointAccessors.fromApplication(
            appContext,
            WordWidgetDependencies::class.java
        )
    }

    private fun getRepository(context: Context): WordRepository {
        return getHiltEntryPoint(context).repository()
    }

    private fun getSettingsRepository(context: Context): WidgetSettingsRepository {
        return getHiltEntryPoint(context).widgetSettingsRepository()
    }

    private fun parseAddTime(addTime: String): Long {
        return try {
            // 尝试解析 ISO 8601 格式 (2024-12-07T01:22:35Z)
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            format.parse(addTime)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            try {
                // 如果失败，尝试解析标准格式 (yyyy-MM-dd HH:mm:ss)
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                format.parse(addTime)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                XLog.e("解析时间失败: $addTime", e)
                System.currentTimeMillis()
            }
        }
    }

    suspend fun getAllWords(context: Context): List<WordEntity> {
        XLog.d("开始获取小组件单词列表")
        try {
            val words = getRepository(context).getAllWords().first()
            XLog.d("从数据库获取到单词列表，数量: ${words.size}")
            
            if (words.isEmpty()) {
                XLog.d("数据库中没有单词数据")
                return emptyList()
            }
            
            val settings = getSettingsRepository(context).getSettings().first()
            XLog.d("获取到小组件设置: $settings")
            
            // 如果filterDays为0，则显示所有单词
            val finalWords = if (settings.filterDays == 0) {
                XLog.d("设置为显示所有单词")
                words
            } else {
                val currentTimeMillis = System.currentTimeMillis()
                val filterTimeMillis = TimeUnit.DAYS.toMillis(settings.filterDays.toLong())
                val cutoffTime = currentTimeMillis - filterTimeMillis

                val filteredWords = words.filter { word ->
                    val wordTime = parseAddTime(word.addTime)
                    wordTime >= cutoffTime
                }
                XLog.d("过滤后的单词列表，数量: ${filteredWords.size}")
                
               filteredWords
            }

            return if (settings.isRandomOrder) {
                XLog.d("随机排序单词列表")
                finalWords.shuffled()
            } else {
                XLog.d("按时间排序单词列表")
                finalWords.sortedByDescending { parseAddTime(it.addTime) }
            }
        } catch (e: Exception) {
            XLog.e("获取单词列表失败", e)
            return emptyList()
        }
    }

    suspend fun getSettings(context: Context): WidgetSettings? {
        XLog.d("开始获取小组件设置")
        return getSettingsRepository(context).getSettings().firstOrNull().also {
            XLog.d("获取到小组件设置: $it")
        }
    }
} 