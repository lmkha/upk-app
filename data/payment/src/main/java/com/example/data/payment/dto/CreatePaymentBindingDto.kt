package com.example.data.payment.dto

import com.example.data.payment.models.CreatePaymentBindingModel
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentBindingDto(
    val customerId: String,
    val clientSecret: String,
    val ephemeralKeySecret: String
)

fun CreatePaymentBindingDto.toModel(): CreatePaymentBindingModel {
    return CreatePaymentBindingModel(
        customerId = this.customerId,
        clientSecret = this.clientSecret,
        ephemeralKeySecret = this.ephemeralKeySecret
    )
}