package com.ljchengx.eudic.data.dao

import androidx.room.*
import com.ljchengx.eudic.data.entity.WordbookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordbookDao {
    @Query("SELECT * FROM wordbooks ORDER BY updateTime DESC")
    fun getAllWordbooks(): Flow<List<WordbookEntity>>

    @Query("SELECT * FROM wordbooks WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedWordbook(): WordbookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordbooks(wordbooks: List<WordbookEntity>)

    @Query("UPDATE wordbooks SET isSelected = CASE WHEN id = :wordbookId THEN 1 ELSE 0 END")
    suspend fun selectWordbook(wordbookId: String)

    @Query("DELETE FROM wordbooks")
    suspend fun deleteAllWordbooks()
} 