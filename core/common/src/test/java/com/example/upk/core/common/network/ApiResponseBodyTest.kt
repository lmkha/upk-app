package com.example.upk.core.common.network

import com.example.upk.core.common.CoreCommonModule
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Decoding of the API envelope with the exact Json configuration the app uses
 * (CoreCommonModule.provideJson), so these tests fail if that configuration changes.
 */
class ApiResponseBodyTest {

    @Serializable
    data class UserDto(val id: Int, val name: String)

    private val json = CoreCommonModule.provideJson()

    // ---------- Success ----------

    @Test
    fun `decodes success envelope with data and message`() {
        val body = json.decodeFromString<ApiResponseBody.Success<UserDto>>(
            """{"data":{"id":1,"name":"Kha"},"message":"ok"}""",
        )

        assertEquals(UserDto(1, "Kha"), body.data)
        assertEquals("ok", body.message)
    }

    @Test
    fun `success message is optional`() {
        val body = json.decodeFromString<ApiResponseBody.Success<UserDto>>(
            """{"data":{"id":1,"name":"Kha"}}""",
        )

        assertNull(body.message)
    }

    @Test
    fun `success ignores unknown keys`() {
        val body = json.decodeFromString<ApiResponseBody.Success<UserDto>>(
            """{"data":{"id":1,"name":"Kha","role":"admin"},"message":"ok","traceId":"abc"}""",
        )

        assertEquals(UserDto(1, "Kha"), body.data)
    }

    @Test(expected = SerializationException::class)
    fun `success without data field fails to decode`() {
        json.decodeFromString<ApiResponseBody.Success<UserDto>>("""{"message":"ok"}""")
    }

    @Test
    fun `success with Unit data decodes from empty object`() {
        val body = json.decodeFromString<ApiResponseBody.Success<Unit>>("""{"data":{}}""")

        assertEquals(Unit, body.data)
    }

    // ---------- Failure ----------

    @Test
    fun `decodes failure envelope with code and field errors`() {
        val body = json.decodeFromString<ApiResponseBody.Failure>(
            """
            {
              "message": "Validation failed",
              "code": "VALIDATION_ERROR",
              "fieldErrors": [
                {"field": "email", "message": "must be valid"},
                {"field": "password", "message": null}
              ]
            }
            """.trimIndent(),
        )

        assertEquals("Validation failed", body.message)
        assertEquals("VALIDATION_ERROR", body.code)
        assertEquals(
            listOf(FieldError("email", "must be valid"), FieldError("password", null)),
            body.fieldErrors,
        )
    }

    @Test
    fun `failure code and field errors are optional`() {
        val body = json.decodeFromString<ApiResponseBody.Failure>("""{"message":"Not found"}""")

        assertEquals("Not found", body.message)
        assertNull(body.code)
        assertTrue(body.fieldErrors.isEmpty())
    }

    @Test(expected = SerializationException::class)
    fun `failure without message fails to decode`() {
        json.decodeFromString<ApiResponseBody.Failure>("""{"code":"X"}""")
    }

    @Test
    fun `failure encodes without null fields`() {
        val encoded = json.encodeToString(
            ApiResponseBody.Failure.serializer(),
            ApiResponseBody.Failure(message = "Boom"),
        )

        // explicitNulls = false drops "code": null; encodeDefaults = true keeps the empty list.
        assertEquals("""{"message":"Boom","fieldErrors":[]}""", encoded)
    }
}
