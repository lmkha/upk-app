package com.example.androidspace.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentBindingDto(
    val customerId: String,
    val clientSecret: String,
    val ephemeralKeySecret: String
) : Dto