# MediaGrabber

Android Native application built with Kotlin and Jetpack Compose to extract and download media from Instagram and TikTok.

## Architecture & Principles

- **Clean Architecture**: Clear separation of concerns across `domain`, `data`, and `ui` layers.
- **SOLID Principles**: Single responsibility UseCases, decoupled repository abstractions, and testable domain logic.
- **Modern Android Stack**: Jetpack Compose, Material 3, Kotlin Coroutines, StateFlow, Retrofit 2, OkHttp 4, Kotlinx Serialization, Coil, Media3 ExoPlayer.
- **Local Persistence & Download History**: SQLite database storing downloaded media metadata, reactive `Flow` updates, with quick actions to play, share, or delete (with confirmation dialog).
- **Built-in Video Player**: Embedded playback with AndroidX Media3 (ExoPlayer) inside the app with full playback controls and lifecycle management.
- **Auto-Clipboard Detection & URL Extractor**: Automatically detects TikTok/Instagram links on app resume, cleans complex share text containing emojis and captions, and prompts one-tap extraction.
- **Tabbed Experience**: Seamless NavigationBar navigation between Downloader and History/Gallery tabs.
- **Download Engine & MediaStore**: Direct streaming of high-quality MP4/media into Android MediaStore (`Movies/MediaGrabber`), compatible with Scoped Storage (Android 10+ to Android 15), with real-time download notifications and progress tracking.
- **In-App Auto Update**: Automatic release checking against GitHub Releases with direct in-app APK installer via FileProvider.
- **Deep System Integration**: Native clipboard detection, intent handling (`ACTION_SEND`) for seamless sharing from Instagram/TikTok, and Scoped Storage compatibility.

## Tech Stack

- Kotlin 2.x
- Jetpack Compose + Material 3
- AndroidX Lifecycle & ViewModel
- Retrofit 2 + OkHttp + Kotlinx Serialization
- Android MediaStore API (Scoped Storage)
- Android Notifications (Channels, Progress)
- Android SQLite Database
- Coil Compose
- AndroidX Media3 (ExoPlayer)
- JUnit 4 & Kotlinx Coroutines Test

## Project Structure

```text
com.portofolio.mediagrabber
├── core
│   ├── notification
│   ├── result
│   ├── updater
│   └── util
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── data
│   ├── local
│   ├── remote
│   ├── repository
│   └── storage
└── ui
    ├── main
    ├── player
    └── theme
```

## Building

```bash
./gradlew testDebugUnitTest assembleDebug
```
