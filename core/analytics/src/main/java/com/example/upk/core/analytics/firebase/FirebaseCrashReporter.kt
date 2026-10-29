package com.example.upk.core.analytics.firebase

import com.example.upk.core.analytics.CrashReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporter @Inject constructor() : CrashReporter {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordNonFatal(exception: Throwable) {
        crashlytics.recordException(exception)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}
