package com.example.upk.core.common.errors

class EmptyResponseBodyException(httpCode: Int) :
    Exception("HTTP $httpCode: Response body is empty but expected data.")