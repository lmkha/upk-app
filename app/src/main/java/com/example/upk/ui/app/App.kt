package com.example.upk.ui.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.upk.home.ui.HomeScreen
import com.example.upk.settings.domain.models.ThemeMode
import com.example.upk.settings.ui.SettingsScreen
import com.example.upk.ui.Destination
import com.example.upk.ui.theme.UPKTheme

@Composable
fun App(viewModel: AppViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val systemDarkTheme = isSystemInDarkTheme()

    val darkTheme = when (uiState.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDarkTheme
    }

    val navController = rememberNavController()

    UPKTheme(darkTheme = darkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destination.MainGraph,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                navigation<Destination.MainGraph>(startDestination = Destination.MainGraph.Home) {
                    composable<Destination.MainGraph.Home> {
                        HomeScreen(
                            onNavigateToSettings = {
                                navController.navigate(Destination.Settings)
                            }
                        )
                    }
                }
                composable<Destination.Settings> {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
