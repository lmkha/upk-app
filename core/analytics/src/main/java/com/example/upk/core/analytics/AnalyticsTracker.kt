package com.example.upk.core.analytics

interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
    fun logScreenView(screenName: String)
}
