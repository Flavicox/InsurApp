package com.flavicox.insurapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.viewmodel.StripePaymentViewModel
import com.flavicox.insurapp.viewmodel.StripePaymentViewModelFactory
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.rememberPaymentSheet

@Composable
fun PaymentScreen(
    navController: NavController,
    fieldLabel: String,
    date: String,
    time: String,
    price: Int,
    isHalfPayment: Boolean,
    reserveId: Int
) {
    val context = LocalContext.current
    val viewModel: StripePaymentViewModel = viewModel(factory = StripePaymentViewModelFactory(context))
    val paymentSheet = rememberPaymentSheet(viewModel::onPaymentResult)
    val paymentState by viewModel.paymentState.collectAsState()
    val finalAmount = if (isHalfPayment) price / 2 else price

    LaunchedEffect(Unit) {
        viewModel.fetchPaymentSheetData(reserveId, finalAmount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp))
        {
            BotonRegresar(navController)
            Titulo("Pagar Reserva")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Campo: $fieldLabel",
            fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Fecha: $date",
            fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Hora: $time",
            fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Precio: S/. $finalAmount",
            fontSize = 16.sp)

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = paymentState) {
            is com.flavicox.insurapp.viewmodel.PaymentState.Loading -> CircularProgressIndicator()
            is com.flavicox.insurapp.viewmodel.PaymentState.Error -> Text("Error: ${'$'}{state.message}", color = Color.Red)
            is com.flavicox.insurapp.viewmodel.PaymentState.Ready -> {
                Button(
                    onClick = {
                        paymentSheet.presentWithPaymentIntent(
                            paymentIntentClientSecret = state.clientSecret,
                            configuration = PaymentSheet.Configuration(
                                merchantDisplayName = "InsurApp",
                                customer = state.customerConfig,
                                allowsDelayedPaymentMethods = true
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pagar con Stripe")
                }
            }
            is com.flavicox.insurapp.viewmodel.PaymentState.Success -> Text("Pago completado", color = Color.Green)
            else -> {}
        }
    }
}
