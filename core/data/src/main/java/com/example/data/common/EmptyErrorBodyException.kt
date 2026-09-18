package com.example.data.common

class EmptyErrorBodyException(httpCode: Int, httpMessage: String) :
    Exception("HTTP $httpCode: $httpMessage")