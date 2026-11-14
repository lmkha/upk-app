package com.example.upk.core.data.remote

import com.example.upk.core.common.CoreCommonModule
import com.example.upk.core.common.errors.EmptyErrorBodyException
import com.example.upk.core.common.errors.EmptyResponseBodyException
import com.example.upk.core.common.errors.NonJsonFormatException
import com.example.upk.core.common.network.ApiException
import com.example.upk.core.common.network.FieldError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/**
 * execute, executeForResponse and executeRaw are protected, so a small subclass exposes them.
 * Responses are built with retrofit2.Response.success/error, exactly what a Retrofit API
 * interface would return.
 */
class RemoteDataSourceTest {

    @Serializable
    data class UserDto(val id: Int, val name: String)

    private class TestRemoteDataSource : RemoteDataSource(CoreCommonModule.provideJson()) {
        suspend fun fetchUser(request: suspend () -> Response<ResponseBody>): Result<UserDto> =
            execute { request() }

        suspend fun fetchNothing(request: suspend () -> Response<ResponseBody>): Result<Unit> =
            execute { request() }

        suspend fun fetchUserResponse(
            request: suspend () -> Response<ResponseBody>,
        ): Result<ApiResponse<UserDto>> = executeForResponse { request() }

        suspend fun fetchNothingResponse(
            request: suspend () -> Response<ResponseBody>,
        ): Result<ApiResponse<Unit>> = executeForResponse { request() }

        suspend fun fetchRaw(
            request: suspend () -> Response<ResponseBody>,
        ): Result<Response<ResponseBody>> = executeRaw { request() }
    }

    private val dataSource = TestRemoteDataSource()
    private val jsonType = "application/json".toMediaType()

