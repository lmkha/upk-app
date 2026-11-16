---
name: add-repository
description: Add a repository (domain interface, data implementation, Hilt binding, fake and unit tests) or extend an existing one with a new method. Use for "/add-repository payment PaymentHistory" or "add getXxx to SettingsRepository".
---

# Add or extend a repository

Arguments: `<feature> <Name>` for a new repository, or `<feature> <ExistingRepository> <methodSignature>` to extend one.

## New repository

1. Domain: `feature/<feature>/domain/.../repository/<Name>Repository.kt` interface. Observing methods return `Flow<T>`, writes are `suspend fun`. Only domain models in signatures.
2. Data: `feature/<feature>/data/.../repository/<Name>RepositoryImpl.kt` with `@Inject constructor(dataSources...)`. Mapping proto/DTO -> domain happens here in private `mapToDomainXxx` functions; `when` over proto enums handles `UNSPECIFIED` and `UNRECOGNIZED`.
3. If the repository needs a new data source, add `local/datasource/<Name>LocalDataSource` (+Impl) or `remote/<Name>RemoteDataSource` extending `core.data.remote.RemoteDataSource` and using `execute`.
4. DI: in `feature/<feature>/data/.../di/<Feature>DataModule.kt` add `@Binds @Singleton abstract fun bind<Name>Repository(impl: <Name>RepositoryImpl): <Name>Repository` (and bindings for new data sources). Create the module if it does not exist.
5. Tests:
   - `domain/src/test/.../Fake<Name>Repository.kt` (in-memory, call counters, `private set`).
   - `data/src/test/.../repository/<Name>RepositoryImplTest.kt` with a fake data source: every mapping branch, `distinctUntilChanged`, input normalization, set-then-get.
6. Run `./gradlew :feature:<feature>:data:testDebugUnitTest :feature:<feature>:domain:testDebugUnitTest`.

## Extending an existing repository

1. Add the method to the domain interface.
2. Implement it in the Impl (plus data source method if needed).
3. Update every fake that implements the interface (`grep -rl "<Name>Repository" --include=*.kt */src/test */src/androidTest`); the build will fail otherwise.
4. Add tests for the new method in the Impl test, and a use case via `/add-use-case` if ui will call it.
5. Run the data and domain module tests, and the ui module tests if fakes there changed.

## Finish

List the files and:
```bash
git add <files>
git commit -m "feat(<feature>): add <Name>Repository"      # or "feat(<feature>): add <method> to <Name>Repository"
```
