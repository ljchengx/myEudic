# Eudic Widget

<p align="right">
  <a href="README_zh.md">中文文档</a>
</p>

A minimalist English word learning widget for Android that helps you learn new words every day.

## Features

- 🔄 Daily word updates
- 📱 Clean and simple widget interface
- 🌐 Network-based word synchronization
- 💾 Local caching support
- 🎯 Focus on words from the last 3 days

## Screenshots

[Screenshots to be added]

## Technical Details

### Architecture
- MVVM architecture
- Kotlin Coroutines for asynchronous operations
- Room Database for local storage
- Ktor Client for network requests
- WorkManager for scheduled tasks

### Key Components
- `WordRepository`: Manages data operations between network and local storage
- `WordWidget`: Handles widget display and updates
- `WordUpdateWorker`: Manages scheduled word updates
- `WordDatabase`: Local storage implementation

### Dependencies
```kotlin
dependencies {
    // Android dependencies
}
```

## 系统要求

- Android 12.0 (API level 31) 或更高版本
- 支持小部件的启动器

## 安装说明

1. 下载并安装应用
2. 长按主屏幕空白处
3. 选择"小部件"
4. 找到"Eudic Widget"并添加到桌面
5. 点击确认按钮切换单词

## 使用说明

- 小部件会显示当前单词和其释义
- 右上角显示当前进度（如：1/10）
- 点击底部的确认按钮进入下一个单词
- 进度会自动保存，重启后继续上次的学习

## 开发计划

- [ ] 添加更多单词
- [ ] 支持自定义单词列表
- [ ] 添加单词发音功能
- [ ] 支持主题自定义
- [ ] 添加复习功能

## 许可证

MIT License

Copyright (c) 2024 ljchengx

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## 联系方式

- GitHub: [ljchengx](https://github.com/ljchengx)
- Email: [您的邮箱]