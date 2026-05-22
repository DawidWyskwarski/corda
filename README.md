# Corda

Corda is an Android app for musicians. It combines a chromatic and instrument-specific **tuner**, a configurable **metronome**, and an **Inspirations** journal for saving ideas with labels and media — all in one Material Design 3 interface.

## Features

### Tuner
- Real-time pitch detection via the microphone (requires `RECORD_AUDIO`)
- Chromatic mode or instrument-specific tunings (guitar, bass, ukulele and custom setups)
- Custom instruments and tunings stored locally
- Visual feedback for sharp, flat, and in-tune states
- No-tuner mode for ear practice

### Metronome
- Adjustable BPM with accent on the first beat of each bar
- Configurable time signature and play/mute bar patterns
- Low-latency click sounds generated with `AudioTrack`

### Inspirations
- Create and browse a personal library of musical ideas
- Attach images or videos, with thumbnails and playback
- Organize entries with custom labels and filter by label
- Search and manage label collections

### Settings
- Dark mode 
- Base frequency calibration
- English and Polish localization

## Tech stack

| Area | Libraries |
|------|-----------|
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation 3 (type-safe, serializable destinations) |
| DI | Hilt |
| Persistence | Room, DataStore Preferences |
| Audio / pitch | TarsosDSP, `AudioRecord`, `AudioTrack` |
| Media | Coil, Media3 ExoPlayer |

- **Language:** Kotlin  
- **Min SDK:** 24 · **Target SDK:** 36 · **Compile SDK:** 37  
- **JDK:** 11  

## Project structure

```
app/src/main/java/com/example/corda/
├── ui/              # Compose screens, navigation, theme
├── domain/          # Tuner pitch/audio logic, metronome player
├── data/            # Room entities, repositories, media storage
└── di/              # Hilt modules
```

The app follows a simple layered layout: UI ViewModels talk to repositories and domain services; Room and DataStore handle persistence.

## Getting started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (recent stable release recommended)
- Android SDK with API 37 platform tools
- JDK 11+

### Clone and run
```bash
git clone <repository-url>
cd Corda
```

Open the project in Android Studio and run the **app** configuration on a device or emulator. Gradle wrapper is included (`./gradlew`).

From the command line:

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # requires a connected device/emulator
```

Grant microphone permission when prompted — the tuner will not work without it.

## Permissions

| Permission | Used for |
|------------|----------|
| `RECORD_AUDIO` | Tuner pitch detection |

## Localization

String resources live in `app/src/main/res/values/` (English) and `values-pl/` (Polish). Language can be switched in Settings without changing the system locale.