# UPK Android app

Multi-module Android app built with Kotlin, Jetpack Compose, Hilt, Retrofit and Proto DataStore.

## Requirements

- Android Studio, or the Android SDK with JDK 17 or newer
- A device or emulator running Android 10 (API 29) or newer

## Getting started

The build needs two files that are not in the repository. Ask the project owner for their contents.

1. Create `secrets.properties` in the repository root from the template and fill in the values of the environment you want to build:

   ```bash
   cp secrets.properties.example secrets.properties
   ```

2. Put the Firebase config at `app/google-services.json`. It must contain a client for the `applicationId` of that environment (see [Environments](#environments)).

3. Build the debug APK, or run the `app` configuration from Android Studio:

   ```bash
   ./gradlew :app:assembleDebug
   ```

Both files are git-ignored. Never commit them.

## Environments

One build targets one environment. The `ENVIRONMENT` key in `secrets.properties` selects it and sets the identity of the installed app, so the three builds can be installed side by side.

| `ENVIRONMENT` | `applicationId` | App name |
| --- | --- | --- |
| `dev` | `com.example.upk.dev` | UPK Dev |
| `staging` | `com.example.upk.staging` | UPK Staging |
| `prod` | `com.example.upk` | UPK |

To switch environment, replace the values in `secrets.properties` and rebuild. Replace `app/google-services.json` too, unless it already contains the clients of all three environments.

The build fails when `secrets.properties` is missing or `ENVIRONMENT` is not one of the values above.

### Adding a configuration value

Every key in `secrets.properties` becomes a `BuildConfig` field in the modules that apply the secrets Gradle plugin (for example `BuildConfig.API_URL` in `core/data`). To add one:

1. Add the key to your `secrets.properties`.
2. Add the key, without a value, to `secrets.properties.example`.
3. Add the key to the `SECRETS_PROPERTIES` secret of each GitHub Environment.

## Project structure

```text
app/                     entry point, navigation, theme
core/                    shared code: common, data (network), domain, ui, analytics, crashlytics
feature/<name>/domain/   models, repository interfaces, use cases
feature/<name>/data/     repository implementations, local and remote data sources
feature/<name>/ui/       Compose screens and ViewModels
build-logic/             Gradle convention plugins
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
| `pr-check.yml` | pull request to `main` or `develop` | unit tests, lint, debug build, compiles instrumented tests |
| `build-apk.yml` | push to `develop` or `main`, or manual run | builds and uploads the APK of one environment |

`build-apk.yml` builds `dev` (debug) on `develop` and `prod` (release) on `main`. A manual run builds the environment you pick, including `staging` (release).

Each GitHub Environment (`develop`, `staging`, `production`) holds two secrets: `SECRETS_PROPERTIES`, the full content of `secrets.properties` for that environment, and `GOOGLE_SERVICES_JSON`.

## Contributing

- Branch from `develop` and open pull requests against `develop`. `main` is for releases and only accepts pull requests that pass the checks.
- Commit messages follow Conventional Commits: `feat: add settings screen`, `fix(auth): refresh expired token`.
- Run the pull request checks locally before pushing:

  ```bash
  ./gradlew testDebugUnitTest lintDebug :app:assembleDebug assembleDebugAndroidTest
  ```

- Coding rules, architecture conventions and step-by-step workflows are in [AGENTS.md](AGENTS.md). They are written for AI coding agents and apply to human contributors as well.
