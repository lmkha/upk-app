---
name: write-tests
description: Write unit tests (and a Compose test for screens) for an existing class following the project's testing conventions. Use when asked to "write tests for X", "add coverage for X" or "/write-tests feature/payment/.../PaymentRepositoryImpl.kt".
---

# Write tests for an existing class

Argument: a file path or class name.

## Steps

1. Read the class and every interface it depends on. Decide the test type:
   - use case / repository / data source / serializer / ViewModel / plain Kotlin -> unit test under `src/test`,
   - `@Composable` screen -> Compose test under `src/androidTest`,
   - Hilt module, interface, plain data class, Retrofit interface -> no test; say so.
2. Reuse existing fakes from the module's `src/test`. If a dependency has no fake, create `Fake<Interface>` next to the test (in-memory state, call counters with `private set`, optional error/gate switches).
3. List the behaviours before writing code: happy path, each branch of every `when`/`if`, default and empty inputs, error path, default error message, "does nothing when" guards, idempotence. Each becomes one test named as a backtick sentence.
4. Write the test class:
   - `@Before` builds fresh fakes; the subject is built in `@Before` or per test when configuration differs,
   - `runBlocking` for simple suspend calls, `runTest` + `UnconfinedTestDispatcher` for scopes and ViewModels (`MainDispatcherRule`),
   - `first()` / `toList()` for flows; `object : X by fake { override ... }` to override one method,
   - group with `// ---------- Section ----------` comments.
5. Run `./gradlew :<module>:testDebugUnitTest --tests "*<ClassName>Test"` and paste the pass/fail summary. Fix failing tests only if the failure is in the test; if it reveals a bug in production code, report it and ask before changing production code.
6. Finish with:
   ```bash
   git add <test files>
   git commit -m "test(<feature>): cover <ClassName>"
   ```
