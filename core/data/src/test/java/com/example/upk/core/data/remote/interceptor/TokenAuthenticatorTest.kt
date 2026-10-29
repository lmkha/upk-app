package com.example.upk.core.data.remote.interceptor

import com.example.upk.core.data.remote.FakeTokenProvider
import com.example.upk.core.data.remote.OkHttpTestSupport.fakeResponse
import com.example.upk.core.data.remote.OkHttpTestSupport.request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * OkHttp calls Authenticator.authenticate() when the server answers 401.
 * Returning a Request makes OkHttp retry with it; returning null gives up.
 * The authenticator is exercised directly with hand-built 401 responses.
 */
class TokenAuthenticatorTest {

    private fun unauthorized(token: String?, priorResponse: Response? = null): Response {
        val request = request {
            if (token != null) header("Authorization", "Bearer $token")
        }
        return fakeResponse(request, code = 401, priorResponse = priorResponse)
    }

    // ---------- Refresh path ----------

    @Test
    fun `refreshes token and retries with the new token when request used the current token`() {
        val tokenProvider = FakeTokenProvider(token = "old", refreshResult = "new")
        val authenticator = TokenAuthenticator(tokenProvider)

        val retry = authenticator.authenticate(null, unauthorized(token = "old"))

        assertNotNull(retry)
        assertEquals("Bearer new", retry!!.header("Authorization"))
        assertEquals(1, tokenProvider.refreshCallCount)
        assertEquals(0, tokenProvider.clearCallCount)
    }

    @Test
    fun `reuses a token already refreshed by another call without refreshing again`() {
        // Another request already refreshed: provider holds "new" while this request still sent "old".
        val tokenProvider = FakeTokenProvider(token = "new", refreshResult = "newer")
        val authenticator = TokenAuthenticator(tokenProvider)

        val retry = authenticator.authenticate(null, unauthorized(token = "old"))

        assertEquals("Bearer new", retry!!.header("Authorization"))
        assertEquals(0, tokenProvider.refreshCallCount)
    }

    @Test
    fun `refreshes when the request carried no Authorization header and no token is stored`() {
        val tokenProvider = FakeTokenProvider(token = null, refreshResult = "new")
        val authenticator = TokenAuthenticator(tokenProvider)

        val retry = authenticator.authenticate(null, unauthorized(token = null))

        assertEquals("Bearer new", retry!!.header("Authorization"))
        assertEquals(1, tokenProvider.refreshCallCount)
    }

    @Test
    fun `uses stored token without refreshing when request had no Authorization header`() {
        // Documents current behavior: a stored token differs from the (absent) request token.
        val tokenProvider = FakeTokenProvider(token = "stored", refreshResult = "new")
        val authenticator = TokenAuthenticator(tokenProvider)

        val retry = authenticator.authenticate(null, unauthorized(token = null))

        assertEquals("Bearer stored", retry!!.header("Authorization"))
        assertEquals(0, tokenProvider.refreshCallCount)
    }

    // ---------- Give-up paths ----------

    @Test
    fun `clears tokens and gives up when refresh fails`() {
        val tokenProvider = FakeTokenProvider(token = "old", refreshResult = null)
        val authenticator = TokenAuthenticator(tokenProvider)

        val retry = authenticator.authenticate(null, unauthorized(token = "old"))

        assertNull(retry)
        assertEquals(1, tokenProvider.refreshCallCount)
        assertEquals(1, tokenProvider.clearCallCount)
        assertNull(tokenProvider.token)
    }

    @Test
    fun `retries when there is one prior 401`() {
        val tokenProvider = FakeTokenProvider(token = "old", refreshResult = "new")
        val authenticator = TokenAuthenticator(tokenProvider)
        val first = unauthorized(token = "old")
        val second = unauthorized(token = "old", priorResponse = first)

        val retry = authenticator.authenticate(null, second)

        assertNotNull(retry)
    }

    @Test
    fun `gives up after two prior 401 responses to avoid an infinite loop`() {
        val tokenProvider = FakeTokenProvider(token = "old", refreshResult = "new")
        val authenticator = TokenAuthenticator(tokenProvider)
        val first = unauthorized(token = "old")
        val second = unauthorized(token = "old", priorResponse = first)
        val third = unauthorized(token = "old", priorResponse = second)

        val retry = authenticator.authenticate(null, third)

        assertNull(retry)
        assertEquals(0, tokenProvider.refreshCallCount)
        assertEquals(0, tokenProvider.clearCallCount)
    }

    @Test
    fun `retry request keeps url and other headers`() {
        val tokenProvider = FakeTokenProvider(token = "old", refreshResult = "new")
        val authenticator = TokenAuthenticator(tokenProvider)
        val original = request {
            header("Authorization", "Bearer old")
            header("X-Trace", "t1")
        }

        val retry = authenticator.authenticate(null, fakeResponse(original, code = 401))

        assertEquals(original.url, retry!!.url)
        assertEquals("t1", retry.header("X-Trace"))
        assertEquals(listOf("Bearer new"), retry.headers("Authorization"))
    }
}
