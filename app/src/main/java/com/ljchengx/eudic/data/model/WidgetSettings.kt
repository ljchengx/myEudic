package com.ljchengx.eudic.data.model

data class WidgetSettings(
    val filterDays: Int = 1, // 默认1天内
    val isRandomOrder: Boolean = false, // 默认按时间顺序
    val hideExplanation: Boolean = false // 默认显示解析
) 