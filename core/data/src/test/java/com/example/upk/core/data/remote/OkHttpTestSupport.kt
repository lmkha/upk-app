package com.example.upk.core.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Helpers to exercise interceptors and authenticators without any network.
 *
 * [executeThrough] builds a real OkHttpClient with the interceptor under test followed by a
 * terminal interceptor that records the outgoing request and answers with a fake 200 response.
 * Because the terminal interceptor returns before the network layer, no socket is ever opened.
 */
object OkHttpTestSupport {

    const val BASE_URL = "http://localhost/api/"

    fun executeThrough(interceptor: Interceptor, request: Request = request()): Request {
        var captured: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor { chain ->
                captured = chain.request()
                fakeResponse(chain.request(), code = 200)
            }
            .build()

        client.newCall(request).execute().close()

        return checkNotNull(captured) { "Terminal interceptor was not reached" }
    }

    fun request(builder: Request.Builder.() -> Unit = {}): Request =
        Request.Builder().url(BASE_URL).apply(builder).build()

    fun fakeResponse(request: Request, code: Int, priorResponse: Response? = null): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 401) "Unauthorized" else "OK")
            .body("".toResponseBody())
            .priorResponse(priorResponse)
            .build()
}
