package com.example.upk.core.analytics

sealed interface AppAnalyticsEvent : AnalyticsEvent {
    data class ThemeChanged(
        val theme: String
    ) : AppAnalyticsEvent {
        override val name: String = "theme_changed"
        override val params: Map<String, Any> = mapOf(
            "theme" to theme
        )
    }

    data object LogoutClicked : AppAnalyticsEvent {
        override val name: String = "logout_clicked"
    }
}