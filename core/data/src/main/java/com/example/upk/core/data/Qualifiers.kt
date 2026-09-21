package com.example.upk.core.data

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedRetrofit