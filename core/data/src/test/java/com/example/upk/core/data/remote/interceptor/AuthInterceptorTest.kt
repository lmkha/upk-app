package com.example.upk.core.data.remote.interceptor

import com.example.upk.core.data.remote.FakeTokenProvider
import com.example.upk.core.data.remote.OkHttpTestSupport.executeThrough
import com.example.upk.core.data.remote.OkHttpTestSupport.request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {

    @Test
    fun `adds bearer header when access token exists`() {
        val interceptor = AuthInterceptor(FakeTokenProvider(token = "abc123"))

        val sent = executeThrough(interceptor)

        assertEquals("Bearer abc123", sent.header("Authorization"))
    }

    @Test
    fun `adds no header when access token is null`() {
        val interceptor = AuthInterceptor(FakeTokenProvider(token = null))

        val sent = executeThrough(interceptor)

        assertNull(sent.header("Authorization"))
    }

    @Test
    fun `adds no header when access token is empty`() {
        val interceptor = AuthInterceptor(FakeTokenProvider(token = ""))

        val sent = executeThrough(interceptor)

        assertNull(sent.header("Authorization"))
    }

    @Test
    fun `keeps other headers and url untouched`() {
        val interceptor = AuthInterceptor(FakeTokenProvider(token = "abc123"))
        val original = request { header("X-Trace", "t1") }

        val sent = executeThrough(interceptor, original)

        assertEquals(original.url, sent.url)
        assertEquals("t1", sent.header("X-Trace"))
    }

    @Test
    fun `reads the token on every request so a refreshed token is picked up`() {
        val tokenProvider = FakeTokenProvider(token = "old")
        val interceptor = AuthInterceptor(tokenProvider)
        executeThrough(interceptor)

        tokenProvider.token = "new"
        val sent = executeThrough(interceptor)

        assertEquals("Bearer new", sent.header("Authorization"))
    }
}
