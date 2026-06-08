# Context Scopes

Use this table to route tasks to the smallest practical context. Tests are not
present yet; where no test exists, use the focused Gradle verification command.

| Task | Read first | Source files | Tests / verification |
|---|---|---|---|
| Room schema, migration, data integrity | `Architecture.md`, `docs/codex-map.md` | `app/src/main/java/com/century/app/data/local/CenturyDatabase.kt`, affected DAO/entity, `app/src/main/java/com/century/app/data/repository/CenturyRepository.kt`, matching `app/schemas/.../*.json` | `rtk .\gradlew.bat :app:compileDebugKotlin --offline`; inspect generated schema diff |
| Workout loading, progress, completion | `docs/codex-map.md` | `app/src/main/java/com/century/app/ui/workout/WorkoutViewModel.kt`, `WorkoutScreen.kt`, `TrainingProgram.kt`, `CenturyRepository.kt` | `rtk .\gradlew.bat :app:compileDebugKotlin --offline`; manually review Room transaction paths |
| Program calendar and day locking | `docs/codex-map.md` | `TrainingProgram.kt`, `ui/program/ProgramViewModel.kt`, `ui/program/ProgramScreen.kt` | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` |
| Navigation routes | `docs/codex-map.md` | `ui/navigation/CenturyNavHost.kt`, route owner screen/ViewModel | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` |
| Home dashboard stats | `docs/codex-map.md` | `ui/home/HomeViewModel.kt`, `ui/home/HomeScreen.kt`, `CenturyRepository.kt`, relevant DAO | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` |
| Progress charts/stats | `docs/codex-map.md` | `ui/progress/ProgressViewModel.kt`, `ui/progress/ProgressScreen.kt`, relevant data entity/DAO | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` |
| Onboarding profile creation | `docs/codex-map.md` | `ui/onboarding/OnboardingViewModel.kt`, `ui/onboarding/OnboardingScreen.kt`, `UserProfile.kt`, `ReminderWorker.kt` if reminders change | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` |
| Settings, export, reminders | `docs/codex-map.md` | `ui/settings/SettingsViewModel.kt`, `ui/settings/SettingsScreen.kt`, `worker/ReminderWorker.kt`, `app/src/main/res/xml/file_paths.xml` | `rtk .\gradlew.bat :app:compileDebugKotlin --offline`; verify FileProvider paths |
| Weight log and unit conversion | `docs/codex-map.md` | `WeightLog.kt`, `ui/weightlog/WeightLogViewModel.kt`, `ui/weightlog/WeightLogScreen.kt`, affected stats screens | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` |
| Nutrition calculations | `docs/codex-map.md` | `ui/nutrition/NutritionViewModel.kt`, `ui/nutrition/NutritionScreen.kt`, `util/CalorieCalculator.kt`, `UserProfile.kt` | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` |
| Shared Compose components | `docs/codex-map.md` | `ui/components/SharedComponents.kt`, `ui/theme/Theme.kt`, `ui/theme/Color.kt` | `rtk .\gradlew.bat :app:compileDebugKotlin --offline`; visual review if UI changed |
| Exercise images/assets | `Architecture.md` exercise section | `util/ExerciseImageHelper.kt`, `SharedComponents.kt`, `TrainingProgram.kt`, affected drawable resources | `rtk .\gradlew.bat :app:compileDebugKotlin --offline`; verify ID map/fallback logic |
| Build config, release, signing | `Architecture.md`, `docs/verification-matrix.md` | `build.gradle.kts`, `app/build.gradle.kts`, `gradle/wrapper/*`, `.gitignore`, manifest | `rtk .\gradlew.bat :app:compileDebugKotlin --offline`; release build only with signing configured |
| Documentation-only updates | `README.md`, `Architecture.md`, this docs folder | affected Markdown files only | docs router checks in `docs/verification-matrix.md` |

## Scope Notes

- Do not read screenshots or drawable assets for logic tasks.
- Do not infer data contracts from UI if Room entities/DAOs define them.
- Do not change release/versioning while fixing app code unless the task asks for a release.
- Keep edits within the row's source files unless verification exposes a direct dependency.
