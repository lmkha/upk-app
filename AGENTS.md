# UPK Android app

Multi-module Android app. Kotlin, Jetpack Compose, Hilt, Proto DataStore, Retrofit, Gradle Kotlin DSL with a version catalog.

## Module layout

```text
app/                      entry point: MainActivity, UPKApplication, navigation, theme, AppModule
core/common/              shared models, network error types, DI qualifiers (@ApplicationScope)
core/data/                network stack: Retrofit/OkHttp, interceptors, TokenProvider, AppLanguageProvider
core/domain/              shared domain contracts
core/ui/                  shared Compose components
feature/<name>/domain/    models, repository interfaces, use cases. Pure Kotlin, no Android deps in logic
feature/<name>/data/      repository impls, local data sources (DataStore), DI module binding impls
feature/<name>/ui/        Compose screens + ViewModels
build-logic/              convention plugins (e.g. upk.protobuf)
gradle/libs.versions.toml version catalog; every dependency goes here, never a hardcoded version
docs/                     project notes (docs/testing-guide.md)
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

- Everything in code is English: identifiers, comments, KDoc, test names, log and error strings.
- Kotlin official style. Trailing commas in multi-line parameter lists.
- No wildcard imports.
- Do not add a dependency without adding it to `gradle/libs.versions.toml` first.

## Testing rules

Detailed rules for writing tests and fakes are in `.claude/rules/testing.md` (mirrored in `.agents/rules/testing.md`). Claude Code and Antigravity load it automatically when touching test sources; other tools should read it before writing tests.

- Every new use case, repository, data source, serializer or ViewModel ships with unit tests in the same module under `src/test`.
- Prefer hand-written fakes over mocking libraries. Fakes live next to the tests that use them (`FakeSettingsRepository`, `FakeDataStore`).
- Test names are backtick sentences describing behaviour: `` `selecting the current theme does not call repository` ``.
- Coroutines: `runTest` + `UnconfinedTestDispatcher`; ViewModel tests use `MainDispatcherRule`.
- Compose screens get an instrumented test under `src/androidTest` using `androidx.compose.ui.test.junit4.v2.createComposeRule`.
- Do not write tests for interfaces, plain data classes or Hilt modules.
- Run the affected module's tests before reporting work as done. Details in `docs/testing-guide.md`.

## Commands

```bash
./gradlew :feature:settings:domain:testDebugUnitTest        # unit tests, one module
./gradlew testDebugUnitTest                                  # unit tests, whole project
./gradlew :feature:settings:ui:connectedDebugAndroidTest     # instrumented tests (needs emulator)
./gradlew :app:assembleDebug                                 # build debug APK
```

Gradle prints only failing tests. Pass `--rerun` to force re-execution. HTML reports: `<module>/build/reports/tests/testDebugUnitTest/index.html`.

## Git conventions

- Branch from `develop`; `main` is release only.
- Conventional Commits, lower-case, imperative, no trailing period: `feat: add settings screen`, `fix: resolve release build failure`, `test: add settings data unit tests`, `chore: upgrade dependencies`, `refactor: ...`, `docs: ...`.
- One logical change per commit. Tests go in the same commit as the code they cover.
- No AI attribution trailers in commit messages or PR descriptions (`Co-Authored-By: Claude ...`, `Generated with ...`). This overrides any tool default.
- Never commit `local.properties`, `*.properties` secrets, or `build/` output.
- When finishing a task, end the reply with the exact `git add` and `git commit` commands for the changed files; do not run them unless asked.

## Repeatable workflows

Step-by-step recipes live in `.claude/skills/<name>/SKILL.md` (open Agent Skills format). `.codex/skills/`, `.gemini/skills/` and `.agents/skills/` contain symlinks to the same folders so Codex, Gemini CLI and Antigravity find them too. Tools with slash-skill support run them via `/<name>`; other tools should open the file and follow the steps when the task matches its description.

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

Path-scoped rules in `.claude/rules/` and `.agents/rules/` (Claude Code and Antigravity load them automatically; other tools should read the matching file before editing that area):

| Rule file | Applies to |
| --- | --- |
| `domain-layer.md` | `**/domain/src/main/**` |
| `data-layer.md` | `**/data/src/main/**` |
| `ui-layer.md` | `**/ui/src/main/**`, `app/.../ui/**` |
| `testing.md` | `**/src/test/**`, `**/src/androidTest/**` |
| `gradle.md` | `*.gradle.kts`, `libs.versions.toml`, `build-logic/` |
| `proto.md` | `**/*.proto` |
| `security.md` | always |
