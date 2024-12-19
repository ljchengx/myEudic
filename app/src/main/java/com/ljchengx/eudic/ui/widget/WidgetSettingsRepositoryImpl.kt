package com.ljchengx.eudic.ui.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ljchengx.eudic.data.model.WidgetSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_settings")

@Singleton
class WidgetSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetSettingsRepository {

    private object PreferencesKeys {
        val FILTER_DAYS = intPreferencesKey("filter_days")
        val IS_RANDOM_ORDER = booleanPreferencesKey("is_random_order")
        val HIDE_EXPLANATION = booleanPreferencesKey("hide_explanation")
    }

    override fun getSettings(): Flow<WidgetSettings> = context.dataStore.data.map { preferences ->
        WidgetSettings(
            filterDays = preferences[PreferencesKeys.FILTER_DAYS] ?: 1,
            isRandomOrder = preferences[PreferencesKeys.IS_RANDOM_ORDER] ?: false,
            hideExplanation = preferences[PreferencesKeys.HIDE_EXPLANATION] ?: false
        )
    }

    override suspend fun updateSettings(settings: WidgetSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILTER_DAYS] = settings.filterDays
            preferences[PreferencesKeys.IS_RANDOM_ORDER] = settings.isRandomOrder
            preferences[PreferencesKeys.HIDE_EXPLANATION] = settings.hideExplanation
        }
    }
} 