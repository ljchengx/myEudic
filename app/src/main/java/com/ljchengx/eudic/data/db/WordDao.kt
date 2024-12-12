package com.ljchengx.eudic.data.db

import androidx.room.*

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY lastUpdateTime DESC")
    suspend fun getAllWords(): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("SELECT MAX(lastUpdateTime) FROM words")
    suspend fun getLastUpdateTime(): Long?
} 