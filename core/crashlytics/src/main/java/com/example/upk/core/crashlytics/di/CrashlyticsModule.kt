package com.example.upk.core.crashlytics.di

import com.example.upk.core.crashlytics.CrashReporter
import com.example.upk.core.crashlytics.firebase.FirebaseCrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CrashlyticsModule {
    @Binds
    @Singleton
    abstract fun bindCrashReporter(
        implementation: FirebaseCrashReporter
    ): CrashReporter
}