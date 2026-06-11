# CENTURION - Architecture Flux Schema

> Flux (data/control-flow) schema of the CENTURION Android app, produced with the
> *model fluxes -> Mermaid -> draw.io -> verify* method
> (`HOWTO-flux-diagrams-mermaid-to-drawio.md`). This `.md` is the **authoritative master
> list**; the Mermaid source and the draw.io pages are filtered views of it. The `F#` is the
> join key across every artifact.

Companion artifacts (same folder):
- `centurion-fluxes.mmd` - full validated Mermaid topology (every component + every flux).
- `centurion-fluxes.drawio` - editable draw.io, two pages: **runtime** (F1-F12) and
  **ops-system** (F13-F18). Open with [app.diagrams.net](https://app.diagrams.net).
- `validate_drawio.py` - geometric verifier (box crossings + duplicate connection points), per HOWTO section 11.

## 0. What this system is

A single-user, **fully offline** Android app (Kotlin + Jetpack Compose, MVVM + Clean Architecture).
Confirmed: zero network calls, no `INTERNET` permission; the only manifest permissions are
`POST_NOTIFICATIONS` and `VIBRATE` (`app/src/main/AndroidManifest.xml:5-6`). All persistence is
**Room** (no DataStore/SharedPreferences); every setting is a column on one `UserProfile` row
pinned to `id=1`.

Because there is no network, the HOWTO's "perimeters" map to **architectural layers**, and its
"protocol/auth" fields are **in-process** (function calls, StateFlow, Room, WorkManager, Intents,
file IO) rather than network protocols.

## 1. Perimeters (layers)

| Perimeter | Role | Palette (node fill / stroke) |
|---|---|---|
| **UI Layer** | Jetpack Compose screens + navigation | `#DAE8FC` / `#6C8EBF` (blue) |
| **Presentation Layer** | Hilt ViewModels (state holders) | `#E1D5E7` / `#9673A6` (purple) |
| **Domain Layer** | Pure Kotlin (program data, calorie math) | `#D5E8D4` / `#82B366` (green) |
| **Data Layer** | Room repository, DAOs, SQLite, DI | `#FFE6CC` / `#D79B00` (orange) |
| **System / Android** | WorkManager, notifications, file IO, Coil, external share | `#F8CECC` / `#B85450` (red) |

## 2. Components

Stable `id`s are reused in the Mermaid and draw.io. Evidence is `file:line` under
`app/src/main/java/com/century/app/` unless noted.

### UI Layer
| id | component | type | evidence |
|---|---|---|---|
| `mainActivity` | MainActivity (host + theme) | activity | `MainActivity.kt:18-27` |
| `nav` | CenturyNavHost (navigation graph) | navigation | `ui/navigation/CenturyNavHost.kt:84` |
| `screens` | 8 Compose screens (Onboarding, Home, Program, Workout, Progress, Nutrition, WeightLog, Settings) + Splash | screen | `ui/.../*Screen.kt`, `CenturyNavHost.kt:271` |
| `components` | Shared composables (ExerciseImageCard, SetTrackerRow, ...) | component | `ui/components/SharedComponents.kt` |

### Presentation Layer
| id | component | type | evidence |
|---|---|---|---|
| `vms` | 8 `@HiltViewModel` ViewModels (one per screen) | viewmodel | e.g. `ui/home/HomeViewModel.kt:15`, `ui/workout/WorkoutViewModel.kt:60` |

### Domain Layer
| id | component | type | evidence |
|---|---|---|---|
| `domain` | TrainingProgramData (28-day program; `adjustForFitnessLevel`, `adjustRestForWeek`) | domain | `domain/model/TrainingProgram.kt:42-144` |
| `calc` | CalorieCalculator (MET-based estimate) | util | `util/CalorieCalculator.kt:3-27` |

### Data Layer
| id | component | type | evidence |
|---|---|---|---|
| `repo` | CenturyRepository (`@Singleton`) | repository | `data/repository/CenturyRepository.kt:11-20` |
| `daos` | 6 Room DAOs (UserProfile, WeightLog, WorkoutSession, ExerciseLog, ExerciseImage, PushUpTest) | dao | `data/local/dao/*.kt` |
| `db` | CenturyDatabase - Room/SQLite `century_database`, v2, `MIGRATION_1_2` | database | `data/local/CenturyDatabase.kt:10-22`; `di/AppModule.kt:24-26` |
| `di` | AppModule (Hilt `@Provides` DB + DAOs) | di | `di/AppModule.kt:14-46` |

6 entities/tables: `user_profile`, `weight_log`, `workout_session`, `exercise_log`,
`exercise_image`, `push_up_test`. Relationships are **by-convention id columns**, not Room
`@ForeignKey` (no DB-level cascade); referential cleanup is done manually in `MIGRATION_1_2`
(`CenturyDatabase.kt:90`). Unique indices back the DAO "insert-IGNORE then update" upsert pattern.

### System / Android
| id | component | type | evidence |
|---|---|---|---|
| `app` | CenturyApp (`@HiltAndroidApp`, WorkManager `Configuration.Provider`) | application | `CenturyApp.kt:11-20` |
| `wm` | WorkManager (periodic scheduler) | system-service | `worker/ReminderWorker.kt:81-115` |
| `worker` | ReminderWorker (`CoroutineWorker`) | worker | `worker/ReminderWorker.kt:23-74` |
| `notif` | Notification channel `century_reminders` + NotificationManagerCompat | system-service | `CenturyApp.kt:27-41`; `worker/ReminderWorker.kt:74` |
| `img` | ExerciseImageHelper (internal file IO) | util | `util/ExerciseImageHelper.kt:11` |
| `coil` | Coil AsyncImage loader (local files/resources only) | external | `ui/components/SharedComponents.kt:201-205` |
| `csv` | CSV export (`cacheDir/exports/*.csv`) + FileProvider | system-service | `ui/settings/SettingsViewModel.kt:138-178`; `AndroidManifest.xml:40-48` |
| `share` | External share app (`ACTION_SEND` target) | external | `ui/settings/SettingsViewModel.kt:172-177` |

## 3. Fluxes (master list)

Format: `F# | name | category | trigger | path | mechanism | data | frequency | evidence`.
Drawn on: **R** = runtime page, **O** = ops-system page.

| F# | name | cat | trigger | path | mechanism | evidence | pg |
|---|---|---|---|---|---|---|---|
| F1 | user input | runtime | tap / type / toggle | user -> screens | Compose events | `OnboardingScreen.kt:421`, `WorkoutScreen.kt:197`, `SettingsScreen.kt:404` | R |
| F2 | screen <-> viewmodel | runtime | UI event / recomposition | screens <-> vms | function call down, StateFlow `collectAsState` up | `HomeScreen.kt:36-39`, `WorkoutScreen.kt:41` | R |
| F3 | navigation | navigation | bottom-nav / push / pop | user -> nav -> screens | `navController.navigate` / `popBackStack` | `CenturyNavHost.kt:133-139,175-177,224-226` | R |
| F4 | start-destination | runtime | NavHost composition | nav -> vms -> nav | `hasProfile` StateFlow picks Onboarding/Splash vs Home | `CenturyNavHost.kt:86-91`; `HomeViewModel.kt:20-21,89` | R |
| F5 | theme from profile | data | profile change | vms -> mainActivity | `profile.useDarkTheme` -> `CenturyTheme` (not drawn; read-back of F2/F7) | `MainActivity.kt:24-27` | - |
| F6 | domain compute | data | workout load / nutrition view | vms -> domain, vms -> calc | `adjustForFitnessLevel`/`adjustRestForWeek`; MET calorie calc | `WorkoutViewModel.kt:113-122,469-478`; `TrainingProgram.kt:96-144`; `CalorieCalculator.kt:13-27` | R |
| F7 | viewmodel -> repository | data | screen action / init | vms -> repo | suspend calls + Flow collection | `HomeViewModel.kt:83-101`; `WorkoutViewModel.kt:433-553`; `SettingsViewModel.kt:94-218` | R |
| F8 | repository -> DAO | data | repository method | repo -> daos | Room `@Query/@Insert/@Update/@Delete`, Flow, upsert `@Transaction` | `CenturyRepository.kt:22-105` | R |
| F9 | DAO -> SQLite | data | DAO call | daos -> db | Room runtime executes SQL | `CenturyDatabase.kt:10-22` | R |
| F10 | finish workout (atomic) | data | finish workout | repo -> db (3 DAOs) | `database.withTransaction { session + logs + currentDay }` | `CenturyRepository.kt:63-75` | R |
| F11 | DI provisioning | secrets-config | first injection | di -> db, di -> daos | Hilt `@Provides @Singleton` Room builder + DAO providers | `AppModule.kt:18-46` | R |
| F12 | schema migration | ops | DB open at stored v1 | db -> db | `Migration(1,2)` execSQL (dedup, add column, unique indices) | `CenturyDatabase.kt:31-140` | R |
| F13 | app init | runtime | `Application.onCreate` | app -> wm, app -> notif | WorkManager `Configuration` + `createNotificationChannel` | `CenturyApp.kt:14-41` | O |
| F14 | schedule reminder | ops | reminder toggle / time change / onboarding save | vms -> wm | `enqueueUniquePeriodicWork(UPDATE)` / `cancelUniqueWork` (1 DAY) | `SettingsViewModel.kt:75-92`; `OnboardingViewModel.kt:210`; `ReminderWorker.kt:81-115` | O |
| F15 | reminder fires | ops | WorkManager periodic trigger (daily) | wm -> worker -> repo / domain -> notif -> user | `doWork` reads profile + day label, `notify(100)` (gated by `POST_NOTIFICATIONS`) | `worker/ReminderWorker.kt:30-74` | O |
| F16 | notification tap | runtime | tap notification | user -> mainActivity | `PendingIntent.getActivity` (NEW_TASK\|CLEAR_TASK) | `worker/ReminderWorker.kt:57-70` | O |
| F17 | CSV export + share | integration | Export Data tap | vms -> repo -> csv -> share | IO write `cacheDir/exports/*.csv`, FileProvider URI, `ACTION_SEND` chooser | `SettingsViewModel.kt:138-185`; `AndroidManifest.xml:40-48` | O |
| F18 | exercise image load | data | card render | screens -> img -> coil -> screens | resolve custom `filesDir/exercise_images/<id>.webp` or drawable, Coil renders | `SharedComponents.kt:165-216`; `util/ExerciseImageHelper.kt` (read side) | O |

### One-line flux descriptions
- **F1-F4** - the user-facing path: navigation chooses a start screen from `hasProfile`, the user
  drives a Compose screen, and each screen talks to its ViewModel (events down, state up).
- **F6-F10** - the persistence spine: ViewModels run domain math, then call the Repository, which
  fans out to 6 Room DAOs and SQLite; workout completion is one atomic multi-DAO transaction.
- **F11-F13** - scaffolding: Hilt provides the DB/DAOs, Room migrates v1->v2 on first open, and the
  Application wires WorkManager and the notification channel at startup.
- **F14-F16** - the daily reminder loop: settings/onboarding schedule a periodic worker; it reads the
  profile, builds a day-specific notification, and a tap reopens the app.
- **F17-F18** - the two file-system fluxes: export profile+weights to a shareable CSV, and load
  custom/built-in exercise images from internal storage via Coil.

## 4. Vestige register (wired-but-unused / scaffolded)

Per the HOWTO ("distinguish wired from scaffolded"), these exist in code with **no live caller**.
They are deliberately NOT drawn as live fluxes.

| id | what | status | evidence |
|---|---|---|---|
| V1 | Custom-image WRITE path: `ExerciseImageHelper.saveCustomImage` / `deleteCustomImage`; `ExerciseImageCard(onLongPress=...)` customize hook | dead - never called (only the READ side F18 is wired) | `ExerciseImageHelper.kt:28-68`; `SharedComponents.kt:158` vs `WorkoutScreen.kt:335-338` |
| V2 | `WeightLogViewModel.setDate` / `_selectedDate` and `updateLog` | wired but no UI invokes them (no date-picker / edit-entry UI) - all logs use `System.currentTimeMillis()` | `WeightLogViewModel.kt:28-29,71-75` |
| V3 | Unused DAO methods: `PushUpTestDao.updateTest`, `ExerciseLogDao.getTotalRepsForSession`, the `insertExercise`/`insertExercises` aliases | not delegated by the Repository | `PushUpTestDao.kt:31-32`; `ExerciseLogDao.kt:15-17,52-53` |

Also noted (not fluxes): Settings "About" hard-codes `Version 1.0.0` while the app is at 1.0.6
(`SettingsScreen.kt:449`); `Theme.kt` imports but does not use `isSystemInDarkTheme`.

## 5. Glossary

- **MVVM** - Model-View-ViewModel UI pattern.
- **Hilt** - Dagger-based compile-time dependency injection for Android.
- **DAO** - Data Access Object; the Room interface that maps methods to SQL.
- **Room** - Android SQLite ORM/persistence library.
- **StateFlow / Flow** - Kotlin coroutine streams; `Flow` is cold/observable, `StateFlow` holds a
  current value the UI collects.
- **Upsert** - insert-or-update; here "insert-IGNORE on a unique index, then update".
- **WorkManager** - Android deferred/periodic background-work scheduler.
- **CoroutineWorker** - a WorkManager worker whose `doWork` is a suspend function.
- **FileProvider** - Android component that hands a `content://` URI to another app safely.
- **MET** - Metabolic Equivalent of Task; used to estimate calories burned.
- **POST_NOTIFICATIONS** - Android 13+ runtime permission required to show notifications.

## 6. How to regenerate / keep in sync

1. Edit this master list first (components, `F#` fluxes, evidence).
2. Regenerate `centurion-fluxes.mmd` from it (one node per `id`, one edge per `F#`); validate it
   renders (Mermaid connector or `app.diagrams.net`).
3. Re-import / refine the draw.io pages; keep `F#` and node `id`s identical across both pages.
4. Run `python validate_drawio.py centurion-fluxes.drawio` - expect `crossings: 0` and
   `duplicate connection points: 0`.
5. Re-check facts against the cited `file:line`s after any code change.
