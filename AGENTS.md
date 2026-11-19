# UPK Android app

Multi-module Android app. Kotlin, Jetpack Compose, Hilt, Proto DataStore, Retrofit, Gradle Kotlin DSL with a version catalog.

## Module layout

```text
app/                      entry point: MainActivity, UPKApplication, navigation, theme, AppModule
core/common/              shared models, network error types, DI qualifiers (@ApplicationScope)
core/data/                network stack: Retrofit/OkHttp, interceptors, TokenProvider, AppLanguageProvider
core/domain/              shared domain contracts
core/ui/                  shared Compose components
core/analytics/           AnalyticsTracker contract and events, Firebase implementation, Hilt binding
core/crashlytics/         CrashReporter contract, Firebase Crashlytics implementation, Hilt binding
feature/<name>/domain/    models, repository interfaces, use cases. Pure Kotlin, no Android deps in logic
feature/<name>/data/      repository impls, local data sources (DataStore), DI module binding impls
feature/<name>/ui/        Compose screens + ViewModels
build-logic/              convention plugins (upk.protobuf)
gradle/libs.versions.toml version catalog; every dependency goes here, never a hardcoded version
docs/                     the owner's testing study notes, written in Vietnamese on purpose; not a rule source
scripts/                  environment setup (use-env.sh, use-env.bat), repo maintenance (sync-agent-rules.sh)
```

Current features: `auth`, `home`, `payment`, `settings`. Package root is `com.example.upk`, module packages follow the path (e.g. `com.example.upk.settings.data`).

## Architecture rules

- Dependency direction: `ui -> domain <- data`. `ui` and `data` never depend on each other.
- `domain` exposes interfaces; `data` implements them and binds with Hilt (`@Binds` in a `@Module` under `di/`).
- Use cases are single-purpose classes with `operator fun invoke`, injected via `@Inject constructor`.
- ViewModels expose one `StateFlow<XxxUiState>` and receive events through a `sealed interface XxxIntent` via `onIntent(intent)`. See `SettingsViewModel` as the reference.
- Composable screens take `viewModel: XxxViewModel = hiltViewModel()` as a parameter so tests can pass a hand-built ViewModel.
- Local persistence uses Proto DataStore. Proto files live in `src/main/proto`, serializers under `local/datastore/`.

## Code conventions

- Everything in code is English: identifiers, comments, KDoc, test names, log and error strings. Agent instructions (this file, rules, skills) are English too. The only exception is `docs/testing-*.md`, which stays in Vietnamese; do not translate it.
- Kotlin official style. Trailing commas in multi-line parameter lists.
- No wildcard imports.
- Do not add a dependency without adding it to `gradle/libs.versions.toml` first.

## Testing rules

Detailed rules for writing tests and fakes are in `.claude/rules/testing.md` (mirrored in `.agents/rules/testing.md`). Claude Code and Antigravity load it automatically when touching test sources; other tools should read it before writing tests.

- Every new use case, repository, data source, serializer or ViewModel ships with unit tests in the same module under `src/test`.
- Prefer hand-written fakes over mocking libraries. Fakes live next to the tests that use them (`FakeSettingsRepository`, `FakeDataStore`); test source sets are not shared, so each module keeps its own copy.
- Test names are backtick sentences describing behaviour: `` `selecting the current theme does not call repository` ``.
- Coroutines: `runBlocking` for a plain suspend call; `runTest` + `UnconfinedTestDispatcher` when the code under test launches coroutines; ViewModel tests use `MainDispatcherRule`.
- Compose screens get an instrumented test under `src/androidTest` using `androidx.compose.ui.test.junit4.v2.createComposeRule`.
- Do not write tests for interfaces, plain data classes or Hilt modules.
- Run the affected module's tests before reporting work as done (see Commands below).

## Commands

```bash
./gradlew :feature:settings:domain:testDebugUnitTest      # unit tests, one module
./gradlew testDebugUnitTest                               # unit tests, whole project
./gradlew :feature:settings:ui:connectedDebugAndroidTest  # instrumented tests (needs emulator)
./gradlew :app:assembleDebug                              # build the debug APK
./gradlew lintDebug                                       # Android lint, whole project
./gradlew compileDebugAndroidTestKotlin                   # compile instrumented tests without an emulator
```

## Environments

