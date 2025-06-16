package com.flavicox.insurapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavController
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.navigation.AppScreens
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.PaymentRequest
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.flavicox.insurapp.viewmodel.ResumeViewModel
import com.flavicox.insurapp.viewmodel.ResumeViewModelFactory
import kotlinx.coroutines.launch

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

    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val userFullName by authViewModel.userFullNameFlow.collectAsState(initial = "")

    val user = reservation.user
    val field = reservation.field

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // TopBar
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

        // Sección: Usuario
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Datos del Usuario", fontSize = 18.sp, color = Color(0xFF0277BD))
                Spacer(Modifier.height(8.dp))
                UserDataRow(icon = Icons.Default.Person, label = "Nombre", value = "${user.name} ${user.surname}")
                UserDataRow(icon = Icons.Default.Person, label = "Correo", value = user.email)
                UserDataRow(icon = Icons.Default.Person, label = "Teléfono", value = user.phone)
                UserDataRow(icon = Icons.Default.Person, label = "DNI", value = user.dni)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Sección: Reserva
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Detalles de la Reserva", fontSize = 18.sp, color = Color(0xFFF57F17))
                Spacer(Modifier.height(8.dp))
                Text("Cancha: ${field.typeField} #${field.numberField}", fontSize = 16.sp)
                Text("Fecha: ${reservation.bookingDate}", fontSize = 16.sp)
                Text("Hora: ${reservation.timetableStart} - ${reservation.timetableEnd}", fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                Text("Precio total: S/. ${reservation.totalPrice}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Sección: Pago
        Text("Opciones de Pago", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        val resumeViewModel: ResumeViewModel = viewModel(factory = ResumeViewModelFactory(context))

        PaymentButtons(
            reservation = reservation,
            resumeViewModel = resumeViewModel
        )
    }
}

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

@Composable
private fun PaymentButtons(
    reservation: ReservationResponse,
    resumeViewModel: ResumeViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    val paymentResult by resumeViewModel.paymentResult.collectAsState()
    val errorMessage by resumeViewModel.errorMessage.collectAsState()

    val user = reservation.user
    val field = reservation.field
    val price = reservation.totalPrice.toInt() // Ej. 80
    val reservationId = reservation.reserveId // ⚠️ ID real de la reserva

    Column {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        }

        errorMessage?.let {
            Text("❌ Error: $it", color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
        }

        paymentResult?.let {
            Text("✅ Pago iniciado con éxito", color = Color(0xFF2ECC71), fontSize = 14.sp)
            Text("🔑 PaymentIntent: ${it.paymentIntentId}", fontSize = 12.sp)
        }

        // Botón para pagar 50%
        Button(
            onClick = {
                coroutineScope.launch {
                    loading = true
                    try {
                        resumeViewModel.initiatePayment(
                            reservationId = reservationId,
                            request = PaymentRequest(
                                amount = (price / 2) * 100, // <-- cambio importante
                                clientName = "${user.name} ${user.surname}",
                                productName = "Reserva ${field.typeField} #${field.numberField}",
                                phone = user.phone,
                                email = user.email
                            )
                        )
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Pagar 50%", color = Color.White)
        }

        // Botón para pagar 100%
        Button(
            onClick = {
                coroutineScope.launch {
                    loading = true
                    try {
                        resumeViewModel.initiatePayment(
                            reservationId = reservationId,
                            request = PaymentRequest(
                                amount = price * 100, // <-- cambio importante
                                clientName = "${user.name} ${user.surname}",
                                productName = "Reserva ${field.typeField} #${field.numberField}",
                                phone = user.phone,
                                email = user.email
                            )
                        )
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000B3E)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Pagar 100%", color = Color.White)
        }
    }
}
