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
 * Compose UI test for [App]. Verifies start destination rendering and screen-view callback wiring.
 */
@RunWith(AndroidJUnit4::class)
class AppTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ---------- Start destination & analytics callback ----------

    @Test
    fun displaysHomeStartDestination() {
        composeRule.setContent {
            UPKTheme {
                App(onScreenViewed = {})
            }
        }

        composeRule.onNodeWithText("UPK App").assertIsDisplayed()
        composeRule.onNodeWithText("Open Settings").assertIsDisplayed()
    }

    @Test
    fun invokesOnScreenViewedWithHomeOnStart() {
        val viewedScreens = mutableListOf<String>()

        composeRule.setContent {
            UPKTheme {
                App(
                    onScreenViewed = { screenName ->
                        viewedScreens.add(screenName)
                    },
                )
            }
        }

        composeRule.runOnIdle {
            assertEquals(listOf("home"), viewedScreens)
        }
    }

    @Test
    fun recompositionWithUpdatedOnScreenViewedDoesNotDuplicateInitialEvent() {
        val firstCallbackEvents = mutableListOf<String>()
        val secondCallbackEvents = mutableListOf<String>()
        var useSecondCallback by mutableStateOf(false)

        composeRule.setContent {
            UPKTheme {
                App(
                    onScreenViewed = if (useSecondCallback) {
                        { secondCallbackEvents.add(it) }
                    } else {
                        { firstCallbackEvents.add(it) }
                    },
                )
            }
        }

        composeRule.runOnIdle {
            assertEquals(listOf("home"), firstCallbackEvents)
            useSecondCallback = true
        }

        composeRule.runOnIdle {
            assertEquals(listOf("home"), firstCallbackEvents)
            assertEquals(emptyList<String>(), secondCallbackEvents)
        }
    }
}
