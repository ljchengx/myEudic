# Eudic Widget (优词小组件)

<p align="left">
  <a href="README.md">English Documentation</a>
</p>

一个极简的英语单词学习小组件，帮助你每天学习新单词。

## 功能特点

### 小组件功能
- 🔄 每日单词更新，支持时间过滤（1-3天）
- 🎲 支持随机和时间顺序显示
- 👁️ 可切换解释显示/隐藏
- 📱 简洁现代的界面设计
- ⚙️ 小组件设置本地持久化

### 核心功能
- 🌐 网络同步单词数据
- 💾 本地缓存支持
- 📚 多单词本支持
- 🔄 自动更新单词列表
- 🎯 专注于最近添加的单词

## 截图

[截图待添加]

## 技术细节

### 架构
- 基于 MVVM 的清洁架构
- Kotlin 协程处理异步操作
- Room 数据库本地存储
- DataStore 偏好设置存储
- Ktor Client 网络请求
- Hilt 依赖注入

### 核心组件
- `WordRepository`: 管理网络和本地存储之间的数据操作
- `WordWidget`: 处理小组件显示和更新
- `WidgetSettings`: 管理小组件配置和偏好设置
- `WordDatabase`: 本地存储实现

### 依赖项
```kotlin
dependencies {
    // Android 核心依赖
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    
    // Ktor
    implementation("io.ktor:ktor-client-android:2.3.8")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

## 系统要求
- Android 12.0 (API level 31) 或更高版本
- 支持小组件的启动器

## 安装说明
1. 下载并安装应用
2. 长按主屏幕
3. 选择"小部件"
4. 找到"优词小组件"并添加到主屏幕
5. 根据需要配置小组件设置

## 使用说明
- 小组件显示来自选定单词本的单词
- 左下角显示进度（例如：1/10）
- 点击眼睛图标切换解释显示/隐藏
- 点击下一个按钮切换到下一个单词
- 点击设置图标访问小组件配置
- 设置会自动保存并持久化

## 版本历史
- v1.1.0: 增强小组件功能，添加解释切换和改进布局
- v1.0.0: 首个稳定版本，包含核心功能

## 贡献
欢迎提交问题和功能改进建议！