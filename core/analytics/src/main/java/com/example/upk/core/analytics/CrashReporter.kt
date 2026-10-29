package com.example.upk.core.analytics

interface CrashReporter {
    fun recordNonFatal(exception: Throwable)

    fun log(message: String)

    fun setCustomKey(key: String, value: String)

}