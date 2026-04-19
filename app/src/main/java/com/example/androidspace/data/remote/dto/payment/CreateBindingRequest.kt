package com.example.androidspace.data.remote.dto.payment

import kotlinx.serialization.Serializable

@Serializable
data class CreateBindingRequest(
    val method: String = "Stripe",
    val userId: String = "38b500b3-ac5a-4bf1-bc36-402a066c0490",
    val email: String = "lmkha0201@gmail.com",
    val bindingType: String = "CARD",
    val bindingMode: String = "BIND",
    val deviceType: String = "MOBILE",
    val isDefault: Boolean = false
)