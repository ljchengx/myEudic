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
import kotlinx.coroutines.flow.firstOrNull
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

    fun getAllWords(context: Context): Flow<List<WordEntity>> {
        val wordsFlow = getRepository(context).getAllWords()
        val settingsFlow = getSettingsRepository(context).getSettings()

        return wordsFlow.combine(settingsFlow) { words, settings ->
            val currentTimeMillis = System.currentTimeMillis()
            val filterTimeMillis = TimeUnit.DAYS.toMillis(settings.filterDays.toLong())
            val cutoffTime = currentTimeMillis - filterTimeMillis

            val filteredWords = words.filter { word ->
                val wordTime = parseAddTime(word.addTime)
                wordTime >= cutoffTime
            }

            if (settings.isRandomOrder) {
                filteredWords.shuffled()
            } else {
                filteredWords.sortedByDescending { parseAddTime(it.addTime) }
            }
        }
    }

    suspend fun getSettings(context: Context): WidgetSettings? {
        return getSettingsRepository(context).getSettings().firstOrNull()
    }
} 