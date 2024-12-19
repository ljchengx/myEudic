package com.ljchengx.eudic.ui.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ljchengx.eudic.data.model.WidgetSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetSettingViewModel @Inject constructor(
    private val widgetSettingsRepository: WidgetSettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(WidgetSettings())
    val settings: StateFlow<WidgetSettings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            widgetSettingsRepository.getSettings().collect { settings ->
                _settings.value = settings
            }
        }
    }

    fun updateFilterDays(days: Int) {
        viewModelScope.launch {
            widgetSettingsRepository.updateSettings(_settings.value.copy(filterDays = days))
        }
    }

    fun updateRandomOrder(isRandom: Boolean) {
        viewModelScope.launch {
            widgetSettingsRepository.updateSettings(_settings.value.copy(isRandomOrder = isRandom))
        }
    }

    fun updateHideExplanation(hide: Boolean) {
        viewModelScope.launch {
            widgetSettingsRepository.updateSettings(_settings.value.copy(hideExplanation = hide))
        }
    }
} 