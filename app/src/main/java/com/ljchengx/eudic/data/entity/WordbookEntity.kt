package com.ljchengx.eudic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wordbooks")
data class WordbookEntity(
    @PrimaryKey
    val id: String,
    val language: String,
    val name: String,
    val addTime: String,
    val isSelected: Boolean = false,
    val updateTime: Long = System.currentTimeMillis()
) 