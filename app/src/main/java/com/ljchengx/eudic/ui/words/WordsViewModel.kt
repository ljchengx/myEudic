package com.ljchengx.eudic.ui.words

import androidx.lifecycle.LiveData
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

    suspend fun refreshWords() {
        repository.refreshWords("default")
    }

    fun deleteWord(word: String) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }
} 