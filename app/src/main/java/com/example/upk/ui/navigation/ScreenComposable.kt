package com.example.upk.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

inline fun <reified T : Destination.Screen> NavGraphBuilder.screenComposable(
    crossinline onScreenViewed: (String) -> Unit,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T> { entry ->
        LifecycleResumeEffect(entry.id) {
            onScreenViewed(entry.toRoute<T>().screenName)
            onPauseOrDispose {}
        }
        content(entry)
    }
}
