---
paths:
  - "**/*.proto"
---

# Protocol Buffers rules (Proto DataStore)

Reference: `feature/settings/data/src/main/proto/app_settings_preferences.proto`.

- `syntax = "proto3"`, `option java_package = "com.example.upk.<feature>.data"`, `option java_multiple_files = true`.
- One message per DataStore file, named `<Feature>Preferences`, file name `<feature>_preferences.proto`, DataStore file `<feature>_preferences.pb`.
- Field numbers are permanent. Never renumber, reuse or change the type of an existing field. Deprecate with `reserved <n>;` and `reserved "<name>";` instead of deleting.
- Enums: first value is `<NAME>_UNSPECIFIED = 0`; every repository `when` maps `UNSPECIFIED` and `UNRECOGNIZED` to the domain default.
- Enum names carry a `Proto` suffix (`ThemeProto`) so they are never confused with domain enums (`ThemeMode`).
- snake_case field names (`theme_mode`); the generated Kotlin accessor is camelCase (`themeMode`).
- Adding a field is backward compatible; reading an old file yields the default (`""`, `0`, first enum value). Write a unit test that asserts the default mapping.
- Generated code is Java lite + Kotlin DSL (`upk.protobuf` plugin). Prefer the Kotlin DSL `copy { }` for updates.
