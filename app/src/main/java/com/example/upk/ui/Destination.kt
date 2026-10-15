package com.example.upk.ui

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {

    @Serializable
    data object MainGraph : Destination {

        @Serializable
        data object Home : Destination
    }

    @Serializable
    data object Settings : Destination
}