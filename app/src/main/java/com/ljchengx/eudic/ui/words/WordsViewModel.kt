package com.ljchengx.eudic.ui.words

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.ljchengx.eudic.App
import com.ljchengx.eudic.data.entity.WordEntity
import kotlinx.coroutines.launch

class WordsViewModel(application: Application) : AndroidViewModel(application) {
    private val wordDao = (application as App).database.wordDao()
    val words = wordDao.getAllWords().asLiveData()

    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            wordDao.deleteWord(word.word)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WordsViewModel::class.java)) {
                return WordsViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 