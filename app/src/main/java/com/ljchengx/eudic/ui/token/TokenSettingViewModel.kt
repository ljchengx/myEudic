package com.ljchengx.eudic.ui.token

import androidx.lifecycle.ViewModel
import com.ljchengx.eudic.data.entity.RequestRecord
import com.ljchengx.eudic.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TokenSettingViewModel @Inject constructor(
    private val repository: WordRepository
) : ViewModel() {

    suspend fun getSavedToken(): String {
        return repository.getLastRequestRecord()?.token ?: ""
    }

    suspend fun saveToken(token: String) {
        val record = repository.getLastRequestRecord() ?: RequestRecord(
            lastRequestTime = System.currentTimeMillis(),
            userId = "default",
            token = ""
        )
        repository.updateRequestRecord(record.copy(token = token))
    }
} 