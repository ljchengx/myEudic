package com.ljchengx.eudic.network.model

import kotlinx.serialization.Serializable

@Serializable
data class WordbookResponse(
    val data: List<WordbookItem>,
    val message: String
)

@Serializable
data class WordbookItem(
    val id: String,
    val language: String,
    val name: String,
    val add_time: String
) 