package com.example.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.data.remote.common.ApiException
import com.example.data.payment.repository.PaymentRepository
import com.stripe.android.paymentsheet.CreateIntentResult
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AddPaymentBindingViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
): ViewModel() {

    suspend fun fetchClientSecret(): CreateIntentResult {
        return paymentRepository.createBinding().fold(
            onSuccess = { model->
                CreateIntentResult.Success(model.clientSecret)
            },
            onFailure = { throwable ->
                when (throwable) {
                    is ApiException -> {
                        Log.d("CHECK_VAR", "errors: ${throwable.errors}")
                        CreateIntentResult.Failure(
                            cause = Exception("Empty client secret"),
                            displayMessage = "Failed to create payment binding."
                        )
                    }
                    is IOException -> {
                        CreateIntentResult.Failure(
                            cause = throwable,
                            displayMessage = "No internet connection. Please check your network."
                        )
                    }
                    else -> {
                        CreateIntentResult.Failure(
                            cause = Exception(throwable),
                            displayMessage = "An unexpected error occurred."
                        )
                    }
                }
            }
        )
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