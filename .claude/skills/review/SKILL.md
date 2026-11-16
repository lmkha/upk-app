---
name: review
description: Review a diff, branch or PR against this project's architecture, Compose, coroutine, data-layer and testing rules and report findings by severity. Use for "/review", "review my changes", "review PR 12".
---

# Project code review

Target: working tree by default, or a branch / PR number if given (`gh pr diff <n>`).

## Procedure

1. Get the diff and the list of touched modules. Read each changed file fully, plus the interface it implements and the tests that cover it.
2. Check, in this order, and collect findings:

   **Correctness**
   - `when` over proto enums or sealed types is exhaustive and handles `UNSPECIFIED` / `UNRECOGNIZED`.
   - `CancellationException` is rethrown, never swallowed; `ensureActive()` after broad catches.
   - State updates use `_uiState.update { it.copy(...) }`; no lost updates from `_uiState.value = ...` races.
   - Flows observed in ViewModels have `catch`; loading flags are cleared on every path (`finally`).
   - Input normalized before persistence; defaults for missing/blank data.

   **Architecture**
   - Dependency direction `ui -> domain <- data`; no proto/DTO types outside `data`; no `Context` in ViewModels or repositories.
   - New bindings live in the feature's `*DataModule`, not `AppModule`.
   - Screens receive `viewModel` and navigation lambdas as parameters; no `NavController` inside feature modules.

   **Build**
   - New dependencies go through `libs.versions.toml`; no duplicate `include` lines; namespaces match paths.

   **Tests**
   - Every new use case / repository / data source / ViewModel has unit tests; new screens have a Compose test.
   - Fakes updated for interface changes; tests assert behaviour, not implementation details; names are backtick sentences in English.
   - Tests actually ran: ask for or run `./gradlew :<module>:testDebugUnitTest`.

   **Conventions**
   - English identifiers/comments/strings; no wildcard imports; Material 3 only; secrets not committed.

3. Report findings grouped by severity: **Blocker** (bug, data loss, security), **Should fix** (rule violation, missing tests), **Nit** (style). For each: `file:line`, what is wrong, and the concrete fix. Skip praise. If nothing is found in a category, say so in one line.
4. End with a one-line verdict: ready to merge / needs changes.
