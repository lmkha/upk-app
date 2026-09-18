package com.example.data.common

class EmptyResponseBodyException(httpCode: Int) :
    Exception("HTTP $httpCode: Response body is empty but expected data.")