---
name: add-use-case
description: Add a new use case to a feature's domain module together with its unit test. Use when asked to create a use case, e.g. "/add-use-case settings GetLanguageTag".
---

# Add a use case

Arguments: `<feature> <UseCaseName>` (without the `UseCase` suffix). Example: `settings GetLanguageTag`.

## Steps

1. Read `feature/<feature>/domain/src/main/java/com/example/upk/<feature>/domain/repository/` to find the repository interface and the method the use case will wrap. If the method does not exist, stop and ask.
2. Create `feature/<feature>/domain/src/main/java/com/example/upk/<feature>/domain/usecases/<UseCaseName>UseCase.kt`:

```kotlin
package com.example.upk.<feature>.domain.usecases

import com.example.upk.<feature>.domain.repository.<Feature>Repository
import javax.inject.Inject

class <UseCaseName>UseCase @Inject constructor(
    private val repository: <Feature>Repository,
) {
    operator fun invoke(/* params */) = repository.<method>(/* params */)
}
```

   Mark `invoke` as `suspend` when the repository method is `suspend`.

3. Create the test `feature/<feature>/domain/src/test/java/com/example/upk/<feature>/domain/usecases/<UseCaseName>UseCaseTest.kt`. Reuse the existing `Fake<Feature>Repository` from the module's test sources; add a call counter to it if missing. Cover at least:
   - the default/initial value,
   - the use case forwards its argument to the repository exactly once,
   - the latest repository value is returned.
4. Run `./gradlew :feature:<feature>:domain:testDevDebugUnitTest` and show the result.
5. Finish the reply with:

```bash
git add feature/<feature>/domain/src/main/java/com/example/upk/<feature>/domain/usecases/<UseCaseName>UseCase.kt \
        feature/<feature>/domain/src/test/java/com/example/upk/<feature>/domain/usecases/<UseCaseName>UseCaseTest.kt
git commit -m "feat(<feature>): add <UseCaseName>UseCase"
```
