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
  - Name them `Fake<Interface>` and keep them in the same package as the interface, under the test source set.
  - Expose call counters (`setThemeModeCallCount`) and "last value" properties (`lastSetLanguageTag`) with `private set`.
  - Add switches for failure (`var xxxError: Throwable?`) and suspension (`var xxxGate: CompletableDeferred<Unit>?`) only when a test needs them.
  - `androidTest` cannot see `src/test`; duplicate the fake there if required.
- Unit tests must not reference `android.*` or `androidx.compose.*` APIs. Anything that needs them goes under `src/androidTest`.
- Instrumented DataStore tests use a unique temp file per test, delete it in `@After`, and `cancel()` + `join()` the scope before reopening the same file.