    private fun ok(
        body: String,
        code: Int = 200,
        headers: Headers = Headers.EMPTY,
    ): Response<ResponseBody> {
        val rawResponse = okhttp3.Response.Builder()
            .request(Request.Builder().url("http://localhost/test").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("OK")
            .headers(headers)
            .build()
        return Response.success(body.toResponseBody(jsonType), rawResponse)
    }

    private fun error(code: Int, body: String): Response<ResponseBody> =
        Response.error(code, body.toResponseBody(jsonType))

    // ---------- Success responses ----------

    @Test
    fun `decodes data from success envelope`() = runBlocking {
        val result = dataSource.fetchUser { ok("""{"data":{"id":7,"name":"Kha"},"message":"ok"}""") }

        assertEquals(UserDto(7, "Kha"), result.getOrThrow())
    }

    @Test
    fun `empty success body with Unit type is success`() = runBlocking {
        val result = dataSource.fetchNothing { ok("") }

        assertEquals(Unit, result.getOrThrow())
    }

    @Test
    fun `empty success body with data type is EmptyResponseBodyException`() = runBlocking {
        val result = dataSource.fetchUser { ok("") }

        val exception = result.exceptionOrNull()
        assertTrue(exception is EmptyResponseBodyException)
        assertEquals("HTTP 200: Response body is empty but expected data.", exception!!.message)
    }

    @Test
    fun `malformed success body is SerializationException`() = runBlocking {
        val result = dataSource.fetchUser { ok("""{"data":{"id":"not-a-number"}}""") }

        assertTrue(result.exceptionOrNull() is SerializationException)
    }

    // ---------- Full parsed responses (executeForResponse) ----------

    @Test
    fun `executeForResponse preserves data envelope message status code and headers`() = runBlocking {
        val headers = headersOf("Location", "/api/v1/users/7", "ETag", "\"v1\"")

        val result = dataSource.fetchUserResponse {
            ok(
                body = """{"data":{"id":7,"name":"Kha"},"message":"Created"}""",
                code = 201,
                headers = headers,
            )
        }

        val response = result.getOrThrow()
        assertEquals(UserDto(7, "Kha"), response.data)
        assertEquals("Created", response.message)
        assertEquals(201, response.code)
        assertEquals("/api/v1/users/7", response.headers["Location"])
        assertEquals("\"v1\"", response.headers["ETag"])
    }

    @Test
    fun `executeForResponse with empty body and Unit type preserves status code and Location header`() = runBlocking {
        val headers = headersOf("Location", "https://pay.example.com/session/abc")

        val result = dataSource.fetchNothingResponse {
            ok(
                body = "",
                code = 201,
                headers = headers,
            )
        }

        val response = result.getOrThrow()
        assertEquals(Unit, response.data)
        assertEquals(null, response.message)
        assertEquals(201, response.code)
        assertEquals("https://pay.example.com/session/abc", response.headers["Location"])
    }

    // ---------- Raw responses (executeRaw) ----------

    @Test
    fun `executeRaw returns raw Retrofit response without decoding json envelope`() = runBlocking {
        val headers = headersOf("Content-Disposition", "attachment; filename=\"invoice.txt\"")

        val result = dataSource.fetchRaw {
            ok(
                body = "plain-text-content",
                code = 200,
                headers = headers,
            )
        }

        val response = result.getOrThrow()
        assertEquals(200, response.code())
        assertEquals("attachment; filename=\"invoice.txt\"", response.headers()["Content-Disposition"])
        assertEquals("plain-text-content", response.body()!!.string())
    }

    @Test
    fun `executeRaw parses error body into ApiException on non 2xx response`() = runBlocking {
        val result = dataSource.fetchRaw { error(403, """{"message":"Forbidden","code":"FORBIDDEN"}""") }

        val exception = result.exceptionOrNull() as ApiException
        assertEquals("FORBIDDEN", exception.code)
        assertEquals("Forbidden", exception.message)
    }

    // ---------- Error responses ----------

    @Test
    fun `json error body becomes ApiException with code message and field errors`() = runBlocking {
        val result = dataSource.fetchUser {
            error(
                422,
                """{"message":"Validation failed","code":"VALIDATION","fieldErrors":[{"field":"name","message":"required"}]}""",
            )
        }

        val exception = result.exceptionOrNull() as ApiException
        assertEquals("VALIDATION", exception.code)
        assertEquals("Validation failed", exception.message)
        assertEquals(listOf(FieldError("name", "required")), exception.fieldErrors)
    }

    @Test
    fun `json error body without optional fields becomes ApiException with defaults`() = runBlocking {
        val result = dataSource.fetchUser { error(404, """{"message":"Not found"}""") }

        val exception = result.exceptionOrNull() as ApiException
        assertEquals("Not found", exception.message)
        assertEquals(null, exception.code)
        assertTrue(exception.fieldErrors.isEmpty())
    }

    @Test
    fun `empty error body becomes EmptyErrorBodyException with http code`() = runBlocking {
        val result = dataSource.fetchUser { error(503, "") }

        val exception = result.exceptionOrNull()
        assertTrue(exception is EmptyErrorBodyException)
        assertTrue(exception!!.message!!.startsWith("HTTP 503: "))
    }

    @Test
    fun `non json error body becomes NonJsonFormatException keeping the body`() = runBlocking {
        val result = dataSource.fetchUser { error(502, "<html>Bad Gateway</html>") }

        val exception = result.exceptionOrNull()
        assertTrue(exception is NonJsonFormatException)
        assertTrue(exception!!.message!!.contains("<html>Bad Gateway</html>"))
        assertTrue(exception.cause is SerializationException)
    }

    // ---------- Thrown exceptions ----------

    @Test
    fun `IOException from request becomes failure`() = runBlocking {
        val io = IOException("no network")

        val result = dataSource.fetchUser { throw io }

        assertSame(io, result.exceptionOrNull())
    }

    @Test
    fun `CancellationException is rethrown when the coroutine is cancelled`() = runBlocking {
        val deferred = async {
            dataSource.fetchUser {
                this@async.cancel()
                throw CancellationException("cancelled during request")
            }
        }

        try {
            deferred.await()
            fail("expected CancellationException")
        } catch (expected: CancellationException) {
            // execute must not swallow cancellation into a Result.failure
        }
    }

    @Test
    fun `CancellationException without actual cancellation is reported as failure`() = runBlocking {
        // Documents current behaviour: only a cancelled coroutine rethrows.
        val result = dataSource.fetchUser { throw CancellationException("spurious") }

        assertTrue(result.exceptionOrNull() is CancellationException)
    }
}
