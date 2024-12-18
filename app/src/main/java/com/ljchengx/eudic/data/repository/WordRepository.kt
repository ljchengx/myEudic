package com.ljchengx.eudic.data.repository

import com.elvishew.xlog.XLog
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
        XLog.d("开始刷新单词列表")
        val record = requestRecordDao.getLastRequestRecord()
        if (record == null) {
            XLog.e("未找到请求记录，请先设置Token")
            throw IllegalStateException("Token not found")
        }
        
        val token = record.token
        if (token.isBlank()) {
            XLog.e("Token为空，请先设置Token")
            throw IllegalStateException("Token is empty")
        }
        
        XLog.d("获取到Token，开始请求API")
        val response = wordService.getWords(userId, token)
        
        XLog.d("API请求成功，开始保存单词数据")
        // 保存单词数据
        val wordEntities = response.data.map { wordItem ->
            WordEntity(
                word = wordItem.word,
                explanation = wordItem.exp,
                addTime = wordItem.add_time
            )
        }
        wordDao.insertWords(wordEntities)
        XLog.d("成功保存 ${wordEntities.size} 个单词")

        // 更新请求记录
        requestRecordDao.insertOrUpdate(
            record.copy(
                lastRequestTime = System.currentTimeMillis()
            )
        )
        XLog.d("更新请求记录成功")
    }

    suspend fun getLastRequestRecord(): RequestRecord? {
        return requestRecordDao.getLastRequestRecord()
    }

    suspend fun updateRequestRecord(record: RequestRecord) {
        XLog.d("更新Token: ${record.token}")
        requestRecordDao.insertOrUpdate(record)
    }

    suspend fun deleteWord(word: String) {
        XLog.d("删除单词: $word")
        wordDao.deleteWord(word)
    }

    suspend fun getWordByName(word: String): WordEntity? {
        return wordDao.getWordByName(word)
    }

    suspend fun deleteAllWords() {
        XLog.d("删除所有单词")
        wordDao.deleteAllWords()
    }
} 