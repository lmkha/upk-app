package com.example.upk.core.common.errors

class NonJsonFormatException(httpCode: Int, errorBody: String, cause: Throwable) :
    Exception("HTTP $httpCode: Server returned non-JSON error format. Body: $errorBody", cause)