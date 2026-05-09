package com.example.data.payment.models

data class CreatePaymentBindingModel(
    val customerId: String,
    val clientSecret: String,
    val ephemeralKeySecret: String
)
