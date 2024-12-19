package com.ljchengx.eudic.ui.wordbook

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ljchengx.eudic.data.entity.WordbookEntity
import com.ljchengx.eudic.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordbookSettingViewModel @Inject constructor(
    private val repository: WordRepository
) : ViewModel() {

    val wordbooks: LiveData<List<WordbookEntity>> = repository.getAllWordbooks().asLiveData()

    suspend fun refreshWordbooks() {
        repository.refreshWordbooks()
    }

    suspend fun selectWordbook(wordbookId: String) {
        repository.selectWordbook(wordbookId)
    }

    suspend fun getSelectedWordbook(): WordbookEntity? {
        return repository.getSelectedWordbook()
    }
} 