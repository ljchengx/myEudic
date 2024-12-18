package com.ljchengx.eudic.ui.words

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.data.repository.WordRepository
import com.ljchengx.eudic.widget.WordWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: WordRepository
) : ViewModel() {
    val words = repository.getAllWords().asLiveData()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            repository.deleteWord(word.word)
            updateWidgets()
        }
    }

    fun refreshWords(userId: String = "133784439026055309") {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                repository.refreshWords(userId)
                updateWidgets()
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WordWidget::class.java)
        )
        val intent = Intent(context, WordWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }
} 