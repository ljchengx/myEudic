package com.ljchengx.eudic.data.repository

import com.ljchengx.eudic.data.entity.RequestRecord
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.data.entity.WordbookEntity
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getAllWords(): Flow<List<WordEntity>>
    fun getAllWordbooks(): Flow<List<WordbookEntity>>
    suspend fun getSelectedWordbook(): WordbookEntity?
    suspend fun selectWordbook(wordbookId: String)
    suspend fun refreshWordbooks()
    suspend fun refreshWords()
    suspend fun getLastRequestRecord(): RequestRecord?
    suspend fun updateRequestRecord(record: RequestRecord)
    suspend fun deleteWord(word: String)
    suspend fun getWordByName(word: String): WordEntity?
    suspend fun deleteAllWords()
} 