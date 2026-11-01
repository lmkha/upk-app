package com.example.upk.settings.ui

import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.usecases.GetThemeModeUseCase
import com.example.upk.settings.domain.usecases.UpdateThemeModeUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeSettingsRepository
    private lateinit var analyticsTracker: FakeAnalyticsTracker

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        analyticsTracker = FakeAnalyticsTracker()
    }

    /** The ViewModel is created inside each test (not in @Before) because some tests configure the fake first. */
    private fun createViewModel() = SettingsViewModel(
        getThemeModeUseCase = GetThemeModeUseCase(repository),
        updateThemeModeUseCase = UpdateThemeModeUseCase(repository),
        analyticsTracker = analyticsTracker
    )

    // ---------- Initial state ----------

    @Test
    fun `default SettingsUiState is loading with SYSTEM theme`() {
        val state = SettingsUiState()

        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertTrue(state.isLoading)
        assertFalse(state.isUpdatingTheme)
        assertNull(state.errorMessage)
    }

    @Test
    fun `after init loading is finished and theme comes from repository`() = runTest {
        repository.themeMode.value = ThemeMode.DARK

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertNull(state.errorMessage)
    }

    @Test
    fun `uiState follows theme changes from repository`() = runTest {
        val viewModel = createViewModel()

        repository.themeMode.value = ThemeMode.LIGHT

        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `getThemeMode error stops loading and shows error message`() = runTest {
        repository.getThemeModeError = IllegalStateException("DataStore is corrupted")

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("DataStore is corrupted", state.errorMessage)
    }

    @Test
    fun `getThemeMode error without message uses default message`() = runTest {
        repository.getThemeModeError = IllegalStateException()

        val viewModel = createViewModel()

        assertEquals("Unable to load appearance settings.", viewModel.uiState.value.errorMessage)
    }

    // ---------- ThemeModeSelected ----------

    @Test
    fun `selecting a new theme calls repository and updates uiState`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))

        val state = viewModel.uiState.value
        assertEquals(1, repository.setThemeModeCallCount)
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertFalse(state.isUpdatingTheme)
        assertNull(state.errorMessage)
    }

    @Test
    fun `selecting the current theme does not call repository`() = runTest {
        val viewModel = createViewModel() // current theme is SYSTEM

        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.SYSTEM))

        assertEquals(0, repository.setThemeModeCallCount)
    }

    @Test
    fun `selecting a theme while loading is ignored`() = runTest {
        // Needs a "not loaded yet" state: use a fake whose getThemeMode never emits.
        val neverEmitRepository = object : FakeSettingsRepository() {
            override fun getThemeMode() = flow<ThemeMode> {
                CompletableDeferred<Unit>().await() // suspends forever
            }
        }
        val viewModel = SettingsViewModel(
            GetThemeModeUseCase(neverEmitRepository),
            UpdateThemeModeUseCase(neverEmitRepository),
            analyticsTracker
        )
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))

        assertEquals(0, neverEmitRepository.setThemeModeCallCount)
    }

    @Test
    fun `isUpdatingTheme is true during update and false afterwards`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.setThemeModeGate = gate
        val viewModel = createViewModel()

        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))
        assertTrue(viewModel.uiState.value.isUpdatingTheme)

        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.isUpdatingTheme)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `selecting another theme while updating is ignored`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.setThemeModeGate = gate
        val viewModel = createViewModel()

        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))
        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.LIGHT))
        gate.complete(Unit)

        assertEquals(1, repository.setThemeModeCallCount)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `update error shows message and keeps previous theme`() = runTest {
        repository.setThemeModeError = RuntimeException("Write failed")
        val viewModel = createViewModel()

        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))

        val state = viewModel.uiState.value
        assertEquals("Write failed", state.errorMessage)
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertFalse(state.isUpdatingTheme)
    }

    @Test
    fun `update error without message uses default message`() = runTest {
        repository.setThemeModeError = RuntimeException()
        val viewModel = createViewModel()

        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))

        assertEquals("Unable to update appearance settings.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `selecting a new theme clears previous error message`() = runTest {
        repository.setThemeModeError = RuntimeException("First error")
        val viewModel = createViewModel()
        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))
        assertEquals("First error", viewModel.uiState.value.errorMessage)

        repository.setThemeModeError = null
        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.LIGHT))

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)
    }

    // ---------- ErrorMessageDismissed ----------

    @Test
    fun `dismiss clears error message`() = runTest {
        repository.setThemeModeError = RuntimeException("Error")
        val viewModel = createViewModel()
        viewModel.onIntent(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))
        assertEquals("Error", viewModel.uiState.value.errorMessage)

        viewModel.onIntent(SettingsIntent.ErrorMessageDismissed)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `dismiss without an error leaves state unchanged`() = runTest {
        val viewModel = createViewModel()
        val before = viewModel.uiState.value

        viewModel.onIntent(SettingsIntent.ErrorMessageDismissed)

        assertEquals(before, viewModel.uiState.value)
    }
}
