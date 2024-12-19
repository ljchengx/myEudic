package com.ljchengx.eudic.data.repository

import com.elvishew.xlog.XLog
import com.ljchengx.eudic.data.dao.RequestRecordDao
import com.ljchengx.eudic.data.dao.WordDao
import com.ljchengx.eudic.data.dao.WordbookDao
import com.ljchengx.eudic.data.entity.RequestRecord
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.data.entity.WordbookEntity
import com.ljchengx.eudic.network.WordService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val requestRecordDao: RequestRecordDao,
    private val wordbookDao: WordbookDao,
    private val wordService: WordService
) : WordRepository {
    override fun getAllWords(): Flow<List<WordEntity>> = wordDao.getAllWords()

    override fun getAllWordbooks(): Flow<List<WordbookEntity>> = wordbookDao.getAllWordbooks()

    override suspend fun getSelectedWordbook(): WordbookEntity? = wordbookDao.getSelectedWordbook()

    override suspend fun selectWordbook(wordbookId: String) {
        wordbookDao.selectWordbook(wordbookId)
    }

    override suspend fun refreshWordbooks() {
        XLog.d("开始刷新单词本列表")
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
        val response = wordService.getWordbooks(token)
        
        XLog.d("API请求成功，开始保存单词本数据")
        // 获取当前选中的单词本
        val selectedWordbook = wordbookDao.getSelectedWordbook()
        
        // 保存单词本数据，保持选中状态
        val wordbookEntities = response.data.map { item ->
            WordbookEntity(
                id = item.id,
                language = item.language,
                name = item.name,
                addTime = item.add_time,
                isSelected = selectedWordbook?.id == item.id,
                updateTime = System.currentTimeMillis()
            )
        }

        // 如果没有选中的单词本，选择第一个
        if (selectedWordbook == null && wordbookEntities.isNotEmpty()) {
            XLog.d("没有选中的单词本，选择第一个")
            val updatedEntities = wordbookEntities.mapIndexed { index, entity ->
                entity.copy(isSelected = index == 0)
            }
            wordbookDao.insertWordbooks(updatedEntities)
        }
        // 如果之前选中的单词本不在新列表中，选择第一个单词本
        else if (selectedWordbook != null && !response.data.any { it.id == selectedWordbook.id }) {
            XLog.d("之前选中的单词本不在新列表中，选择第一个单词本")
            val updatedEntities = wordbookEntities.mapIndexed { index, entity ->
                entity.copy(isSelected = index == 0)
            }
            wordbookDao.insertWordbooks(updatedEntities)
        } else {
            wordbookDao.insertWordbooks(wordbookEntities)
        }
        
        XLog.d("成功保存 ${wordbookEntities.size} 个单词本")
    }

    override suspend fun refreshWords() {
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

        val selectedWordbook = wordbookDao.getSelectedWordbook()
        if (selectedWordbook == null) {
            XLog.e("未选择单词本")
            throw IllegalStateException("No wordbook selected")
        }
        
        XLog.d("获取到Token和单词本ID，开始请求API")
        val response = wordService.getWords(selectedWordbook.id, token)
        
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

    override suspend fun getLastRequestRecord(): RequestRecord? {
        return requestRecordDao.getLastRequestRecord()
    }

    override suspend fun updateRequestRecord(record: RequestRecord) {
        XLog.d("更新Token: ${record.token}")
        requestRecordDao.insertOrUpdate(record)
    }

    override suspend fun deleteWord(word: String) {
        XLog.d("删除单词: $word")
        wordDao.deleteWord(word)
    }

    override suspend fun getWordByName(word: String): WordEntity? {
        return wordDao.getWordByName(word)
    }

    override suspend fun deleteAllWords() {
        XLog.d("删除所有单词")
        wordDao.deleteAllWords()
    }
} 