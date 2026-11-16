---
name: add-feature
description: Scaffold a new feature with domain, data and ui modules wired into settings.gradle.kts, following the settings feature as template. Use when asked to create a new feature module, e.g. "/add-feature profile".
---

# Add a feature

Argument: `<feature>` in lower-case (e.g. `profile`). `<Feature>` below is the PascalCase form.

## Steps

1. Confirm `feature/<feature>/` does not exist. If it does, stop and ask.
2. Create three modules by copying the settings modules' build files and adjusting `namespace`:
   - `feature/<feature>/domain/build.gradle.kts` from `feature/settings/domain/build.gradle.kts`, namespace `com.example.upk.<feature>.domain`.
   - `feature/<feature>/data/build.gradle.kts` from `feature/settings/data/build.gradle.kts`, namespace `com.example.upk.<feature>.data`. Drop `upkProtobuf` and the datastore/protobuf dependencies unless the feature needs local persistence; add `project(":core:data")` only if it calls the network.
   - `feature/<feature>/ui/build.gradle.kts` from `feature/settings/ui/build.gradle.kts`, namespace `com.example.upk.<feature>.ui`.
   Each module also needs an empty `consumer-rules.pro` and the standard `proguard-rules.pro` copied from settings.
3. Create the package skeleton:
   ```
   domain/src/main/java/com/example/upk/<feature>/domain/{models,repository,usecases}/
   data/src/main/java/com/example/upk/<feature>/data/{di,repository,local/datasource}/
   ui/src/main/java/com/example/upk/<feature>/ui/
   ```
   with at minimum: `<Feature>Repository` interface, `<Feature>RepositoryImpl`, `<Feature>DataModule` (`@Binds` impl -> interface), `<Feature>ViewModel` + `<Feature>UiState` + `<Feature>Intent`, `<Feature>Screen`.
4. Add to `settings.gradle.kts`, grouped together:
   ```kotlin
   include(":feature:<feature>:domain")
   include(":feature:<feature>:data")
   include(":feature:<feature>:ui")
   ```
5. Add to `app/build.gradle.kts`: `implementation(project(":feature:<feature>:ui"))`, `:domain` and `:data` (data is needed so Hilt sees the bindings).
6. Add a `Destination.<Feature>` object in `app/.../ui/navigation/Destination.kt` and a `composable<Destination.<Feature>>` entry in `app/.../ui/app/App.kt`. Do not add a navigation trigger from another screen unless asked.
7. Create test scaffolding: `Fake<Feature>Repository` under `domain/src/test` and a copy under `ui/src/test` (test source sets are not shared between modules), a `<Feature>ViewModelTest` with at least the initial-state test, and `MainDispatcherRule` copied from `feature/settings/ui/src/test`.
8. Run `./gradlew :feature:<feature>:ui:testDebugUnitTest :app:assembleDebug`.
9. Finish with the file list and:
   ```bash
   git add settings.gradle.kts app/build.gradle.kts app/src/main/java/com/example/upk/ui feature/<feature>
   git commit -m "feat(<feature>): scaffold <feature> feature modules"
   ```
