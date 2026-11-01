package com.example.upk.core.crashlytics.firebase

import com.example.upk.core.crashlytics.CrashReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FirebaseCrashReporter @Inject constructor() : CrashReporter {

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
