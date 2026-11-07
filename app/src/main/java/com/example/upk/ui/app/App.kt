package com.example.upk.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.upk.home.ui.HomeScreen
import com.example.upk.settings.ui.SettingsScreen
import com.example.upk.ui.navigation.Destination
import com.example.upk.ui.navigation.toAnalyticsScreenName
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

@Composable
fun App(
    modifier: Modifier = Modifier,
    onScreenViewed: (String) -> Unit,
) {
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnScreenViewed by rememberUpdatedState(onScreenViewed)

    LaunchedEffect(navController, lifecycleOwner) {
        navController.currentBackStackEntryFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .mapNotNull { entry -> entry.destination.toAnalyticsScreenName() }
            .distinctUntilChanged()
            .collect { screenName ->
                currentOnScreenViewed(screenName)
            }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.MainGraph,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            navigation<Destination.MainGraph>(startDestination = Destination.MainGraph.Home) {
                composable<Destination.MainGraph.Home> {
                    HomeScreen(
                        onNavigateToSettings = { navController.navigate(Destination.Settings) },
                    )
                }
            }
            composable<Destination.Settings> {
                SettingsScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}