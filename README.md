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

The script writes `ENVIRONMENT` itself, from the name of the subfolder, so the files in `secrets/` must not define it; the script stops if one does. In CI, the GitHub Environment of the job decides it in the same way.

The build fails when `secrets.properties` is missing or `ENVIRONMENT` is not one of the values above.

### Adding a configuration value

Every key in `secrets.properties` becomes a `BuildConfig` field in the modules that apply the `upk.secrets` convention plugin (for example `BuildConfig.API_URL` in `core/data`).

To add a new key:

1. Add the key to `secrets/<environment>/secrets.properties` of every environment, then run the setup script again (`scripts/use-env.sh <env>` or `scripts\use-env.bat <env>`).
2. Add the key to the `SECRETS_PROPERTIES` secret of each GitHub Environment. Its content is the same as the file in `secrets/`, without `ENVIRONMENT`.

To read values from `secrets.properties` in a module:

1. Apply `alias(libs.plugins.upk.secrets)` in the module's `build.gradle.kts` (it enables `BuildConfig` generation and configures the secrets Gradle plugin to read `secrets.properties`).
2. Read the generated field directly from the module's `BuildConfig` (for example `BuildConfig.API_URL`).

To add another file to the setup, add it to `scripts/use-env.sh` and `scripts/use-env.bat`.

### Release signing

Debug builds are signed with the debug key of the machine. Release builds (`staging` and `prod`) are signed with a keystore that is never committed, and the build stops when it is not configured: a release build never falls back to the debug key.

The build reads four environment variables:

| Variable | Value |
| --- | --- |
| `UPK_RELEASE_KEYSTORE_FILE` | absolute path of the keystore file |
| `UPK_RELEASE_KEYSTORE_PASSWORD` | password of the keystore |
| `UPK_RELEASE_KEY_ALIAS` | alias of the signing key in the keystore |
| `UPK_RELEASE_KEY_PASSWORD` | password of the signing key |

They are environment variables, not keys of `secrets.properties`, because every key of that file is compiled into `BuildConfig` and so into the APK.

To create a keystore (once per environment; keep the file and its passwords somewhere safe, an app can only be updated with the key it was first published with):

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore upk-staging.jks -alias upk -keyalg RSA -keysize 2048 -validity 10000
```

To build a signed release on your machine, set the four variables in the shell and run `./gradlew :app:assembleRelease`. Keep the keystore outside the repository.

In CI, each of the `staging` and `production` GitHub Environments holds four more secrets: `RELEASE_KEYSTORE_BASE64` (the output of `base64 -i upk-staging.jks`), `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` and `RELEASE_KEY_PASSWORD`. `build-apk.yml` stops a staging or production build when one of them is missing.

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
| `build-apk.yml` | tag `v*.*.*`, or manual run | builds the APK of one environment, uploads an artifact for manual runs, and publishes a GitHub Release on tags |

`build-apk.yml` builds `staging` (release + GitHub Pre-release) on pre-release tags such as `v1.0.0-rc.1`, and `prod` (release + GitHub Release) on release tags such as `v1.0.0`. A manual run builds the environment you pick (`develop`, `staging`, or `production`) and uploads the APK as a workflow artifact. Tag builds and manual runs are never cancelled.

Each GitHub Environment (`develop`, `staging`, `production`) holds two secrets: `SECRETS_PROPERTIES`, the full content of `secrets.properties` for that environment, and `GOOGLE_SERVICES_JSON`. `staging` and `production` also hold the four signing secrets described in [Release signing](#release-signing).

## Contributing

- Branch from `main` and open pull requests against `main`. Releases are cut by pushing `v*.*.*` tags (`v1.0.0-rc.1` for staging pre-releases, `v1.0.0` for production releases).
- Commit messages follow Conventional Commits: `feat: add settings screen`, `fix(auth): refresh expired token`.
- Run the pull request checks locally before pushing:

  ```bash
  ./gradlew testDebugUnitTest lintDebug :app:assembleDebug compileDebugAndroidTestKotlin
  ```

- Coding rules, architecture conventions and step-by-step workflows are in [AGENTS.md](AGENTS.md). They are written for AI coding agents and apply to human contributors as well.
