package com.example.upk.core.analytics.firebase

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.upk.core.analytics.AnalyticsEvent
import com.example.upk.core.analytics.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseAnalyticsTracker @Inject constructor(
    @ApplicationContext context: Context
) : AnalyticsTracker {

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(event: AnalyticsEvent) {
        Log.d("FirebaseAnalytics", "logEvent -> name: ${event.name}, params: ${event.params}")
        analytics.logEvent(event.name) {
            event.params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Double -> param(key, value)
                    is Float -> param(key, value.toDouble())
                    is Bundle -> param(key, value)

                    is Array<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val bundles = value as? Array<Bundle>

                        if (bundles != null) {
                            param(key, bundles)
                        } else {
                            Log.w(
                                TAG,
                                "Unsupported array type for analytics parameter: $key"
                            )
                        }
                    }

                    else -> {
                        Log.w(
                            TAG,
                            "Unsupported analytics parameter type: ${value::class.simpleName}"
                        )
                    }
                }
            }
        }
    }

    override fun logScreenView(screenName: String) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(
                    FirebaseAnalytics.Param.SCREEN_NAME,
                    screenName
                )
                putString(
                    FirebaseAnalytics.Param.SCREEN_CLASS,
                    "MainActivity"
                )
            }
        )
    }
}