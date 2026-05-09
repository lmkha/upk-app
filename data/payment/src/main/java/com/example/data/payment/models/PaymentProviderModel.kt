package com.example.data.payment.models

data class PaymentProviderModel(
    val id: String,
    val name: String,
    val code: String,
    val logo: String,
    val priority: Int,
    val isDefault: Boolean,
    val description: String,
    val status: String
)
