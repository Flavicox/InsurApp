package com.flavicox.insurapp.screens

// ─────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.flavicox.insurapp.model.ReservationByIdResponse
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory

// ─────────────────────────────────────────────────────────────
// Pantalla: ConfirmationScreen
// Muestra la reserva confirmada, los detalles y el código QR
// para validación futura.
// ─────────────────────────────────────────────────────────────
@Composable
fun ConfirmationScreen(navController: NavController, reservationId: Int) {

    // ─── ViewModel y estado ─────────────────────────────────
    val context = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val fullName by vm.userFullNameFlow.collectAsState(initial = "")

    var reservation by remember { mutableStateOf<ReservationByIdResponse?>(null) }
    var loading     by remember { mutableStateOf(true) }
    var error       by remember { mutableStateOf<String?>(null) }

    // ─── Consultar reserva al iniciar ───────────────────────
    LaunchedEffect(reservationId) {
        vm.fetchReservationById(reservationId) { result ->
            reservation = result
            if (result == null) error = "No se pudo obtener la reserva"
            loading = false
        }
    }

    // ─── TopBar (nombre usuario y perfil) ───────────────────
    TopBarCampos(
        nombreUsuario = fullName,
        onProfileClick = { navController.navigate(AppScreens.ProfileScreen.route) }
    )

    // ─── Contenido principal ────────────────────────────────
    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error!!, color = Color.Red)
        }

        reservation != null -> {
            val r = reservation!!
            val typeField   = r.field.typeField.replaceFirstChar { it.uppercase() }
            val numberField = r.field.numberField
            val timetable   = "${r.timetableStart.removeSuffix(":00")} - ${r.timetableEnd.removeSuffix(":00")}"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ─── Encabezado ───────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    IconButton(onClick = {
                        navController.navigate(AppScreens.ProfileScreen.route) {
                            popUpTo(0) // limpia la pila si deseas evitar backstack anterior
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }

                Spacer(Modifier.height(15.dp))

                Text(
                    text = "¡Tu reserva está confirmada!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(15.dp))

                Text(
                    text = "Gracias por reservar con nosotros. Aquí están los detalles:",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(24.dp))

                // ─── Detalles de reserva ─────────────────────
                ReservationDetailItem(Icons.Default.Tag, "Tipo de Campo",  typeField)
                ReservationDetailItem(Icons.Default.Tag, "Número de Campo", "Campo #$numberField")
                ReservationDetailItem(Icons.Default.CalendarToday, "Fecha", r.bookingDate)
                ReservationDetailItem(Icons.Default.Schedule, "Hora", timetable)

                Spacer(Modifier.height(24.dp))

                // ─── Código QR ───────────────────────────────
                AsyncImage(
                    model = r.qrUrl,
                    contentDescription = "Código QR",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Spacer(Modifier.height(24.dp))

                // ─── Botón Ver Reservas ──────────────────────
                Button(
                    onClick = {
                        navController.navigate(AppScreens.ProfileScreen.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ver Reservas", color = Color.White)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Componente: Detalle individual de la reserva
// ─────────────────────────────────────────────────────────────
@Composable
fun ReservationDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Color.Black, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp
            )
        }
    }
}
