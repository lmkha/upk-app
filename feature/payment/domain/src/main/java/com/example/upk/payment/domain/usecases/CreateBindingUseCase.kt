package com.example.upk.payment.domain.usecases

import com.example.upk.payment.domain.models.CreatePaymentBindingModel
import com.example.upk.payment.domain.repository.PaymentRepository
import javax.inject.Inject

class CreateBindingUseCase @Inject constructor(private val repository: PaymentRepository) {
    suspend operator fun invoke() : Result<CreatePaymentBindingModel> {
        return repository.createBinding()
    }
}