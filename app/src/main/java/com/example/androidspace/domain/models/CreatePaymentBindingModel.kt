package com.example.androidspace.domain.models

data class CreatePaymentBindingModel(
    val customerId: String,
    val clientSecret: String,
    val ephemeralKeySecret: String
)
