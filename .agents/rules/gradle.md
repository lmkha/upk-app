---
trigger: glob
globs: "**/*.gradle.kts, gradle/libs.versions.toml, build-logic/**, gradle.properties"
description: "Gradle Kotlin DSL, version catalog, module dependency, and build-logic rules."
---

# Gradle and build rules

- Kotlin DSL only. Every dependency and plugin comes from `gradle/libs.versions.toml`:
  - add a `[versions]` entry (camelCase key),
  - add a `[libraries]` entry (kebab-case key, `module = "group:artifact"`, `version.ref`),
  - reference it as `libs.group.artifact` in the module. Never write a version string in a `build.gradle.kts`.
- Compose artifacts have no version: they come from `platform(libs.androidx.compose.bom)`.
- Test dependencies go under `testImplementation` / `androidTestImplementation`; `ui-test-manifest` goes under `debugImplementation`.
- New modules copy the closest existing module's `build.gradle.kts` as a template:
  - domain: `feature/settings/domain/build.gradle.kts`
  - data (with DataStore/proto): `feature/settings/data/build.gradle.kts`
  - ui (Compose): `feature/settings/ui/build.gradle.kts`
- Module dependencies follow `ui -> domain <- data`; `app` depends on `ui` and `domain` modules, plus `data` modules only to pull their Hilt bindings. A `data` module never depends on a `ui` module and vice versa.
- Register new modules in `settings.gradle.kts` with `include(":feature:<name>:<layer>")`, one per line, grouped by feature, no duplicates.
- `namespace` mirrors the path: `com.example.upk.<feature>.<layer>`.
- `compileSdk`, `minSdk`, Java target and `testInstrumentationRunner` are identical across modules; do not change them in one module only.
- Cross-cutting build logic (e.g. protobuf setup) lives in `build-logic/src/main/kotlin/upk.<name>.gradle.kts` and is applied with `alias(libs.plugins.upk<Name>)`. Do not copy plugin configuration between modules.
- After any build-file change run `./gradlew :<module>:testDebugUnitTest` (or `:app:assembleDebug` when no tests exist) to prove it still configures.
- A module that needs a per-environment value applies `alias(libs.plugins.secrets)` with `buildFeatures { buildConfig = true }` and reads its own `BuildConfig`. Never hand-write `buildConfigField` for such values.
- Never touch `local.properties`, `*.properties` secrets or `.idea/` in a change.
