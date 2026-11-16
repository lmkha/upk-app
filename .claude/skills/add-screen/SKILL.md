---
name: add-screen
description: Add a Compose screen with its ViewModel, UiState, Intent, unit test, Compose test and navigation entry, following SettingsScreen/SettingsViewModel. Use when asked for a new screen, e.g. "/add-screen payment PaymentHistory".
---

# Add a screen

Arguments: `<feature> <ScreenName>` (e.g. `payment PaymentHistory`). The feature's `ui` and `domain` modules must already exist (otherwise run `/add-feature` first).

## Steps

1. Read `feature/<feature>/domain/.../usecases/` to know which use cases the screen can call. Ask if the screen needs data no use case provides.
2. Create `feature/<feature>/ui/src/main/java/com/example/upk/<feature>/ui/<ScreenName>ViewModel.kt` following `SettingsViewModel`:
   - `data class <ScreenName>UiState(...)` with `isLoading = true` default when data is loaded on start,
   - `sealed interface <ScreenName>Intent`,
   - `@HiltViewModel class <ScreenName>ViewModel @Inject constructor(use cases) : ViewModel()` with `uiState`, `onIntent`, `init { observe...() }`, and the try/catch/finally pattern for one-shot actions (rethrow `CancellationException`).
3. Create `<ScreenName>Screen.kt`:
   - `fun <ScreenName>Screen(modifier: Modifier = Modifier, viewModel: <ScreenName>ViewModel = hiltViewModel(), onNavigateBack: () -> Unit = {})`,
   - collect with `collectAsStateWithLifecycle()`, render loading / content / error (with a dismiss action) states using Material 3.
4. Navigation (app module): add `Destination.<ScreenName>` in `app/.../ui/navigation/Destination.kt`, a `composable<Destination.<ScreenName>>` block in `app/.../ui/app/App.kt`, and the navigation lambda on the screen that opens it if the user named one.
5. Tests:
   - `ui/src/test/.../<ScreenName>ViewModelTest.kt`: initial state, data loaded, each intent, error path and default error message. Use `MainDispatcherRule` and the feature's fake repository; create it under `ui/src/test` if missing.
   - `ui/src/androidTest/.../<ScreenName>ScreenTest.kt`: renders loaded state, loading indicator, one interaction, error + dismiss. Use `androidx.compose.ui.test.junit4.v2.createComposeRule`. Add `androidTestImplementation(libs.androidx.ui.test.junit4)` and `debugImplementation(libs.androidx.ui.test.manifest)` to the ui module if absent.
6. Run `./gradlew :feature:<feature>:ui:testDebugUnitTest :feature:<feature>:ui:assembleDebugAndroidTest :app:assembleDevDebug`.
7. Finish with the file list and:
   ```bash
   git add feature/<feature>/ui app/src/main/java/com/example/upk/ui
   git commit -m "feat(<feature>): add <ScreenName> screen"
   ```
