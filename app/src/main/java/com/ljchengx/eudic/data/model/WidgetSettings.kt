package com.ljchengx.eudic.data.model

data class WidgetSettings(
    val filterDays: Int = 0, // 0表示显示所有单词，1-3表示对应天数
    val isRandomOrder: Boolean = false, // 默认按时间顺序
    val hideExplanation: Boolean = false // 默认显示解析
) 