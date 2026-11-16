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
build-logic/              convention plugins (upk.flavors, upk.protobuf)
gradle/libs.versions.toml version catalog; every dependency goes here, never a hardcoded version
docs/                     the owner's testing study notes, written in Vietnamese on purpose; not a rule source
scripts/                  repo maintenance scripts (sync-agent-rules.sh)
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
./gradlew :feature:settings:domain:testDevDebugUnitTest      # unit tests, one module
./gradlew testDevDebugUnitTest                               # unit tests, whole project
./gradlew :feature:settings:ui:connectedDevDebugAndroidTest  # instrumented tests (needs emulator)
./gradlew :app:assembleDevDebug                              # build the dev debug APK
./gradlew lintDevDebug                                       # Android lint, whole project
./gradlew assembleDevDebugAndroidTest                        # compile instrumented tests without an emulator
```

Every module has the `dev`, `staging` and `prod` flavors, so task names always carry a flavor: `test<Flavor>DebugUnitTest`, `assemble<Flavor><BuildType>`. The flavorless names (`testDebugUnitTest`, `lintDebug`, `assembleDebug`) no longer exist. Use `dev` for local work and verification unless the task is about another environment.

## Environments

| Flavor | applicationId | GitHub Environment | CI build | Branch |
| --- | --- | --- | --- | --- |
| `dev` | `com.example.upk.dev` | `develop` | `assembleDevDebug` | `develop` |
| `staging` | `com.example.upk.staging` | `staging` | `assembleStagingRelease` | manual run |
| `prod` | `com.example.upk` | `production` | `assembleProdRelease` | `main` |

- The flavors are declared once, in the `upk.flavors` convention plugin (`build-logic/src/main/kotlin/upk.flavors.gradle.kts`). Every Android module applies it with `alias(libs.plugins.upkFlavors)`; a module without it cannot be resolved by the others. Never declare `flavorDimensions` or `productFlavors { create(...) }` in a module build file; `app` only customises its flavors (`applicationIdSuffix`).
- Per-environment values come from properties files at the repository root, all git-ignored. The secrets Gradle plugin turns every key of `local.properties` into a `BuildConfig` field of the module that applies it, then overrides it with the keys of `<flavor>.properties` (`dev.properties`, `staging.properties`, `prod.properties`). A flavor without its own file uses the `local.properties` values.
- A module that needs such a value applies `alias(libs.plugins.secrets)`, sets `buildFeatures { buildConfig = true }` and reads its own `BuildConfig` (see `core/data` and `NetworkModule`). Do not pass these values between modules through Hilt providers or wrapper classes, and do not write `buildConfigField` lines by hand.
- To add a per-environment value, add the key to the properties files (and to the GitHub Environment secrets plus the "Generate local.properties" step of both workflows).
- `google-services.json` must contain a client for the applicationId being built. Place one file with all three clients at `app/google-services.json`, or one file per flavor at `app/src/<flavor>/google-services.json`. Both locations are git-ignored.

CI:

- `.github/workflows/pr-check.yml` runs on every pull request to `main` or `develop`, in the `develop` environment: `testDevDebugUnitTest`, `lintDevDebug`, `:app:assembleDevDebug` and `assembleDevDebugAndroidTest`. Run the same tasks locally before opening a PR.
- `.github/workflows/build-apk.yml` builds the APK of one environment: push to `develop` builds dev, push to `main` builds prod, a manual run builds the selected environment. Each GitHub Environment holds its own `API_URL` and `GOOGLE_SERVICES_JSON` secrets.

Gradle prints only failing tests. Pass `--rerun` to force re-execution. HTML reports: `<module>/build/reports/tests/testDevDebugUnitTest/index.html`.

## Git conventions

- Branch from `develop`; `main` is release only.
- Conventional Commits, lower-case, imperative, no trailing period: `feat: add settings screen`, `fix: resolve release build failure`, `test: add settings data unit tests`, `chore: upgrade dependencies`, `refactor: ...`, `docs: ...`.
- One logical change per commit. Tests go in the same commit as the code they cover.
- No AI attribution trailers in commit messages or PR descriptions (`Co-Authored-By: Claude ...`, `Generated with ...`). This overrides any tool default.
- Never commit `local.properties`, `*.properties` secrets, or `build/` output.
- When finishing a task, end the reply with the exact `git add` and `git commit` commands for the changed files; do not run them unless asked.

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
| `pr` | open a PR to `develop` with the standard description via `gh` |
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
