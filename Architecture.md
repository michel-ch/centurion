# CENTURION — Architecture

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Clean Architecture layers
- **Local DB:** Room (SQLite)
- **DI:** Hilt
- **Navigation:** Jetpack Navigation Compose
- **Charts:** Vico (Compose M3)
- **Image Loading:** Coil (Compose-native)
- **Notifications:** WorkManager
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34

## Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Steps
1. Clone/download this project
2. Open in Android Studio
3. Sync Gradle (File > Sync Project with Gradle Files)
4. Run on emulator or physical device (API 26+)

## Project Structure

```
app/src/main/java/com/century/app/
├── CenturyApp.kt              # Application class (Hilt, notifications)
├── MainActivity.kt            # Single activity entry point
├── data/
│   ├── local/
│   │   ├── CenturyDatabase.kt # Room database
│   │   ├── dao/               # Data access objects
│   │   └── entity/            # Room entities
│   └── repository/
│       └── CenturyRepository.kt
├── di/
│   └── AppModule.kt           # Hilt dependency injection
├── domain/
│   └── model/
│       └── TrainingProgram.kt  # Complete 4-week program data
├── ui/
│   ├── theme/                 # Dark brutalist theme
│   ├── navigation/            # NavHost and routes
│   ├── components/            # Shared UI components
│   ├── onboarding/            # User profile setup
│   ├── home/                  # Dashboard screen
│   ├── workout/               # Active workout tracking
│   ├── program/               # 4-week calendar view
│   ├── progress/              # Stats and charts
│   ├── nutrition/             # Calorie & macro calculator
│   ├── weightlog/             # Body weight tracking
│   └── settings/              # App configuration
├── util/                      # Helpers (calories, images)
└── worker/                    # WorkManager notifications
```

## Exercise Illustrations

The app uses a placeholder system for exercise images. To add real illustrations:

1. Create WebP images (800px wide, 16:9 ratio recommended)
2. Name them `exercise_{illustration_id}.webp` (e.g., `exercise_push_up_standard.webp`)
3. Place in `app/src/main/res/drawable/`
4. The app will automatically use them instead of placeholders

Users can also replace any exercise image with their own photos via long-press on the image card.

See the full list of illustration IDs in `TrainingProgram.kt`.

## Screens Navigation

```
Onboarding ──► Home ──► Workout
                  ├──► Program ──► Workout
                  ├──► Progress
                  ├──► Nutrition
                  ├──► Weight Log
                  └──► Settings
```

## Design

- **Background:** #0A0A0A
- **Accent:** #D4121A (Red)
- **Success:** #22C55E (Green)
- **Typography:** Monospace headings, sans-serif body
- **Aesthetic:** Brutalist fitness — bold, high contrast, sharp corners

## License

Private project — all rights reserved.