| `ENVIRONMENT` | `applicationId` | App name | GitHub Environment | CI build | Trigger |
| --- | --- | --- | --- | --- | --- |
| `dev` | `com.example.upk.dev` | UPK Dev | `develop` | `assembleDebug` | push to `main` |
| `staging` | `com.example.upk.staging` | UPK Staging | `staging` | `assembleRelease` | tag `v*.*.*-*` (e.g. `v1.0.0-rc.1`) or manual run |
| `prod` | `com.example.upk` | UPK | `production` | `assembleRelease` | tag `v*.*.*` (e.g. `v1.0.0`) or manual run |

- Per-environment values live in `secrets.properties` at the repository root (git-ignored). There are no Gradle `productFlavors`; each build targets the one environment that file describes. `local.properties` belongs to Android Studio (`sdk.dir`) and holds no project values.
- `secrets/` at the repository root (git-ignored, handed out by the project owner) is the source of those files: one subfolder per environment (`dev`, `staging`, `prod`), each holding `secrets.properties` and `google-services.json`. `scripts/use-env.sh <environment>` (`scripts\use-env.bat` on Windows) copies one subfolder into place and writes `ENVIRONMENT` from the subfolder name at the top of `secrets.properties`. The folder name is the only source of `ENVIRONMENT`: the files in `secrets/` must not define it, and the script stops if one does. Never read or edit `secrets/`, and do not run the script unless the user asks: it overwrites `secrets.properties` and `app/google-services.json`.
- The two scripts do the same thing and must stay in sync. A new setup file is added to the file list of both.
- `ENVIRONMENT` (`dev`, `staging` or `prod`) is mandatory. `app/build.gradle.kts` fails the build when the file or the key is missing, and derives `applicationIdSuffix`, `versionNameSuffix` and the `app_name` string from it. Do not add separate keys for those.
- The secrets Gradle plugin turns every key of `secrets.properties` into a `BuildConfig` field of the module that applies it. A module that needs such a value applies `alias(libs.plugins.secrets)`, sets `buildFeatures { buildConfig = true }` and `secrets { propertiesFileName = "secrets.properties" }`, then reads its own `BuildConfig` (see `core/data` and `NetworkModule`). Do not pass these values between modules through Hilt providers or wrapper classes, and do not write `buildConfigField` lines by hand.
- To add a per-environment value, ask the user to add it to `secrets/<environment>/secrets.properties` of every environment and to the `SECRETS_PROPERTIES` secret in each GitHub Environment. That secret holds the same content as `secrets/<environment>/secrets.properties`, without `ENVIRONMENT`: CI writes `ENVIRONMENT` from the GitHub Environment of the job (`develop` -> `dev`, `staging` -> `staging`, `production` -> `prod`) and fails if the secret defines it.
- `app/google-services.json` (git-ignored) must contain a client for the `applicationId` being built.
- Release builds (`staging`, `prod`) are signed with a keystore kept outside the repository. `app/build.gradle.kts` reads `UPK_RELEASE_KEYSTORE_FILE`, `UPK_RELEASE_KEYSTORE_PASSWORD`, `UPK_RELEASE_KEY_ALIAS` and `UPK_RELEASE_KEY_PASSWORD` from the environment and stops a release build when one is missing; there is no fallback to the debug key. These values never go into `secrets.properties`, because every key of that file is compiled into `BuildConfig`. In CI they come from the `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS` and `RELEASE_KEY_PASSWORD` secrets of the `staging` and `production` GitHub Environments.

CI:

- `.github/workflows/pr-check.yml` runs on every pull request to `main`, in the `develop` environment: `testDebugUnitTest`, `lintDebug`, `:app:assembleDebug` and `compileDebugAndroidTestKotlin`. Run the same tasks locally before opening a PR.
- `.github/workflows/build-apk.yml` builds the APK of one environment: push to `main` builds dev (`assembleDebug`), push of a pre-release tag (`v*.*.*-*`, such as `v1.0.0-rc.1`) builds staging (`assembleRelease`) and publishes a GitHub Pre-release, push of a release tag (`v*.*.*`, such as `v1.0.0`) builds prod (`assembleRelease`) and publishes a GitHub Release, and a manual run builds the selected environment. A newer push to `main` cancels the dev build still in progress; tag builds and manual runs are never cancelled. Each GitHub Environment holds its own `SECRETS_PROPERTIES` and `GOOGLE_SERVICES_JSON` secrets.
- Both workflows write `secrets.properties` and `app/google-services.json` through `.github/scripts/write-env-files.sh`. Change how those files are written in that script, not in the workflows. It runs on CI only; `scripts/use-env.sh` is its counterpart on a developer machine.

