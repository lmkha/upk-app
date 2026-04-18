package com.example.androidspace.ui.screens.add_payment_binding

import androidx.lifecycle.ViewModel
import com.example.androidspace.data.repository.PaymentRepository
import com.stripe.android.paymentsheet.CreateIntentResult
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddPaymentBindingViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    suspend fun fetchClientSecret(): CreateIntentResult {
        return try {
            val result = paymentRepository.createBinding()

            if (result != null && result.clientSecret.isNotEmpty()) {
                CreateIntentResult.Success(result.clientSecret)
            } else {
                CreateIntentResult.Failure(
                    cause = Exception("Empty client secret"),
                    displayMessage = "Failed to create payment binding."
                )
            }
        } catch (e: Exception) {
            CreateIntentResult.Failure(
                cause = e,
                displayMessage = e.message
            )
        }
    }

    fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Canceled -> {
                println("Payment canceled by user.")
            }
            is PaymentSheetResult.Failed -> {
                println("Payment failed: ${result.error.message}")
            }
            is PaymentSheetResult.Completed -> {
                println("Payment completed successfully!")
                // TODO: Update your UI state or navigate to a success screen
            }
        }
    }
}