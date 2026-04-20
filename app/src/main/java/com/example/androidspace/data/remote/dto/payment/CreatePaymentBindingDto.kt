package com.example.androidspace.data.remote.dto.payment

import com.example.androidspace.domain.models.CreatePaymentBindingModel
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentBindingDto(
    val customerId: String,
    val clientSecret: String,
    val ephemeralKeySecret: String
)

fun CreatePaymentBindingDto.toDomain(): CreatePaymentBindingModel {
    return CreatePaymentBindingModel(
        customerId = this.customerId,
        clientSecret = this.clientSecret,
        ephemeralKeySecret = this.ephemeralKeySecret
    )
}