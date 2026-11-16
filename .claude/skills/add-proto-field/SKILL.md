---
name: add-proto-field
description: Add a field to a Proto DataStore schema and thread it through local data source, repository, domain model and tests. Use when asked to persist a new setting or preference, e.g. "/add-proto-field settings notifications_enabled bool".
---

# Add a proto field

Arguments: `<feature> <field_name> <type>` (e.g. `settings notifications_enabled bool`, or `settings font_scale FontScaleProto` for an enum).

## Steps

1. List `feature/<feature>/data/src/main/proto/` and open the preferences proto there (settings uses `app_settings_preferences.proto`, auth uses `auth_preferences.proto`). If there are several or none, stop and ask. Pick the next unused field number; never reuse a `reserved` number.
2. Add the field in snake_case. For an enum, declare it inside the message with `<NAME>_UNSPECIFIED = 0` first and a `Proto` suffix on the enum name.
3. Local data source (`local/datasource/<Feature>LocalDataSource` + Impl): add `observe<Field>(): Flow<T>` and `suspend fun set<Field>(value: T)` using `updateData { it.copy { <field> = value } }`.
4. Domain: add the model/enum under `domain/models` if the proto type is an enum, and the two methods to `<Feature>Repository`.
5. Repository Impl: map proto -> domain (`UNSPECIFIED`/`UNRECOGNIZED` -> default) and domain -> proto; apply `distinctUntilChanged()`; normalize input if it is a string.
6. Use cases: `Get<Field>UseCase` and `Update<Field>UseCase` via `/add-use-case` if ui will use them.
7. Update every fake implementing the touched interfaces (`Fake<Feature>LocalDataSource`, `Fake<Feature>Repository` in test and androidTest sets).
8. Tests to add:
   - `<Feature>LocalDataSourceImplTest`: default value, set/observe, setting the new field keeps other fields and vice versa.
   - `<Feature>RepositoryImplTest`: every enum branch, default mapping, round trip.
   - `AppSettingsPreferencesSerializerTest` (or the feature's serializer test): round trip with the new field set.
   - Use case tests.
9. Run `./gradlew :feature:<feature>:data:testDebugUnitTest :feature:<feature>:domain:testDebugUnitTest :feature:<feature>:ui:testDebugUnitTest`.
10. Finish with the file list and:
    ```bash
    git add feature/<feature>
    git commit -m "feat(<feature>): persist <field_name>"
    ```
