package com.ljchengx.eudic

import android.app.Application
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.Printer
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initXLog()
        XLog.d("App onCreate")
    }

    private fun initXLog() {
        val config = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .tag("Eudic")
            .enableThreadInfo()
            .enableStackTrace(2)
            .enableBorder()
            .build()

        val androidPrinter: Printer = AndroidPrinter(true)

        val filePrinter: Printer = FilePrinter.Builder(getExternalFilesDir("log")?.absolutePath)
            .fileNameGenerator(DateFileNameGenerator())
            .build()

        XLog.init(config, androidPrinter, filePrinter)
    }
}