Gradle prints only failing tests. Pass `--rerun` to force re-execution. HTML reports: `<module>/build/reports/tests/testDebugUnitTest/index.html`.

## Git conventions

- Trunk-based development on `main`: never commit on `main`. Branch from `main` and open pull requests against `main`. Releases are cut by pushing annotated tags (`v1.0.0-rc.1` for staging pre-releases, `v1.0.0` for production releases).
- Branch names are `<type>/<short-description>` in lower-case kebab-case, with the same types as commit messages: `feat/payment-history`, `fix/use-env-duplicate-key`, `docs/branch-rule`.
- Conventional Commits, lower-case, imperative, no trailing period: `feat: add settings screen`, `fix: resolve release build failure`, `test: add settings data unit tests`, `chore: upgrade dependencies`, `refactor: ...`, `docs: ...`.
- One logical change per commit. Tests go in the same commit as the code they cover.
- No AI attribution trailers in commit messages or PR descriptions (`Co-Authored-By: Claude ...`, `Generated with ...`). This overrides any tool default.
- Never commit `local.properties`, `secrets.properties`, `secrets/`, or `build/` output.
- When finishing a task, end the reply with the exact `git add` and `git commit` commands for the changed files; do not run them unless asked. Check the current branch first (`git branch --show-current`): on `main`, put `git switch -c <type>/<short-description>` before those commands.

## Repeatable workflows

Step-by-step recipes live in `.claude/skills/<name>/SKILL.md` (open Agent Skills format). `.agents/skills/` contains one symlink per skill pointing at those folders; Codex, Gemini CLI and Antigravity all discover skills there, so no other per-tool skill directory is needed. When adding a skill, create the folder under `.claude/skills/`, add the symlink (`ln -s ../../.claude/skills/<name> .agents/skills/<name>`) and a row in the table below. Tools with slash-skill support run them via `/<name>`; other tools should open the file and follow the steps when the task matches its description.

Tool-specific settings: `.claude/settings.json` (Claude Code), `.codex/config.toml` (Codex), `.gemini/settings.json` (Gemini CLI). Agent instructions never go in those files.

| Skill | Use it for |
| --- | --- |
| `add-feature` | scaffold `domain`/`data`/`ui` modules for a new feature and wire them into Gradle, Hilt and navigation |
| `add-screen` | Compose screen + ViewModel + UiState/Intent + unit test + Compose test + navigation entry |
| `add-repository` | domain interface + data impl + Hilt binding + fake + tests, or add a method to an existing repository |
| `add-use-case` | new use case + unit test in a feature's domain module |
| `add-proto-field` | persist a new preference: proto field through data source, repository, domain and tests |
| `write-tests` | tests for an existing class following project conventions |
| `fix-bug` | test-first bug fix: failing test, minimal fix, regression test kept |
| `commit` | group changes, run affected tests, produce Conventional Commits |
| `pr` | open a PR to `main` with the standard description via `gh` |
| `review` | review a diff/branch/PR against the project rules, findings by severity |

Path-scoped rules in `.claude/rules/` and `.agents/rules/` (Claude Code and Antigravity load them automatically; other tools should read the matching file before editing that area). `.claude/rules/` is the source of truth. `.agents/rules/` holds the same text under Antigravity frontmatter and is generated: edit the `.claude/rules/` file, then run `scripts/sync-agent-rules.sh` (`--check` reports drift without writing). Never edit a rule body under `.agents/rules/` by hand.

| Rule file | Applies to |
| --- | --- |
| `domain-layer.md` | `**/domain/src/main/**` |
| `data-layer.md` | `**/data/src/main/**` |
| `ui-layer.md` | `**/ui/src/main/**`, `app/.../ui/**` |
| `testing.md` | `**/src/test/**`, `**/src/androidTest/**` |
| `gradle.md` | `*.gradle.kts`, `libs.versions.toml`, `build-logic/` |
| `proto.md` | `**/*.proto` |
| `security.md` | always |
