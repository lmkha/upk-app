package com.example.upk.settings.ui

import com.example.upk.core.analytics.AnalyticsEvent
import com.example.upk.core.analytics.AnalyticsTracker

/** Fake tracker for Compose UI tests. Records logged events in memory. */
class FakeAnalyticsTracker : AnalyticsTracker {

    var logEventCallCount = 0
        private set

    var lastLoggedEvent: AnalyticsEvent? = null
        private set

    override fun logEvent(event: AnalyticsEvent) {
        logEventCallCount++
        lastLoggedEvent = event
    }

    override fun logScreenView(screenName: String) = Unit
}
