package com.example.upk

import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI test for [MainActivity] covering navigation, per-app night mode sync,
 * and edge-to-edge system bar appearance updates without recreating the Activity.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForSettingsOptionsLoaded() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Light").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithText("Dark").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithText("System").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // ---------- Navigation ----------

    @Test
    fun launchesHomeAndNavigatesToSettings() {
        composeRule.onNodeWithText("UPK App").assertIsDisplayed()

        composeRule.onNodeWithText("Open Settings").performClick()
        waitForSettingsOptionsLoaded()

        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        composeRule.onNodeWithText("System").assertIsDisplayed()
    }

    // ---------- Night mode & system bars sync ----------

    @Test
    fun selectingLightAndDarkSyncsSystemBarsAndNightModeWithoutRecreatingActivity() {
        val initialActivity = composeRule.activity

        composeRule.onNodeWithText("Open Settings").performClick()
        waitForSettingsOptionsLoaded()

        composeRule.onNodeWithText("Light").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            val controller = WindowCompat.getInsetsController(
                composeRule.activity.window,
                composeRule.activity.window.decorView,
            )
            controller.isAppearanceLightStatusBars && controller.isAppearanceLightNavigationBars
        }
        composeRule.onNodeWithText("Light").assertIsSelected()
        composeRule.runOnIdle {
            val activity = composeRule.activity
            val controller = WindowCompat.getInsetsController(
                activity.window,
                activity.window.decorView,
            )
            assertTrue(controller.isAppearanceLightStatusBars)
            assertTrue(controller.isAppearanceLightNavigationBars)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val nightBits = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                assertEquals(Configuration.UI_MODE_NIGHT_NO, nightBits)
            }
        }

        composeRule.onNodeWithText("Dark").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            val controller = WindowCompat.getInsetsController(
                composeRule.activity.window,
                composeRule.activity.window.decorView,
            )
            !controller.isAppearanceLightStatusBars && !controller.isAppearanceLightNavigationBars
        }
        composeRule.onNodeWithText("Dark").assertIsSelected()
        composeRule.runOnIdle {
            val activity = composeRule.activity
            val controller = WindowCompat.getInsetsController(
                activity.window,
                activity.window.decorView,
            )
            assertFalse(controller.isAppearanceLightStatusBars)
            assertFalse(controller.isAppearanceLightNavigationBars)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val nightBits = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                assertEquals(Configuration.UI_MODE_NIGHT_YES, nightBits)
            }
            assertSame(initialActivity, activity)
        }

        // Restore default preference on device after assertions complete.
        composeRule.onNodeWithText("System").performClick()
    }
}
