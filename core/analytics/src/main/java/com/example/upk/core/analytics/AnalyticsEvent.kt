package com.example.upk.core.analytics

interface AnalyticsEvent {
    val name: String
    val params: Map<String, Any> get() = emptyMap()
}