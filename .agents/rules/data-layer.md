---
trigger: glob
globs: "**/data/src/main/**"
description: "Data layer rules for repositories, Proto DataStore, Retrofit remote data sources, and Hilt DI."
---

# Data layer rules

Applies to `core/data` and every `feature/<name>/data` module.

## Package layout

```
di/                   Hilt module: @Binds impl -> interface, @Provides for DataStore/Retrofit services
repository/           <Feature>RepositoryImpl implementing the domain interface
local/datasource/     <Feature>LocalDataSource interface + Impl (DataStore access)
local/datastore/      proto Serializer objects
remote/               <Feature>RemoteDataSource extending core RemoteDataSource, Retrofit API interface, DTOs
```

## Repositories

- Implement the domain interface; constructor-inject data sources only, never `Context`.
- Map at the boundary: proto/DTO -> domain model inside the repository, in private `mapToDomainXxx` / `mapToProtoXxx` functions. Domain models never leak proto or DTO types upward.
- Every `when` over a proto enum handles `*_UNSPECIFIED` and `UNRECOGNIZED` explicitly, mapping them to the domain default.
- Apply `distinctUntilChanged()` on observed flows after mapping.
- Normalize input before persisting (trim, canonical case, e.g. `Locale.forLanguageTag(tag).toLanguageTag()`).

## Local data sources (Proto DataStore)

- One `DataStore<XxxPreferences>` per feature, provided in the feature's `di` module via `Context.dataStore(fileName, serializer)`.
- The data source interface exposes `observeXxx(): Flow<T>` and `suspend fun setXxx(value)`; nothing else.
- Writes use `updateData { it.copy { field = value } }` (Kotlin proto DSL) or `toBuilder()`, never replace the whole message, so unrelated fields survive.
- Serializer: `object XxxSerializer : Serializer<XxxPreferences>`, wrap `InvalidProtocolBufferException` in `CorruptionException`.

## Remote data sources

- Extend `com.example.upk.core.data.remote.RemoteDataSource` and call APIs through `execute<T> { api.xxx() }` (or `executeForResponse<T>` / `executeRaw` when HTTP headers, status code or raw body are needed). It already handles success/error envelopes (`ApiResponseBody`), empty bodies and `ApiException`; do not re-implement that logic.
- Retrofit API interfaces return `Response<ResponseBody>`; decoding happens in `execute` / `executeForResponse`.
- DTOs are `@Serializable` data classes under `remote/model/`, with `@SerialName` when the JSON key differs from the Kotlin name.
- Never catch `CancellationException`; if you must catch `Exception`, call `currentCoroutineContext().ensureActive()` first (see `RemoteDataSource`).

## DI

- `@Module @InstallIn(SingletonComponent::class) abstract class <Feature>DataModule` with `@Binds @Singleton` for each interface, and a `companion object` for `@Provides`.
- Bind data-layer singletons here, not in `app/di/AppModule`.

## Tests

- Every repository, data source and serializer gets unit tests with `FakeDataStore` / `Fake<Feature>LocalDataSource`.
- Anything that needs a real file or `Context` goes to `src/androidTest` (see `SettingsDataStoreInstrumentedTest`).

