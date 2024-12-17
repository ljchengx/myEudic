package com.ljchengx.eudic.network

import com.ljchengx.eudic.data.WordResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class WordService {
    private val client = ApiClient.httpClient
    
    companion object {
        private const val AUTH_TOKEN = "NIS kCIT/cTUpouzWdOYkx+RZZ16aUHgyteYF9ZoFVWZe6EUQKkb6vGjmw=="
        private const val MAX_EMPTY_EXP_COUNT = 8
        private const val MAX_RETRY_TIMES = 3  // 最大重试次数，防止无限循环
    }

    suspend fun getWords(userId: String): WordResponse {
        var retryCount = 0
        while (retryCount < MAX_RETRY_TIMES) {
            val response = client.get("https://api.frdic.com/api/open/v1/studylist/words/$userId") {
                parameter("language", "en")
                header(HttpHeaders.Authorization, AUTH_TOKEN)
            }
            
            val wordResponse: WordResponse = response.body()
            
            // 计算空exp的数量
            val emptyExpCount = wordResponse.data.count { it.exp.isBlank() }
            
            if (emptyExpCount < MAX_EMPTY_EXP_COUNT) {
                return wordResponse
            }
            
            println("发现 $emptyExpCount 个空释义，重试第 ${retryCount + 1} 次")
            retryCount++
        }
        
        // 如果重试次数用完还是没有获取到合适的数据，返回最后一次的结果
        return client.get("https://api.frdic.com/api/open/v1/studylist/words/$userId") {
            parameter("language", "en")
            header(HttpHeaders.Authorization, AUTH_TOKEN)
        }.body()
    }
} 