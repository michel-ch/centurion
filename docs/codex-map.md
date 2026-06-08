# Codex Map

Read this before scanning source.

This repo is a native Android app for the CENTURION 28-day push-up program.
Use this file to choose the smallest useful context before opening source.

## Repo Shape

- `Architecture.md` - current architecture, build notes, project structure, and navigation overview.
- `README.md` - product overview, feature list, and screenshot references.
- `app/build.gradle.kts` - Android app config, dependencies, release signing, Room schema export.
- `build.gradle.kts` - root Gradle plugin versions.
- `app/src/main/AndroidManifest.xml` - app component, permissions, FileProvider.
- `app/src/main/java/com/century/app/data/local/` - Room database, DAOs, entities, migrations.
- `app/src/main/java/com/century/app/data/repository/` - persistence boundary and transaction orchestration.
- `app/src/main/java/com/century/app/domain/model/TrainingProgram.kt` - program data, exercise prescriptions, day helpers.
- `app/src/main/java/com/century/app/ui/` - Jetpack Compose screens, ViewModels, navigation, shared components, theme.
- `app/src/main/java/com/century/app/util/` - calorie and image helpers.
- `app/src/main/java/com/century/app/worker/` - WorkManager reminder worker.
- `app/schemas/com.century.app.data.local.CenturyDatabase/` - exported Room schema history.
- `docs/*.png` - screenshots only; do not inspect unless the task is visual/docs screenshot related.

## Read First By Task

- Persistence, migrations, data integrity: `Architecture.md`, `docs/context-scopes.md`, then `CenturyDatabase.kt`, affected DAO/entity, and `CenturyRepository.kt`.
- Workout behavior: `docs/context-scopes.md`, then `WorkoutViewModel.kt`, `WorkoutScreen.kt`, `TrainingProgram.kt`.
- Program/navigation: `docs/context-scopes.md`, then `TrainingProgram.kt`, `CenturyNavHost.kt`, `ProgramViewModel.kt`, `ProgramScreen.kt`.
- Stats/progress/home: `docs/context-scopes.md`, then `HomeViewModel.kt`, `HomeScreen.kt`, `ProgressViewModel.kt`, `ProgressScreen.kt`.
- Settings/export/reminders/weight: `docs/context-scopes.md`, then relevant ViewModel/screen plus `ReminderWorker.kt` and `WeightLog.kt`.
- Theme/shared UI/assets: `docs/context-scopes.md`, then `SharedComponents.kt`, `Theme.kt`, `Color.kt`, `ExerciseImageHelper.kt`.
- Build/release/security: `Architecture.md`, `docs/verification-matrix.md`, then Gradle files, manifest, `.gitignore`, `file_paths.xml`.

## Do Not Open Unless Asked

- `app/build/`, `.gradle/`, `.kotlin/`, `.idea/`, `.vscode/`, `.fleet/`
- `graphify-out/`, `.stfolder/`, `.stversions/`
- `*.apk`, `*.aab`, `*.idsig`, `*.jks`, `*.keystore`, `keystore.properties`, `local.properties`
- `docs/*.png`, drawable media/vector assets, launcher assets, generated reports
- Room schema JSON except when changing Room entities, migrations, or database version

## Source Of Truth Rules

- Room schema history is append-only: add a new version file when schema changes; do not mutate older schema files.
- Data writes should go through `CenturyRepository` unless a feature has a narrow DAO-only reason.
- Release signing secrets must stay outside tracked source.
- Use `rtk` before shell commands.
- Prefer focused reads over repo-wide scans once the owning area is known.
