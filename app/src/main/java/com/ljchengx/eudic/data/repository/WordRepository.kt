package com.ljchengx.eudic.data.repository

import com.ljchengx.eudic.data.dao.RequestRecordDao
import com.ljchengx.eudic.data.dao.WordDao
import com.ljchengx.eudic.data.entity.RequestRecord
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.network.WordService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val wordDao: WordDao,
    private val requestRecordDao: RequestRecordDao,
    private val wordService: WordService
) {
    fun getAllWords(): Flow<List<WordEntity>> = wordDao.getAllWords()

    suspend fun refreshWords(userId: String) {
        val response = wordService.getWords(userId)
        
        // 保存单词数据
        val wordEntities = response.data.map { wordItem ->
            WordEntity(
                word = wordItem.word,
                explanation = wordItem.exp,
                addTime = wordItem.add_time
            )
        }
        wordDao.insertWords(wordEntities)

        // 更新请求记录
        requestRecordDao.insertOrUpdate(
            RequestRecord(
                lastRequestTime = System.currentTimeMillis(),
                userId = userId
            )
        )
    }

    suspend fun getLastRequestRecord(): RequestRecord? {
        return requestRecordDao.getLastRequestRecord()
    }

    suspend fun deleteWord(word: String) {
        wordDao.deleteWord(word)
    }

    suspend fun getWordByName(word: String): WordEntity? {
        return wordDao.getWordByName(word)
    }

    suspend fun deleteAllWords() {
        wordDao.deleteAllWords()
    }
} 