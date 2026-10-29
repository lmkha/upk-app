---
name: fix-bug
description: Fix a reported bug test-first - reproduce it with a failing unit test, fix the code, prove the test passes, keep the test as a regression guard. Use when the user describes wrong behaviour or pastes a stack trace.
---

# Fix a bug

Input: a description, a stack trace or a failing scenario.

## Steps

1. Locate the code path. Read the class, its callers and its existing tests. State in one sentence what the expected and actual behaviours are; ask if the expected behaviour is unclear.
2. Write a failing test first in the existing test class for that unit (create one via `/write-tests` conventions if absent). Name it after the behaviour, not the bug id: `` `setLanguageTag stores empty string for blank input` ``, not `` `fixes bug 123` ``.
3. Run only that test and confirm it fails for the right reason (assertion, not compile error):
   `./gradlew :<module>:testDebugUnitTest --tests "*<TestClass>"`.
4. Make the smallest production change that makes it pass. Do not refactor unrelated code in the same change.
5. Run the whole module's tests, plus the ui module's tests if a domain/data interface changed.
6. If the bug was in a ViewModel or screen, also check whether `SettingsScreenTest`-style Compose coverage should get a case; add it when the bug was visible in UI.
7. Finish with: root cause in one or two sentences, the file list, and:
   ```bash
   git add <files>
   git commit -m "fix(<feature>): <what was wrong, imperative>"
   ```
