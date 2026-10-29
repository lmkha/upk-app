package com.example.upk.core.analytics.di

import com.example.upk.core.analytics.AnalyticsTracker
import com.example.upk.core.analytics.CrashReporter
import com.example.upk.core.analytics.firebase.FirebaseAnalyticsTracker
import com.example.upk.core.analytics.firebase.FirebaseCrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        implementation: FirebaseAnalyticsTracker
    ): AnalyticsTracker

    @Binds
    @Singleton
    abstract fun bindCrashReporter(
        implementation: FirebaseCrashReporter
    ): CrashReporter
}
