# 每日单词小部件

<p align="right">
  <a href="README.md">English</a>
</p>

一个简约的 Android 英语单词学习小部件，帮助您每天学习新单词。

## 功能特点

- 🔄 每日更新单词
- 📱 简洁的小部件界面
- 🌐 网络同步单词
- 💾 本地缓存支持
- 🎯 专注于最近3天的单词

## 界面预览

[待添加截图]

## 技术细节

### 架构
- MVVM 架构设计
- Kotlin 协程处理异步操作
- Room 数据库实现本地存储
- Ktor Client 处理网络请求
- WorkManager 实现定时任务

### 核心组件
- `WordRepository`: 管理网络和本地数据操作
- `WordWidget`: 处理小部件显示和更新
- `WordUpdateWorker`: 管理定时更新任务
- `WordDatabase`: 本地存储实现

### 依赖项 