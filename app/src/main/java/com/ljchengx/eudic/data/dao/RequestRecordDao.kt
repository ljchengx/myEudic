package com.ljchengx.eudic.data.dao

import androidx.room.*
import com.ljchengx.eudic.data.entity.RequestRecord

@Dao
interface RequestRecordDao {
    @Query("SELECT * FROM request_records WHERE id = 1")
    suspend fun getLastRequestRecord(): RequestRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: RequestRecord)
} 