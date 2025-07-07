package com.flavicox.insurapp.screens

// ─────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.flavicox.insurapp.viewmodel.*
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// Pantalla: ResumeScreen
// Muestra datos de una reserva, crea el PaymentIntent y
// presenta la hoja de pago de Stripe.
// ─────────────────────────────────────────────────────────────
@Composable
fun ResumeScreen(
    navController: NavController,
    reservation: ReservationResponse?
) {

    // ─── Validación inicial ────────────────────────────────
    if (reservation == null) {
        Text(
            "Error: datos de reserva no disponibles",
            modifier = Modifier.padding(24.dp),
            color = Color.Red,
            fontSize = 18.sp
        )
        return
    }

    // ─── ViewModels y contexto ─────────────────────────────
    val context = LocalContext.current
    val authVM:    AuthViewModel    = viewModel(factory = AuthViewModelFactory(context))
    val resumeVM:  ResumeViewModel  = viewModel(factory = ResumeViewModelFactory(context))
    val scope                         = rememberCoroutineScope()

    // ─── Estado local ──────────────────────────────────────
    var loadingIntent  by remember { mutableStateOf(true) }
    var customerConfig by remember { mutableStateOf<PaymentSheet.CustomerConfiguration?>(null) }
    var clientSecret   by remember { mutableStateOf<String?>(null) }
    val errorMessage   by resumeVM.errorMessage.collectAsState()

    // ─── Crear PaymentIntent al entrar ─────────────────────
    LaunchedEffect(reservation.reserveId) {
        val user  = reservation.user
        val field = reservation.field

        // 1️⃣ Petición al backend
        resumeVM.initiatePayment(
            reservationId = reservation.reserveId,
            request = PaymentRequest(
                amount      = reservation.totalPrice.toInt() * 100, // centavos
                clientName  = "${user.name} ${user.surname}",
                productName = "Reserva ${field.typeField} #${field.numberField}",
                phone       = user.phone,
                email       = user.email
            )
        )

        // 2️⃣ Esperar primer resultado válido
        val resp = resumeVM.paymentResult.filterNotNull().first()

        // 3️⃣ Configuración de Stripe PaymentSheet
        customerConfig = PaymentSheet.CustomerConfiguration(
            id = resp.id,
            ephemeralKeySecret = resp.ephemeralSecret
        )
        clientSecret   = resp.clientSecret
        loadingIntent  = false
    }

    // ─── PaymentSheet (Stripe) ─────────────────────────────
    val paymentSheet = remember {
        PaymentSheet.Builder { result ->
            onPaymentSheetResult(result) {
                navController.navigate("${AppScreens.ConfirmationScreen.route}/${reservation.reserveId}") {
                    popUpTo(AppScreens.ResumeScreen.route) { inclusive = true }
                }
            }
        }
    }.build()

    val publishableKey = "pk_test_51RVkIOQpnfpZJWEMPz3o71Oda9MNqCzsa1O7SbymgotbNZKMZUPHF2cgKJyfpetJHbiKbuRLoNhjp0VI2wmaFSJ2005369CYmw"

    // ─── UI ────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // TopBar ------------------------------------------------
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

        // Tarjeta: Usuario -------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Datos del Usuario", fontSize = 18.sp, color = Color(0xFF0277BD))
                Spacer(Modifier.height(8.dp))
                with(reservation.user) {
                    UserDataRow(Icons.Default.Person, "Nombre",  "$name $surname")
                    UserDataRow(Icons.Default.Person, "Correo",  email)
                    UserDataRow(Icons.Default.Person, "Teléfono", phone)
                    UserDataRow(Icons.Default.Person, "DNI",     dni)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Tarjeta: Reserva -------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Detalles de la Reserva", fontSize = 18.sp, color = Color(0xFFF57F17))
                Spacer(Modifier.height(8.dp))
                val field = reservation.field
                Text("Cancha: ${field.typeField} #${field.numberField}", fontSize = 16.sp)
                Text("Fecha: ${reservation.bookingDate}",                 fontSize = 16.sp)
                Text("Hora:  ${reservation.timetableStart} - ${reservation.timetableEnd}", fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Precio total: S/. ${reservation.totalPrice}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Sección Pago -----------------------------------------
        Text("Pago", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        if (loadingIntent) {
            CircularProgressIndicator()
        } else {
            errorMessage?.let { Text("❌ $it", color = Color.Red, fontSize = 14.sp) }

            Button(
                onClick = {
                    PaymentConfiguration.init(context, publishableKey)
                    customerConfig?.let { cfg ->
                        clientSecret?.let { secret ->
                            presentPaymentScreen(paymentSheet, cfg, secret)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = customerConfig != null && clientSecret != null,
                shape    = RoundedCornerShape(8.dp)
            ) {
                Text("Pagar", color = Color.White)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Componente: Fila simple de datos de usuario
// ─────────────────────────────────────────────────────────────
@Composable
private fun UserDataRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp), tint = Color(0xFF0277BD))
        Spacer(Modifier.width(8.dp))
        Text("$label:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(value, fontSize = 14.sp)
    }
}

// ─────────────────────────────────────────────────────────────
// Stripe: Presentar hoja de pago
// ─────────────────────────────────────────────────────────────
private fun presentPaymentScreen(
    sheet: PaymentSheet,
    customerCfg: PaymentSheet.CustomerConfiguration,
    intentSecret: String
) {
    sheet.presentWithPaymentIntent(
        intentSecret,
        PaymentSheet.Configuration.Builder(merchantDisplayName = "Reserva de Cancha")
            .customer(customerCfg)
            .allowsDelayedPaymentMethods(true)
            .build()
    )
}

// ─────────────────────────────────────────────────────────────
// Stripe: Manejar resultado del PaymentSheet
// ─────────────────────────────────────────────────────────────
private fun onPaymentSheetResult(
    result: PaymentSheetResult,
    onSuccess: () -> Unit
) {
    when (result) {
        is PaymentSheetResult.Canceled   -> println("Pago cancelado")
        is PaymentSheetResult.Failed     -> println("Pago fallido: ${result.error}")
        is PaymentSheetResult.Completed  -> {
            println("Pago completado")
            onSuccess()       // Redirige a ConfirmationScreen
        }
    }
}
