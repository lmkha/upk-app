package com.example.upk.core.analytics.firebase

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.upk.core.analytics.AnalyticsEvent
import com.example.upk.core.analytics.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ParametersBuilder
import com.google.firebase.analytics.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirebaseAnalytics"
private const val DEFAULT_SCREEN_CLASS = "MainActivity"

@Singleton
internal class FirebaseAnalyticsTracker @Inject constructor(
    @ApplicationContext context: Context,
) : AnalyticsTracker {

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(event: AnalyticsEvent) {
        if (event.params.isEmpty()) {
            analytics.logEvent(event.name, null)
            return
        }
        analytics.logEvent(event.name) {
            event.params.forEach { (key, value) -> putParam(key, value) }
        }
    }

    override fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, DEFAULT_SCREEN_CLASS)
        }
    }

    private fun ParametersBuilder.putParam(key: String, value: Any) {
        when (value) {
            is String -> param(key, value)
            is Long -> param(key, value)
            is Int -> param(key, value.toLong())
            is Double -> param(key, value)
            is Float -> param(key, value.toDouble())
            is Boolean -> param(key, value.toString())
            is Bundle -> param(key, value)
            is Array<*> -> if (value.isArrayOf<Bundle>()) {
                @Suppress("UNCHECKED_CAST")
                param(key, value as Array<Bundle>)
            } else {
                Log.w(TAG, "Unsupported array type for analytics parameter '$key'")
            }
            else -> Log.w(TAG, "Unsupported analytics parameter '$key' of type ${value::class.simpleName}")
        }
    }
}