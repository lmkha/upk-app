package com.example.androidspace.di

import javax.inject.Qualifier

// 1. The OkHttp Clients
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedOkHttpClient

// 2. The Retrofit Instances
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedRetrofit