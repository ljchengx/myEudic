package com.ljchengx.eudic

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.ljchengx.eudic.data.AppDatabase

class App : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}