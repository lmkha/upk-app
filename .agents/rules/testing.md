---
trigger: glob
globs: "**/src/test/**, **/src/androidTest/**"
description: "Testing rules and conventions for unit tests, Compose UI tests, and hand-written fakes."
---

# Rules for test sources

These rules load only when working on files under `src/test` or `src/androidTest`.

- Structure every test as Arrange / Act / Assert, separated by blank lines. Comments marking the sections are not needed.
- `assertEquals(expected, actual)`: expected value first.
- One behaviour per test. Several asserts are fine when they verify the same behaviour.
- Create the object under test inside each test (or in `@Before`) so tests never share state.
- Group related tests with a `// ---------- Section ----------` comment line.
- Fakes:
  - Name them `Fake<Interface>` and keep them in the test source set of the module whose tests use them, in the same package as those tests.
  - Test source sets are not shared between modules: a fake in `domain/src/test` is invisible to `ui/src/test`. Copy the fake into each module that needs it (see the `FakeSettingsRepository` copies) and update every copy when the interface changes.
  - Expose call counters (`setThemeModeCallCount`) and "last value" properties (`lastSetLanguageTag`) with `private set`.
  - Add switches for failure (`var xxxError: Throwable?`) and suspension (`var xxxGate: CompletableDeferred<Unit>?`) only when a test needs them.
  - `androidTest` cannot see `src/test`; duplicate the fake there if required.
- Coroutines: `runBlocking` for a plain suspend call or a single `first()` on a flow; `runTest` + `UnconfinedTestDispatcher` when the code under test launches coroutines or takes a scope/dispatcher; ViewModel tests add `MainDispatcherRule`.
- Unit tests must not reference `android.*` or `androidx.compose.*` APIs (JVM-only AndroidX artifacts such as `androidx.datastore.core` are fine). Anything that needs them goes under `src/androidTest`.
- Instrumented DataStore tests use a unique temp file per test, delete it in `@After`, and `cancel()` + `join()` the scope before reopening the same file.
