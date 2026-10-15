package com.example.upk.core.data.remote.interceptor

import com.example.upk.core.data.remote.AppLanguageProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcceptLanguageInterceptor @Inject constructor(
    private val appLanguageProvider: AppLanguageProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val languageTag = appLanguageProvider.getLanguageTag()

        val request = chain.request()
            .newBuilder()
            .header("Accept-Language", languageTag)
            .build()

        return chain.proceed(request)
    }
}
