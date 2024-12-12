package com.ljchengx.eudic.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.ljchengx.eudic.WordData
import java.time.LocalDateTime

@Entity(
    tableName = "words",
    indices = [Index(value = ["lastUpdateTime"])]
)
data class WordEntity(
    @PrimaryKey
    val word: String,
    val exp: String,
    val addTime: String,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromWordData(wordData: WordData) = WordEntity(
            word = wordData.word,
            exp = wordData.exp,
            addTime = LocalDateTime.now().toString(),
            lastUpdateTime = System.currentTimeMillis()
        )
    }
}

fun WordEntity.toWordData() = WordData(word, exp) 