package com.ljchengx.eudic.network

import com.elvishew.xlog.XLog
import com.ljchengx.eudic.data.WordResponse
import com.ljchengx.eudic.network.model.WordbookResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject

class WordService @Inject constructor() {
    private val client = ApiClient.httpClient
    
    companion object {
        private const val MAX_EMPTY_EXP_COUNT = 8
        private const val MAX_RETRY_TIMES = 5 // 最大重试次数，防止无限循环
    }

    suspend fun getWords(userId: String, token: String): WordResponse {
        XLog.d("开始获取单词列表，userId: $userId")
        var retryCount = 0
        while (retryCount < MAX_RETRY_TIMES) {
            try {
                XLog.d("发起API请求，第 ${retryCount + 1} 次尝试")
                val response = client.get("https://api.frdic.com/api/open/v1/studylist/words/$userId") {
                    parameter("language", "en")
                    header(HttpHeaders.Authorization, token)
                }
                
                val wordResponse: WordResponse = response.body()
                XLog.d("API响应成功，获取到 ${wordResponse.data.size} 个单词")
                
                // 计算空exp的数量
                val emptyExpCount = wordResponse.data.count { it.exp.isBlank() }
                
                if (emptyExpCount < MAX_EMPTY_EXP_COUNT) {
                    XLog.d("空释义数量在可接受范围内，返回结果")
                    return wordResponse
                }
                
                XLog.w("发现 $emptyExpCount 个空释义，重试第 ${retryCount + 1} 次")
                retryCount++
            } catch (e: Exception) {
                XLog.e("API请求失败: ${e.message}", e)
                throw e
            }
        }
        
        XLog.w("达到最大重试次数，返回最后一次结果")
        // 如果重试次数用完还是没有获取到合适的数据，返回最后一次的结果
        return client.get("https://api.frdic.com/api/open/v1/studylist/words/$userId") {
            parameter("language", "en")
            header(HttpHeaders.Authorization, token)
        }.body()
    }

    suspend fun getWordbooks(token: String): WordbookResponse {
        return client.get("https://api.frdic.com/api/open/v1/studylist/category") {
            parameter("language", "en")
            header("Authorization", token)
        }.body()
    }
} 