package com.ljchengx.eudic.ui.widget

import com.ljchengx.eudic.data.model.WidgetSettings
import kotlinx.coroutines.flow.Flow

interface WidgetSettingsRepository {
    fun getSettings(): Flow<WidgetSettings>
    suspend fun updateSettings(settings: WidgetSettings)
} 