package com.ljchengx.eudic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "request_records")
data class RequestRecord(
    @PrimaryKey
    val id: Int = 1, // 只需要一条记录
    val lastRequestTime: Long, // 使用时间戳
    val userId: String, // 用户ID
    val token: String = "" // API Token
) 