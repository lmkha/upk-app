package com.example.androidspace.models

data class CreatePaymentBindingModel(
    val customerId: String,
    val clientSecret: String,
    val ephemeralKeySecret: String
)
