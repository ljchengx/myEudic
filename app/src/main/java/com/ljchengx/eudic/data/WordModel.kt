package com.ljchengx.eudic.data

import kotlinx.serialization.Serializable

@Serializable
data class WordResponse(
    val data: List<WordItem> = emptyList(),
    val message: String = ""
)

@Serializable
data class WordItem(
    val word: String = "",
    val exp: String = "",
    val add_time: String = ""
) 