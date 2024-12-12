package com.ljchengx.eudic

import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化时预加载数据
        preloadWordData()
    }

    private fun preloadWordData() {
        scope.launch {
            try {
                Log.d("MainActivity", "开始预加载单词数据")
                withContext(Dispatchers.IO) {
                    wordRepository.getWordsFromNetwork()
                }
                Log.d("MainActivity", "单词数据预加载完成")
            } catch (e: Exception) {
                Log.e("MainActivity", "预加载数据失败: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
} 