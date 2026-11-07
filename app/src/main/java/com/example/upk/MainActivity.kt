package com.example.upk

import android.app.UiModeManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.ui.app.App
import com.example.upk.ui.app.AppIntent
import com.example.upk.ui.app.AppViewModel
import com.example.upk.ui.theme.UPKTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val uiState by appViewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (uiState.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM, null -> isSystemInDarkTheme()
            }

            // ThemeMode: LIGHT, DARK, SYSTEM
            LaunchedEffect(uiState.themeMode) {
                uiState.themeMode?.let(::applyApplicationNightMode)
            }

            // isDarkTheme or not
            DisposableEffect(darkTheme) {
                val systemBarStyle = SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ) { darkTheme }
                enableEdgeToEdge(
                    statusBarStyle = systemBarStyle,
                    navigationBarStyle = systemBarStyle,
                )
                onDispose {}
            }

            UPKTheme(darkTheme = darkTheme) {
                App(
                    modifier = Modifier.fillMaxSize(),
                    onScreenViewed = { screenName ->
                        appViewModel.onIntent(AppIntent.ScreenViewed(screenName))
                    },
                )
            }
        }
    }

    private fun applyApplicationNightMode(themeMode: ThemeMode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val uiModeManager = getSystemService(UiModeManager::class.java) ?: return
        val nightMode = when (themeMode) {
            ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
            ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
        }
        uiModeManager.setApplicationNightMode(nightMode)
    }
}