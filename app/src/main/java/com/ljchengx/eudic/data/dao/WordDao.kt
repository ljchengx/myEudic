package com.ljchengx.eudic.data.dao

import androidx.room.*
import com.ljchengx.eudic.data.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY updateTime DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word = :word")
    suspend fun getWordByName(word: String): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("DELETE FROM words WHERE word = :word")
    suspend fun deleteWord(word: String)
} 