package com.example.upk.settings.ui

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.domain.usecases.GetThemeModeUseCase
import com.example.upk.settings.domain.usecases.UpdateThemeModeUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI test for [SettingsScreen]. Runs on a device or emulator.
 *
 * The screen is rendered with a real [SettingsViewModel] backed by [FakeSettingsRepository],
 * so the test covers the full UI -> ViewModel -> use case path without Hilt or DataStore.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var repository: FakeSettingsRepository

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
    }

    private fun createViewModel(repo: FakeSettingsRepository = repository) = SettingsViewModel(
        getThemeModeUseCase = GetThemeModeUseCase(repo),
        updateThemeModeUseCase = UpdateThemeModeUseCase(repo),
    )

    private fun setContent(viewModel: SettingsViewModel = createViewModel()) {
        composeRule.setContent {
            SettingsScreen(viewModel = viewModel)
        }
    }

    private val indeterminateProgress = SemanticsMatcher.expectValue(
        SemanticsProperties.ProgressBarRangeInfo,
        ProgressBarRangeInfo.Indeterminate,
    )

    // ---------- Rendering ----------

    @Test
    fun showsTitleAndAllThemeOptionsWhenLoaded() {
        setContent()

        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        composeRule.onNodeWithText("System").assertIsDisplayed()
    }

    @Test
    fun showsProgressIndicatorAndHidesOptionsWhileLoading() {
        val neverEmitRepository = object : FakeSettingsRepository() {
            override fun getThemeMode() = flow<ThemeMode> {
                CompletableDeferred<Unit>().await()
            }
        }
        setContent(createViewModel(neverEmitRepository))

        composeRule.onNode(indeterminateProgress).assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertDoesNotExist()
    }

    @Test
    fun selectedOptionReflectsStoredTheme() {
        repository.themeMode.value = ThemeMode.DARK
        setContent()

        composeRule.onNodeWithText("Dark").assertIsSelected()
        composeRule.onNodeWithText("Light").assertIsNotSelected()
        composeRule.onNodeWithText("System").assertIsNotSelected()
    }

    // ---------- Interaction ----------

    @Test
    fun clickingAnOptionUpdatesRepositoryAndSelection() {
        setContent()

        composeRule.onNodeWithText("Dark").performClick()

        composeRule.onNodeWithText("Dark").assertIsSelected()
        composeRule.onNodeWithText("System").assertIsNotSelected()
        composeRule.runOnIdle {
            assertEquals(1, repository.setThemeModeCallCount)
        }
    }

    @Test
    fun clickingTheCurrentOptionDoesNotCallRepository() {
        setContent() // current theme is SYSTEM

        composeRule.onNodeWithText("System").performClick()

        composeRule.runOnIdle {
            assertEquals(0, repository.setThemeModeCallCount)
        }
    }

    @Test
    fun optionsAreDisabledWhileUpdateIsInProgress() {
        val gate = CompletableDeferred<Unit>()
        repository.setThemeModeGate = gate
        setContent()

        composeRule.onNodeWithText("Dark").performClick()

        composeRule.onNodeWithText("Dark").assertIsNotEnabled()
        composeRule.onNodeWithText("Light").assertIsNotEnabled()

        gate.complete(Unit)

        composeRule.onNodeWithText("Dark").assertIsEnabled()
        composeRule.onNodeWithText("Dark").assertIsSelected()
    }

    // ---------- Error handling ----------

    @Test
    fun showsErrorMessageAndDismissButtonWhenUpdateFails() {
        repository.setThemeModeError = RuntimeException("Write failed")
        setContent()

        composeRule.onNodeWithText("Dark").performClick()

        composeRule.onNodeWithText("Write failed").assertIsDisplayed()
        composeRule.onNodeWithText("Dismiss").assertIsDisplayed()
        composeRule.onNodeWithText("System").assertIsSelected()
    }

    @Test
    fun clickingDismissHidesErrorMessage() {
        repository.setThemeModeError = RuntimeException("Write failed")
        setContent()
        composeRule.onNodeWithText("Dark").performClick()
        composeRule.onNodeWithText("Write failed").assertIsDisplayed()

        composeRule.onNodeWithText("Dismiss").performClick()

        composeRule.onNodeWithText("Write failed").assertDoesNotExist()
        composeRule.onNodeWithText("Dismiss").assertDoesNotExist()
    }

    @Test
    fun showsErrorMessageWhenLoadingFails() {
        repository.getThemeModeError = IllegalStateException("DataStore is corrupted")
        setContent()

        composeRule.onNodeWithText("DataStore is corrupted").assertIsDisplayed()
        composeRule.onNode(indeterminateProgress).assertDoesNotExist()
    }
}
