# Eudic Widget

<p align="left">
  <a href="README_zh.md">中文文档</a>
</p>

A minimalist English word learning widget for Android that helps you learn new words every day.

## Features

### Widget Features
- 🔄 Daily word updates with time filtering (1-3 days)
- 🎲 Support for both random and chronological order
- 👁️ Toggleable word explanation visibility
- 📱 Clean and modern widget interface
- ⚙️ Persistent widget settings

### Core Features
- 🌐 Network-based word synchronization
- 💾 Local caching support
- 📚 Multiple wordbook support
- 🔄 Automatic word list updates
- 🎯 Focus on recent words

## Screenshots

[Screenshots to be added]

## Technical Details

### Architecture
- MVVM architecture with Clean Architecture principles
- Kotlin Coroutines for asynchronous operations
- Room Database for local storage
- DataStore for preferences
- Ktor Client for network requests
- Hilt for dependency injection

### Key Components
- `WordRepository`: Manages data operations between network and local storage
- `WordWidget`: Handles widget display and updates
- `WidgetSettings`: Manages widget configuration and preferences
- `WordDatabase`: Local storage implementation

### Dependencies
```kotlin
dependencies {
    // Android core dependencies
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

## System Requirements
- Android 12.0 (API level 31) or higher
- Launcher that supports widgets

## Installation
1. Download and install the app
2. Long press on the home screen
3. Select "Widgets"
4. Find "Eudic Widget" and add it to your home screen
5. Configure widget settings as needed

## Usage
- The widget displays words from your selected wordbook
- Progress is shown in the bottom left corner (e.g., 1/10)
- Click the eye icon to toggle explanation visibility
- Click the next button to move to the next word
- Click the settings icon to access widget configuration
- Settings are automatically saved and persisted

## Version History
- v1.1.0: Enhanced widget with explanation toggle and improved layout
- v1.0.0: Initial stable release with core functionality

## Contributing
Feel free to submit issues and enhancement requests!

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