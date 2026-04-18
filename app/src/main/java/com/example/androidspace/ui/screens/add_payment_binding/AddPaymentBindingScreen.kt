package com.example.androidspace.ui.screens.add_payment_binding

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.androidspace.BuildConfig
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet


@Composable
fun AddPaymentBindingScreen(viewModel: AddPaymentBindingViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val paymentSheet = remember {
        PaymentSheet.Builder(viewModel::onPaymentSheetResult)
            .createIntentCallback { paymentMethod, shouldSavePaymentMethod ->
                println(paymentMethod.toString())
                println(shouldSavePaymentMethod.toString())
                viewModel.fetchClientSecret()
            }
    }.build()

    LaunchedEffect(Unit) {
        PaymentConfiguration.init(
            context,
            publishableKey = BuildConfig.stripePublishableKey
        )
    }

    Button(
        onClick = {
            val intentConfig = PaymentSheet.IntentConfiguration(
                mode = PaymentSheet.IntentConfiguration.Mode.Setup(
                    currency = "usd",
                ),
            )

            paymentSheet.presentWithIntentConfiguration(
                intentConfiguration = intentConfig,
                configuration = PaymentSheet.Configuration.Builder(
                    merchantDisplayName = "Example Inc.",
                ).build()
            )
        }
    ) {
        Text("Checkout")
    }
}