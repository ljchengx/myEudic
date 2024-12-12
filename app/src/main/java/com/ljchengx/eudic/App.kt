package com.ljchengx.eudic

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 WorkManager
        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(
                this,
                Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
                    .build()
            )
        }
    }
} 