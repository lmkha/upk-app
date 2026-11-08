package com.example.upk.ui.app

import com.example.upk.core.analytics.AnalyticsEvent
import com.example.upk.core.analytics.AnalyticsTracker

class FakeAnalyticsTracker : AnalyticsTracker {

    var logEventCallCount = 0
        private set

    var lastLoggedEvent: AnalyticsEvent? = null
        private set

    var logScreenViewCallCount = 0
        private set

    var lastLoggedScreenName: String? = null
        private set

    private val _loggedScreenNames = mutableListOf<String>()
    val loggedScreenNames: List<String>
        get() = _loggedScreenNames.toList()

    override fun logEvent(event: AnalyticsEvent) {
        logEventCallCount++
        lastLoggedEvent = event
    }

    override fun logScreenView(screenName: String) {
        logScreenViewCallCount++
        lastLoggedScreenName = screenName
        _loggedScreenNames.add(screenName)
    }
}
