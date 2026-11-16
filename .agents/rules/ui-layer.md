---
trigger: glob
globs: "**/ui/src/main/**, app/src/main/java/com/example/upk/ui/**"
description: "UI layer rules for Jetpack Compose screens, ViewModels, UiState/Intent, and navigation."
---

# UI layer rules (Compose + ViewModel)

Applies to `core/ui`, every `feature/<name>/ui` module and `app/.../ui`.

## ViewModel

Reference implementation: `feature/settings/ui/.../SettingsViewModel.kt`.

- One `data class <Screen>UiState` with defaults that represent the initial state (`isLoading = true` when data must be loaded first).
- One `sealed interface <Screen>Intent`; the ViewModel exposes a single `fun onIntent(intent: <Screen>Intent)` with an exhaustive `when`.
- State lives in `private val _uiState = MutableStateFlow(UiState())` exposed as `val uiState = _uiState.asStateFlow()`. Update with `_uiState.update { it.copy(...) }`.
- Observe domain flows in `init` with `viewModelScope.launch { useCase().distinctUntilChanged().catch { ... }.collect { ... } }`.
- One-shot work: set a `isXxxInProgress` flag, `try` the use case, `catch (e: CancellationException) { throw e }`, `catch (e: Exception)` -> error message, `finally` clear the flag.
- Guard against duplicate work (`if (state.isLoading || state.isUpdating || sameValue) return@launch`).
- Error messages: `exception.message ?: "<sensible default>"`, cleared by an explicit `ErrorMessageDismissed` intent and whenever a new action starts.
- No `android.*` imports in ViewModels except `androidx.lifecycle`. No `Context`, no resources, no navigation controller.
- Annotate `@HiltViewModel` with `@Inject constructor(useCases...)`. Inject use cases, never repositories.

## Composables

- Screen entry point: `@Composable fun <Name>Screen(modifier: Modifier = Modifier, viewModel: <Name>ViewModel = hiltViewModel(), onNavigateToX: () -> Unit = {})`. Keep `viewModel` as a parameter so tests can inject one.
- Collect state with `collectAsStateWithLifecycle()`.
- Split into a stateless content composable when the screen grows: `<Name>Screen` collects state and wires callbacks, `<Name>Content(state, onIntent)` renders. Previews target the content composable.
- Navigation callbacks are plain lambdas passed in; a feature module never references `NavController` or `Destination`.
- `Modifier` is the first optional parameter and is applied once to the root element.
- Material 3 only (`androidx.compose.material3`). Colors and typography come from `MaterialTheme`; no hardcoded `Color(...)` or `sp` values outside `app/ui/theme`.
- User-visible strings: keep the current inline-literal approach for now (existing screens do this); when a string is reused or the feature is localized, move it to `strings.xml` and use `stringResource`.
- Loading state shows `CircularProgressIndicator`; disable interactive controls while an update is in progress instead of hiding them.
- Selection groups use `Modifier.selectableGroup()` + `Modifier.selectable(role = Role.RadioButton)` on the row, with `RadioButton(onClick = null)` inside, so the whole row is clickable and accessible.

## Navigation (app module)

- Destinations are `@Serializable` objects/classes in `app/.../ui/navigation/Destination.kt`; nested graphs are nested objects (`Destination.MainGraph.Home`).
- Wire screens in `app/.../ui/app/App.kt` with `composable<Destination.X> { XScreen(onNavigateToY = { navController.navigate(Destination.Y) }) }`.

## Tests

- ViewModel: unit test under `src/test` with `MainDispatcherRule` and the feature's `Fake<Feature>Repository` plus real use cases.
- Screen: Compose test under `src/androidTest` with `androidx.compose.ui.test.junit4.v2.createComposeRule`, rendering the screen with a hand-built ViewModel.
