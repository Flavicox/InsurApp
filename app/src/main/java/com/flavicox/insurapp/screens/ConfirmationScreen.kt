// File: com/flavicox/insurapp/screens/ConfirmationScreen.kt
package com.flavicox.insurapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.flavicox.insurapp.R
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory

@Composable
fun ConfirmationScreen(navController: NavController) {
    // Recuperar ReservationResponse del SavedStateHandle de la entrada anterior
    val reservation: ReservationResponse? =
        navController
            .previousBackStackEntry
            ?.savedStateHandle
            ?.get<ReservationResponse>("reservation")

    if (reservation == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val r = reservation
    val typeField      = r.field.typeField.replaceFirstChar { it.uppercase() }
    val numberField    = r.field.numberField
    val bookingDate    = r.bookingDate
    val timetableStart = r.timetableStart.removeSuffix(":00")
    val timetableEnd   = r.timetableEnd.removeSuffix(":00")
    val qrUrl          = r.qrUrl

    //Para guardar el nombre de usuario del TopBarCampos (HEADER)
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val fullName by viewModel.userFullNameFlow.collectAsState(initial = "")

    Column {
        TopBarCampos(
            nombreUsuario = fullName,
            onProfileClick = {
                navController.navigate(AppScreens.ProfileScreen.route)
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box (modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp))
        {
            BotonRegresar(navController)
            Titulo("Reserva Confirmada")
        }
        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "¡Tu reserva está confirmada!",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Gracias por reservar con nosotros. Aquí están los detalles:",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        ReservationDetailItem(Icons.Default.Tag, "Tipo de Campo", typeField)
        ReservationDetailItem(Icons.Default.Tag, "Número de Campo", "Campo #$numberField")
        ReservationDetailItem(Icons.Default.CalendarToday, "Fecha", bookingDate)
        ReservationDetailItem(Icons.Default.Schedule, "Hora", "$timetableStart - $timetableEnd")

        Spacer(modifier = Modifier.height(24.dp))

        // Cargar y mostrar el QR desde la URL
        AsyncImage(
            model = qrUrl,
            contentDescription = "Código QR de la reserva",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.popBackStack(route = AppScreens.ListScreen.route, inclusive = false)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Ver Reservas", color = Color.White)
        }
    }
}

@Composable
fun ReservationDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.Black,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 14.sp)
        }
    }
}
