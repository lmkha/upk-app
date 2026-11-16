---
paths:
  - "**/domain/src/main/**"
---

# Domain layer rules

Applies to `core/domain` and every `feature/<name>/domain` module.

- Pure Kotlin only. No `android.*` or `androidx.*`, no Compose, no Retrofit, no DataStore, no proto types. `javax.inject` and `kotlinx.coroutines` are the only framework imports allowed.
- Package layout inside a feature domain module:
  - `models/`      plain data classes, enums, sealed classes used by ui and data
  - `repository/`  interfaces only
  - `usecases/`    one class per use case
- Repository interfaces:
  - Observing data returns `Flow<T>`; one-shot reads and writes are `suspend fun`.
  - Expose domain models, never proto, DTO or database entities.
  - Failures are signalled by exceptions from `suspend` functions or by `Result<T>` when the caller is expected to branch on it. Do not mix both in one interface.
- Use cases:
  - Name `<Verb><Noun>UseCase`, e.g. `GetThemeModeUseCase`, `UpdateThemeModeUseCase`, `CreateBindingUseCase`.
  - `class X @Inject constructor(private val repository: Y)` with a single `operator fun invoke(...)`.
  - A use case does one thing. Combining several repositories is allowed; branching on UI concerns is not.
  - Keep them thin. If a use case only forwards a call, that is fine; it still gives ui a stable entry point.
- Models:
  - `data class` with immutable `val` properties. Provide defaults only when the default is meaningful in the domain.
  - Enums with a safe default (e.g. `ThemeMode.SYSTEM`) so data mapping always has a fallback.
- Every new use case ships with a unit test using the module's `Fake<Feature>Repository` (see `/add-use-case`).
