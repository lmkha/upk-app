package com.example.upk.core.data.remote.interceptor

import com.example.upk.core.data.remote.AppLanguageProvider
import com.example.upk.core.data.remote.OkHttpTestSupport.executeThrough
import com.example.upk.core.data.remote.OkHttpTestSupport.request
import org.junit.Assert.assertEquals
import org.junit.Test

class AcceptLanguageInterceptorTest {

    private class FakeAppLanguageProvider(var tag: String) : AppLanguageProvider {
        override fun getLanguageTag(): String = tag
    }

    @Test
    fun `sets Accept-Language header from provider`() {
        val interceptor = AcceptLanguageInterceptor(FakeAppLanguageProvider("vi-VN"))

        val sent = executeThrough(interceptor)

        assertEquals("vi-VN", sent.header("Accept-Language"))
    }

    @Test
    fun `replaces an Accept-Language header already on the request`() {
        val interceptor = AcceptLanguageInterceptor(FakeAppLanguageProvider("ja-JP"))
        val original = request { header("Accept-Language", "en-US") }

        val sent = executeThrough(interceptor, original)

        assertEquals(listOf("ja-JP"), sent.headers("Accept-Language"))
    }

    @Test
    fun `reads the language on every request`() {
        val provider = FakeAppLanguageProvider("en")
        val interceptor = AcceptLanguageInterceptor(provider)
        executeThrough(interceptor)

        provider.tag = "vi"
        val sent = executeThrough(interceptor)

        assertEquals("vi", sent.header("Accept-Language"))
    }
}
