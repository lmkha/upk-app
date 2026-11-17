# UPK Android app

Multi-module Android app built with Kotlin, Jetpack Compose, Hilt, Retrofit and Proto DataStore.

## Requirements

- Android Studio, or the Android SDK with JDK 17 or newer
- A device or emulator running Android 10 (API 29) or newer

## Getting started

The build needs files that are not in the repository: `secrets.properties` and `app/google-services.json`. The project owner hands them out in a folder named `secrets`, with one subfolder per environment. You may receive only some of the environments.

1. Put the `secrets` folder in the repository root:

   ```text
   secrets/
     dev/
       secrets.properties
       google-services.json
     staging/
     prod/
   ```

2. Copy the files of one environment into place:

   ```bash
   scripts/use-env.sh dev      # macOS, Linux
   scripts\use-env.bat dev     # Windows
   ```

3. Build the debug APK, or sync Gradle and run the `app` configuration from Android Studio:

   ```bash
   ./gradlew :app:assembleDebug
   ```

`secrets/` and the copied files are git-ignored. Never commit them.

## Environments

One build targets one environment. The `ENVIRONMENT` key in `secrets.properties` selects it and sets the identity of the installed app, so the three builds can be installed side by side.

| `ENVIRONMENT` | `applicationId` | App name |
| --- | --- | --- |
| `dev` | `com.example.upk.dev` | UPK Dev |
| `staging` | `com.example.upk.staging` | UPK Staging |
| `prod` | `com.example.upk` | UPK |

To switch environment, run the script again with another name, then sync Gradle. The script removes `secrets.properties` and `app/google-services.json` and writes them again, so change values in `secrets/`, not in the copies. If it fails halfway, a file is missing and the build stops; run the script again.

Each `secrets/<environment>/secrets.properties` starts with its own `ENVIRONMENT` line (`ENVIRONMENT=dev` in `secrets/dev/`, and so on). The script copies the files as they are.

The build fails when `secrets.properties` is missing or `ENVIRONMENT` is not one of the values above.

### Adding a configuration value

Every key in `secrets.properties` becomes a `BuildConfig` field in the modules that apply the secrets Gradle plugin (for example `BuildConfig.API_URL` in `core/data`). To add one:

1. Add the key to `secrets/<environment>/secrets.properties` of every environment, then run the script again.
2. Add the key to the `SECRETS_PROPERTIES` secret of each GitHub Environment. Its content is the same as the file in `secrets/`, `ENVIRONMENT` line included.

To add another file to the setup, add it to `scripts/use-env.sh` and `scripts/use-env.bat`.

## Project structure

```text
app/                     entry point, navigation, theme
core/                    shared code: common, data (network), domain, ui, analytics, crashlytics
feature/<name>/domain/   models, repository interfaces, use cases
feature/<name>/data/     repository implementations, local and remote data sources
feature/<name>/ui/       Compose screens and ViewModels
build-logic/             Gradle convention plugins
scripts/                 environment setup and repository maintenance scripts
```

Features: `auth`, `home`, `payment`, `settings`. Dependencies point one way: `ui -> domain <- data`.

## Common commands

```bash
./gradlew :app:assembleDebug                              # build the debug APK
./gradlew testDebugUnitTest                               # unit tests, whole project
./gradlew :feature:settings:domain:testDebugUnitTest      # unit tests, one module
./gradlew lintDebug                                       # Android lint
./gradlew :feature:settings:ui:connectedDebugAndroidTest  # instrumented tests (needs a device)
```

## CI

| Workflow | Runs on | What it does |
| --- | --- | --- |
| `pr-check.yml` | pull request to `main` | unit tests, lint, debug build, compiles instrumented tests |
| `build-apk.yml` | push to `main`, tag `v*.*.*`, or manual run | builds and uploads the APK of one environment, and publishes a GitHub Release on tags |

`build-apk.yml` builds `dev` (debug) on push to `main`, `staging` (release + GitHub Pre-release) on pre-release tags such as `v1.0.0-rc.1`, and `prod` (release + GitHub Release) on release tags such as `v1.0.0`. A manual run builds the environment you pick. When several pushes reach `main` close together, only the latest one is built: a newer push cancels the dev build still in progress. Tag builds and manual runs are never cancelled.

Each GitHub Environment (`develop`, `staging`, `production`) holds two secrets: `SECRETS_PROPERTIES`, the full content of `secrets.properties` for that environment, and `GOOGLE_SERVICES_JSON`.

## Contributing

- Branch from `main` and open pull requests against `main`. Releases are cut by pushing `v*.*.*` tags (`v1.0.0-rc.1` for staging pre-releases, `v1.0.0` for production releases).
- Commit messages follow Conventional Commits: `feat: add settings screen`, `fix(auth): refresh expired token`.
- Run the pull request checks locally before pushing:

  ```bash
  ./gradlew testDebugUnitTest lintDebug :app:assembleDebug compileDebugAndroidTestKotlin
  ```

- Coding rules, architecture conventions and step-by-step workflows are in [AGENTS.md](AGENTS.md). They are written for AI coding agents and apply to human contributors as well.
