# Codex Workflows

These rules adapt the generic agent guidance to this Android repo.

## Before Editing

1. Read `docs/codex-map.md`.
2. Pick one row from `docs/context-scopes.md`.
3. Read only the docs and files listed for that row.
4. Check `rtk git status --short` before staging or committing.

Avoid broad scans after the owning area is known. Use `rtk rg` or `rtk grep`
only when the map/table does not identify the owner.

## Editing Rules

- Match existing Kotlin/Compose style.
- Keep Room writes behind `CenturyRepository` when possible.
- If an entity changes, update database version, migration, and exported schema.
- Do not mutate historical Room schema files.
- Keep release signing secrets out of tracked source.
- Do not add custom image picker claims unless the UI flow is actually wired.
- Do not touch screenshots or generated graph output unless the task is specifically about them.

## Parallel Agent Boundaries

Use disjoint write scopes:

- Data/workout persistence: `data/local/**`, `data/repository/**`, `ui/workout/WorkoutViewModel.kt`.
- Program/navigation/stats: `domain/model/TrainingProgram.kt`, `ui/navigation/**`, `ui/program/**`, `ui/home/**`, `ui/progress/**`.
- Settings/weight/reminders/export: `ui/settings/**`, `ui/weightlog/**`, `ui/onboarding/**`, `worker/**`, `file_paths.xml`.
- Shared UI/theme/assets: `ui/components/**`, `ui/theme/**`, `util/ExerciseImageHelper.kt`, docs that describe assets.
- Build/security/config: Gradle files, `.gitignore`, manifest, wrapper files, `Architecture.md`.

Agents must not commit or push unless explicitly asked.

## Git Rules

- Stage explicit files only. Never use `git add .` or `git add -A`.
- Do not stage `.stfolder/`, `.stversions/`, `graphify-out/`, build outputs, secrets, logs, or IDE files.
- Commit messages follow local style: short `fix:` / `docs:` / `chore:` subjects.
- Do not include AI or agent attribution in commit messages.
- Push only after explicit per-push authorization.

## Command Examples

```powershell
rtk git status --short
rtk .\gradlew.bat :app:compileDebugKotlin --offline
rtk git diff --check
rtk rg "pattern" app/src/main/java/com/century/app/ui/workout
```

## Handoff Notes

When ending a coding task, report:

- changed paths,
- verification command and exit status,
- untracked files intentionally left alone,
- release-signing impact if `app/build.gradle.kts` changed.
