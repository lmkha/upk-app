package com.example.upk.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {

    sealed interface Screen : Destination {
        val screenName: String
    }
    @Serializable
    data object MainGraph : Destination {

        @Serializable
        data object Home : Screen {
            override val screenName: String get() = "home"
        }
    }

    @Serializable
    data object Settings : Screen {
        override val screenName: String get() = "setting"
    }
}