package com.ljchengx.eudic.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("data") val data: T,
    @SerializedName("message") val message: String
)

data class WordItem(
    @SerializedName("word") val word: String,
    @SerializedName("exp") val exp: String,
    @SerializedName("add_time") val add_time: String
) 