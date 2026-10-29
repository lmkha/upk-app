package com.example.upk.core.data.remote

/**
 * In-memory [TokenProvider]. `refreshResult` controls what refreshToken() returns
 * and is also stored as the new token, mirroring a real refresh.
 *
 * The backing property is named `token` (not `accessToken`) because a Kotlin property
 * `accessToken` would generate a getAccessToken() that clashes with the interface method.
 */
class FakeTokenProvider(
    var token: String? = null,
    var refreshResult: String? = null,
) : TokenProvider {

    var refreshCallCount = 0
        private set
    var clearCallCount = 0
        private set

    override fun getAccessToken(): String? = token

    override fun refreshToken(): String? {
        refreshCallCount++
        token = refreshResult
        return refreshResult
    }

    override fun clearTokens() {
        clearCallCount++
        token = null
    }
}
