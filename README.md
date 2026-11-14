# MediaGrabber

Android Native application built with Kotlin and Jetpack Compose to extract and download media from Instagram and TikTok.

## Architecture & Principles
- **Clean Architecture**: Clear separation of concerns across `domain`, `data`, and `ui` layers.
- **SOLID Principles**: Focused UseCases, repository contracts, and decoupled data sources.
- **Modern Android Stack**: Jetpack Compose, Material 3, Kotlin Coroutines, StateFlow, Retrofit 2, Coil, Media3 ExoPlayer.
- **Auto-Clipboard Detection**: Automatically detects TikTok/Instagram links on app resume and prompts one-tap extraction.
- **Tabbed Experience**: Seamless NavigationBar navigation between Downloader and History/Gallery tabs.
- **Download Engine & MediaStore**: Direct streaming of high-quality MP4/media into Android MediaStore (`Movies/MediaGrabber`), compatible with Scoped Storage (Android 10+), with real-time download notifications and progress tracking.
- **In-App Auto Update**: Automatic release checking against GitHub Releases with direct in-app APK installer.
- **Deep System Integration**: Native clipboard detection, intent handling (`ACTION_SEND`) for seamless sharing from Instagram/TikTok, and Scoped Storage compatibility.

## Tech Stack
- Kotlin 2.x
- Jetpack Compose + Material 3
- AndroidX Lifecycle & ViewModel
- Retrofit 2 + OkHttp + Kotlinx Serialization
- Android MediaStore API (Scoped Storage)
- Android Notifications (Channels, Progress)
- Coil Compose
- AndroidX Media3 (ExoPlayer)
- JUnit & Kotlinx Coroutines Test

## Project Structure
```text
com.portofolio.mediagrabber
├── core
│   ├── notification
│   ├── result
│   └── updater
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── data
│   ├── remote
│   ├── repository
│   └── storage
└── ui
    ├── main
    └── theme
```

## Building
```bash
./gradlew assembleDebug
```
