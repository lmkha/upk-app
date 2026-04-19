package com.example.androidspace.data.remote.dto

import com.example.androidspace.models.PaymentProviderModel
import kotlinx.serialization.Serializable

@Serializable
data class PaymentProviderDto(
    val id: String,
    val name: String,
    val code: String,
    val logo: String,
    val priority: Int,
    val isDefault: Boolean,
    val description: String,
    val status: String
)

fun PaymentProviderDto.toDomain() : PaymentProviderModel {
    return PaymentProviderModel(
        id = this.id,
        name = this.name,
        code = this.code,
        logo = this.logo,
        priority = this.priority,
        isDefault = this.isDefault,
        description = this.description,
        status = this.status,
    )
}