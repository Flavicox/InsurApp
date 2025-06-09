package com.flavicox.insurapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.flavicox.insurapp.navigation.AppNavigation
import com.flavicox.insurapp.viewmodel.StripePaymentViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class MainActivity : ComponentActivity() {
    private lateinit var paymentSheet: PaymentSheet
    private val stripeViewModel: StripePaymentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paymentSheet = PaymentSheet(this) { result ->
            stripeViewModel.onPaymentResult(result)
        }

        setContent {
            AppNavigation()
        }
    }
}
