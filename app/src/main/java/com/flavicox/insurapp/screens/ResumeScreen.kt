package com.flavicox.insurapp.screens

// ───── Imports ─────
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.model.PaymentRequest
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.flavicox.insurapp.viewmodel.ResumeViewModel
import com.flavicox.insurapp.viewmodel.ResumeViewModelFactory
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ───── Pantalla principal ─────
@Composable
fun ResumeScreen(
    navController: NavController,
    reservation: ReservationResponse?
) {
    if (reservation == null) {
        Text(
            "Error: datos de reserva no disponibles",
            modifier = Modifier.padding(24.dp),
            color = Color.Red,
            fontSize = 18.sp
        )
        return
    }

    val context      = LocalContext.current
    val authVM: AuthViewModel       = viewModel(factory = AuthViewModelFactory(context))
    val resumeVM: ResumeViewModel   = viewModel(factory = ResumeViewModelFactory(context))
    val coroutineScope              = rememberCoroutineScope()

    /* ---------- STATE ---------- */
    var loadingIntent by remember { mutableStateOf(true) }   // creando PaymentIntent
    var customerConfig by remember { mutableStateOf<PaymentSheet.CustomerConfiguration?>(null) }
    var clientSecret   by remember { mutableStateOf<String?>(null) }
    val errorMessage   by resumeVM.errorMessage.collectAsState()

    /* ---------- CREAR INTENT AL ENTRAR ---------- */
    LaunchedEffect(reservation.reserveId) {
        val user  = reservation.user
        val field = reservation.field

        // 1️⃣ Pedimos el PaymentIntent
        resumeVM.initiatePayment(
            reservationId = reservation.reserveId,
            request = PaymentRequest(
                amount = reservation.totalPrice.toInt() * 100,          // 100 %
                clientName = "${user.name} ${user.surname}",
                productName = "Reserva ${field.typeField} #${field.numberField}",
                phone = user.phone,
                email = user.email
            )
        )

        // 2️⃣ Esperamos el PRIMER valor no nulo que emita el StateFlow
        val response = resumeVM
            .paymentResult
            .filterNotNull()     // descarta null
            .first()             // suspende hasta recibir uno

        // 3️⃣ Guardamos datos y habilitamos el botón
        customerConfig = PaymentSheet.CustomerConfiguration(
            id = response.id,
            ephemeralKeySecret = response.ephemeralSecret
        )
        clientSecret   = response.clientSecret
        loadingIntent  = false
    }

    /* ---------- UI ---------- */
    val user  = reservation.user
    val field = reservation.field
    val paymentSheet = remember {
        PaymentSheet.Builder { result ->
            onPaymentSheetResult(result) {
                navController.navigate("${AppScreens.ConfirmationScreen.route}/${reservation.reserveId}") {
                    popUpTo(AppScreens.ResumeScreen.route) { inclusive = true }
                }
            }
        }}.build()
    val publishableKey = "pk_test_51RVkIOQpnfpZJWEMPz3o71Oda9MNqCzsa1O7SbymgotbNZKMZUPHF2cgKJyfpetJHbiKbuRLoNhjp0VI2wmaFSJ2005369CYmw"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        /* ---------- TopBar ---------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(12.dp))
            Text("Resumen de Reserva", fontSize = 20.sp, color = Color.Black)
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- Tarjeta Usuario ---------- */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Datos del Usuario", fontSize = 18.sp, color = Color(0xFF0277BD))
                Spacer(Modifier.height(8.dp))
                UserDataRow(Icons.Default.Person, "Nombre",  "${user.name} ${user.surname}")
                UserDataRow(Icons.Default.Person, "Correo",  user.email)
                UserDataRow(Icons.Default.Person, "Teléfono", user.phone)
                UserDataRow(Icons.Default.Person, "DNI",     user.dni)
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ---------- Tarjeta Reserva ---------- */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Detalles de la Reserva", fontSize = 18.sp, color = Color(0xFFF57F17))
                Spacer(Modifier.height(8.dp))
                Text("Cancha: ${field.typeField} #${field.numberField}", fontSize = 16.sp)
                Text("Fecha: ${reservation.bookingDate}",                fontSize = 16.sp)
                Text("Hora:  ${reservation.timetableStart} - ${reservation.timetableEnd}", fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                Text("Precio total: S/. ${reservation.totalPrice}",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))

        /* ---------- Pago ---------- */
        Text("Pago", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        if (loadingIntent) {
            CircularProgressIndicator()
        } else {
            errorMessage?.let {
                Text("❌ $it", color = Color.Red, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    PaymentConfiguration.init(context, publishableKey)
                    customerConfig?.let { config ->
                        clientSecret?.let { secret ->
                            presentPaymentScreen(paymentSheet, config, secret)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = customerConfig != null && clientSecret != null,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pagar", color = Color.White)
            }
        }
    }
}

/* ───── Fila simple de datos ───── */
@Composable
private fun UserDataRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp), tint = Color(0xFF0277BD))
        Spacer(Modifier.width(8.dp))
        Text("$label:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(value, fontSize = 14.sp)
    }
}

/* ───── Presentar hoja de pago Stripe ───── */
private fun presentPaymentScreen(
    paymentSheet: PaymentSheet,
    customerConfig: PaymentSheet.CustomerConfiguration,
    paymentIntentSecret: String
) {
    paymentSheet.presentWithPaymentIntent(
        paymentIntentSecret,
        PaymentSheet.Configuration.Builder(merchantDisplayName = "Reserva de Cancha")
            .customer(customerConfig)
            .allowsDelayedPaymentMethods(true)
            .build()
    )
}

/* ───── Resultado de Stripe ───── */
private fun onPaymentSheetResult(
    result: PaymentSheetResult,
    onSuccess: () -> Unit
) {
    when (result) {
        is PaymentSheetResult.Canceled -> println("Pago cancelado")
        is PaymentSheetResult.Failed   -> println("Pago fallido: ${result.error}")
        is PaymentSheetResult.Completed -> {
            println("Pago completado")
            onSuccess() // 🔥 Redirigir luego de pago exitoso
        }
    }
}
