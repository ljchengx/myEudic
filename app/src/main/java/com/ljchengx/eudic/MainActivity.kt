package com.ljchengx.eudic

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ljchengx.eudic.data.repository.WordRepository
import com.ljchengx.eudic.network.WordService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private val wordRepository by lazy {
        WordRepository(
            (application as App).database.wordDao(),
            (application as App).database.requestRecordDao(),
            WordService()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)

        lifecycleScope.launch {
            try {
                // 刷新并保存数据到数据库
                wordRepository.refreshWords("133784439026055309")
                
                // 从数据库观察数据变化
                wordRepository.getAllWords().collectLatest { words ->
                    // 更新UI显示
                    words.forEach { wordEntity ->
                        Log.d("WordDB", "Word: ${wordEntity.word}, Exp: ${wordEntity.explanation}")
                    }
                    
                    // 更新状态文本
                    statusText.text = "已加载 ${words.size} 个单词"
                }
            } catch (e: Exception) {
                Log.e("WordAPI", "Error fetching words", e)
                statusText.text = "加载失败: ${e.message}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private suspend fun checkLastRequestTime() {
        wordRepository.getLastRequestRecord()?.let { record ->
            val lastRequestTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(record.lastRequestTime))
            Log.d("WordDB", "Last request time: $lastRequestTime")
        }
    }
} 