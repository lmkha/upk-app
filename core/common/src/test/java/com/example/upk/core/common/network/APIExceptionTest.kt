package com.example.upk.core.common.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class APIExceptionTest {

    @Test
    fun `exposes message code and field errors`() {
        val exception = APIException(
            code = "AUTH_001",
            message = "Token expired",
            fieldErrors = listOf(FieldError("token", "expired")),
        )

        assertEquals("AUTH_001", exception.code)
        assertEquals("Token expired", exception.message)
        assertEquals(listOf(FieldError("token", "expired")), exception.fieldErrors)
    }

    @Test
    fun `message and code default to null`() {
        val exception = APIException(fieldErrors = emptyList())

        assertNull(exception.message)
        assertNull(exception.code)
        assertTrue(exception.fieldErrors.isEmpty())
    }

    @Test
    fun `is an Exception so it can be thrown and caught`() {
        val caught = runCatching { throw APIException(message = "x", fieldErrors = emptyList()) }
            .exceptionOrNull()

        assertTrue(caught is APIException)
    }
}
