package com.example.upk.core.common.errors

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NetworkErrorsTest {

    @Test
    fun `EmptyErrorBodyException message contains http code and message`() {
        val exception = EmptyErrorBodyException(httpCode = 503, httpMessage = "Service Unavailable")

        assertEquals("HTTP 503: Service Unavailable", exception.message)
    }

    @Test
    fun `EmptyResponseBodyException message contains http code`() {
        val exception = EmptyResponseBodyException(httpCode = 200)

        assertEquals("HTTP 200: Response body is empty but expected data.", exception.message)
    }

    @Test
    fun `NonJsonFormatException keeps body and cause`() {
        val cause = IllegalStateException("bad json")

        val exception = NonJsonFormatException(httpCode = 500, errorBody = "<html>", cause = cause)

        assertEquals("HTTP 500: Server returned non-JSON error format. Body: <html>", exception.message)
        assertSame(cause, exception.cause)
    }
}
