package com.ljchengx.eudic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey
    val word: String,
    val explanation: String,
    val addTime: String,
    val updateTime: Long = System.currentTimeMillis()
) 