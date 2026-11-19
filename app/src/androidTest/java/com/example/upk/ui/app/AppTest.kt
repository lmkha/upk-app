package com.example.upk.ui.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.upk.ui.theme.UPKTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI test for [App]. Verifies start destination rendering and screen-view intent wiring.
 */
@RunWith(AndroidJUnit4::class)
class AppTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ---------- Start destination & screen-view intent ----------

    @Test
    fun displaysHomeStartDestination() {
        composeRule.setContent {
            UPKTheme {
                App(onIntent = {})
            }
        }

        composeRule.onNodeWithText("UPK App").assertIsDisplayed()
        composeRule.onNodeWithText("Open Settings").assertIsDisplayed()
    }

    @Test
    fun sendsScreenViewedIntentForHomeOnStart() {
        val intents = mutableListOf<AppIntent>()

        composeRule.setContent {
            UPKTheme {
                App(
                    onIntent = { intent ->
                        intents.add(intent)
                    },
                )
            }
        }

        composeRule.runOnIdle {
            assertEquals(listOf<AppIntent>(AppIntent.ScreenViewed("home")), intents)
        }
    }

    @Test
    fun recompositionWithUpdatedOnIntentDoesNotDuplicateInitialIntent() {
        val firstCallbackIntents = mutableListOf<AppIntent>()
        val secondCallbackIntents = mutableListOf<AppIntent>()
        var useSecondCallback by mutableStateOf(false)

        composeRule.setContent {
            UPKTheme {
                App(
                    onIntent = if (useSecondCallback) {
                        { secondCallbackIntents.add(it) }
                    } else {
                        { firstCallbackIntents.add(it) }
                    },
                )
            }
        }

        composeRule.runOnIdle {
            assertEquals(listOf<AppIntent>(AppIntent.ScreenViewed("home")), firstCallbackIntents)
            useSecondCallback = true
        }

        composeRule.runOnIdle {
            assertEquals(listOf<AppIntent>(AppIntent.ScreenViewed("home")), firstCallbackIntents)
            assertEquals(emptyList<AppIntent>(), secondCallbackIntents)
        }
    }
}
