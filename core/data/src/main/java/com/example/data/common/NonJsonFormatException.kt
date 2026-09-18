package com.example.data.common

class NonJsonFormatException(httpCode: Int, errorBody: String, cause: Throwable) :
    Exception("HTTP $httpCode: Server returned non-JSON error format. Body: $errorBody", cause)