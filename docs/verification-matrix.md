# Verification Matrix

Run commands from repo root: `C:\Users\mtx\desktop\finish\centurion`.
Prefix shell commands with `rtk`.

## Focused Checks

| Change type | Command | Notes |
|---|---|---|
| Kotlin/Compose source | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` | Runs KSP, so Room SQL and Hilt graph are checked. |
| Room entity/DAO/database | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` | Also inspect `app/schemas/...`; add a new schema version, do not edit older schema. |
| Gradle/build config | `rtk .\gradlew.bat :app:compileDebugKotlin --offline` | If release config changed, document signing prerequisites. |
| Release APK | `rtk .\gradlew.bat :app:assembleRelease` | Requires release signing env/local properties. Do not run unless release build is requested. |
| Docs-only routing files | `rtk rg "codex-map|context-scopes|codex-workflows|verification-matrix" docs/README.md` | Confirms index links. |
| Docs path sanity | `rtk rg "app/src/main|Architecture.md|README.md" docs/codex-map.md docs/context-scopes.md docs/codex-workflows.md docs/verification-matrix.md` | Confirms docs mention real repo paths. |
| Markdown size limits | `rtk powershell -NoProfile -Command '$files="docs/codex-map.md","docs/context-scopes.md","docs/codex-workflows.md","docs/verification-matrix.md"; foreach ($f in $files) { $n=(Get-Content $f | Measure-Object -Line).Lines; "$f $n" }'` | Router docs should stay compact. |
| Whitespace/staging hygiene | `rtk git diff --check` | Run before commit. |
| Secret/permission cleanup | `rtk grep 'centurion123|storePassword = "|keyPassword = "|READ_MEDIA_IMAGES|READ_EXTERNAL_STORAGE|CAMERA' .` | PowerShell-safe single quotes. |
| Ignore rules | `rtk git check-ignore -v .stfolder .stversions graphify-out local.properties centurion.jks .env .env.local app/build` | Wrapper jar should not be ignored. |

## Current Test Caveat

There is no `app/src/test` or `app/src/androidTest` test tree in this checkout.
Do not claim tests pass unless tests are added and run. For now, debug compile
is the primary automated verification.

## Release Caveat

Release signing secrets are intentionally externalized. A release build requires
the `CENTURION_RELEASE_*` environment variables or untracked local properties.
Debug compile does not require signing secrets.
