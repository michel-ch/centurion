# CENTURY — 100 Push-Up Challenge

A native Android app built with Kotlin and Jetpack Compose that guides users through a 30-day bodyweight training program to build up to 100 consecutive push-ups.

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

## Features

- **30-Day Program:** 4 weeks progressing from foundation to the 100 push-up challenge
- **Fitness Level Adaptation:** Beginner/Intermediate/Advanced adjustments
- **Rest Timer System:** Auto-countdown between sets and exercises with progressive reduction
- **Set-by-Set Tracking:** Individual set completion with visual circles
- **Timed Exercise Support:** Inline countdown for planks, wall sits, etc.
- **Body Weight Logging:** Track weight with trend visualization
- **Push-Up Test Tracking:** Record max push-up tests on Day 6 of each week
- **Progress Stats:** BMI, streaks, total reps, calorie estimates
- **Nutrition Calculator:** Personalized BMR/TDEE, daily calorie target, macronutrient split (protein/carbs/fat), hydration goal, BMI scale, and goal-aware tips — derived from your profile (cut / maintain / bulk)
- **Daily Reminders:** Configurable notifications via WorkManager
- **Custom Exercise Images:** Replace any illustration with personal photos
- **Dark Brutalist Theme:** High-contrast dark theme with red accents
- **Data Export:** Export workout history as CSV

## Design

- **Background:** #0A0A0A
- **Accent:** #D4121A (Red)
- **Success:** #22C55E (Green)
- **Typography:** Monospace headings, sans-serif body
- **Aesthetic:** Brutalist fitness — bold, high contrast, sharp corners

## Screens

| Screen | Description |
|---|---|
| Onboarding | Multi-step profile setup (name, gender, age, height, weight, fitness level, goal weight, notification time) |
| Home | Today's workout card, quick-access grid, streak counter |
| Workout | Set-by-set tracking with rest timers, timed exercise countdowns, completion summary |
| Program | 4-week calendar grid with day status (complete / today / locked) |
| Progress | Charts for push-up reps over time, weight trend, total reps, streaks, calorie burn |
| Nutrition | BMR/TDEE, daily calorie target, macro split, hydration goal, BMI scale, nutrition tips |
| Weight Log | Daily weigh-in entry with trend chart |
| Settings | Notification toggle/time picker, unit preferences (kg/lbs, cm/ft), data reset, CSV export |

## Screens Navigation

```
Onboarding ──► Home ──► Workout
                  ├──► Program ──► Workout
                  ├──► Progress
                  ├──► Nutrition
                  ├──► Weight Log
                  └──► Settings
```

## License

Private project — all rights reserved.
