package com.ljchengx.eudic.ui.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljchengx.eudic.data.model.WidgetSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetSettingViewModel @Inject constructor(
    private val widgetSettingsRepository: WidgetSettingsRepository
) : ViewModel() {

    val settings: StateFlow<WidgetSettings> = widgetSettingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WidgetSettings()
        )

    fun updateFilterDays(days: Int) {
        viewModelScope.launch {
            widgetSettingsRepository.updateSettings(settings.value.copy(filterDays = days))
        }
    }

    fun updateRandomOrder(isRandom: Boolean) {
        viewModelScope.launch {
            widgetSettingsRepository.updateSettings(settings.value.copy(isRandomOrder = isRandom))
        }
    }

    fun updateHideExplanation(hide: Boolean) {
        viewModelScope.launch {
            widgetSettingsRepository.updateSettings(settings.value.copy(hideExplanation = hide))
        }
    }
} 