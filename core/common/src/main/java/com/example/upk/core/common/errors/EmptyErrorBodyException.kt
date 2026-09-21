package com.example.upk.core.common.errors

class EmptyErrorBodyException(httpCode: Int, httpMessage: String) :
    Exception("HTTP $httpCode: $httpMessage")