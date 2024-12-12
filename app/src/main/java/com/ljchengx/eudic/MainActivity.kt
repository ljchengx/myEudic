package com.ljchengx.eudic

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ljchengx.eudic.data.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {
    private val wordRepository by lazy { WordRepository(applicationContext) }
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        
        // 初始化时预加载数据
        preloadWordData()
    }

    private fun preloadWordData() {
        scope.launch {
            try {
                statusText.text = "正在更新数据..."
                val words = withContext(Dispatchers.IO) {
                    wordRepository.getWords()
                }
                Log.d("MainActivity", "数据更新完成，获取到 ${words.size} 个单词")
                statusText.text = "数据更新成功"
            } catch (e: Exception) {
                Log.e("MainActivity", "预加载数据失败: ${e.message}", e)
                statusText.text = "数据更新失败：${e.message}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
} 