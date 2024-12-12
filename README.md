# Eudic Widget

<p align="left">
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

## System Requirements

- Android 12.0 (API level 31) or higher
- Launcher that supports widgets

## Installation

1. Download and install the app
2. Long press on the home screen
3. Select "Widgets"
4. Find "Eudic Widget" and add it to your desktop
5. Click the confirm button to switch words

## Usage

- The widget displays the current word and its definition
- Progress is shown in the top right corner (e.g., 1/10)
- Click the confirm button at the bottom to move to the next word
- Progress is automatically saved and continues from where you left off after restart

## Roadmap

- [ ] Add more words
- [ ] Support custom word lists
- [ ] Add word pronunciation
- [ ] Support theme customization
- [ ] Add review functionality

## License

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

## Contact

- GitHub: [ljchengx](https://github.com/ljchengx)
- Email: [Your Email]