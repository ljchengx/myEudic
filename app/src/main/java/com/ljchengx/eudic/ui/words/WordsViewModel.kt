package com.ljchengx.eudic.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordsViewModel @Inject constructor(
    private val repository: WordRepository
) : ViewModel() {

    val words: LiveData<List<WordEntity>> = repository.getAllWords().asLiveData()
    private val _currentWordbookName = MutableLiveData<String>()
    val currentWordbookName: LiveData<String> = _currentWordbookName

    init {
        updateCurrentWordbookName()
    }

    private fun updateCurrentWordbookName() {
        viewModelScope.launch {
            val selectedWordbook = repository.getSelectedWordbook()
            _currentWordbookName.value = selectedWordbook?.name ?: "未选择单词本"
        }
    }

    suspend fun refreshWordbooks() {
        repository.refreshWordbooks()
        updateCurrentWordbookName()
    }

    suspend fun refreshWords() {
        repository.refreshWords()
        updateCurrentWordbookName()
    }

    fun deleteWord(word: String) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }
} 