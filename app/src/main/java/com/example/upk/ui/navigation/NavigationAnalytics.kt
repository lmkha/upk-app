package com.example.upk.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute

internal fun NavDestination.toAnalyticsScreenName(): String? = when {
    hasRoute<Destination.MainGraph.Home>() -> "home"
    hasRoute<Destination.Settings>() -> "setting"
    else -> null
}