// com/flavicox/insurapp/screens/ValidateReservationScreen.kt
package com.flavicox.insurapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.ValidateReservationViewModel
import com.flavicox.insurapp.viewmodel.ValidateReservationViewModelFactory

@Composable
fun ValidateReservationScreen(
    navController: NavController,
    reservationId: Int
) {
    val context = LocalContext.current
    val validateVM: ValidateReservationViewModel =
        viewModel(factory = ValidateReservationViewModelFactory(context))

    val reservation by validateVM.reservation.collectAsState()
    val loading by validateVM.loading.collectAsState()
    val error by validateVM.error.collectAsState()
    val validated by validateVM.validationSuccess.collectAsState()

    // Al entrar a la pantalla, cargamos la info vía GET /validate-info?id={id}
    LaunchedEffect(reservationId) {
        validateVM.loadReservation(reservationId)
    }

    // En cuanto validated sea true, navegamos de inmediato a AdminScreen
    LaunchedEffect(validated) {
        if (validated) {
            validateVM.clear()
            navController.navigate(AppScreens.AdminScreen.route) {
                popUpTo(AppScreens.AdminScreen.route) { inclusive = true }
            }
        }
    }

    // Mientras carga (y aún no está validada), mostramos indicador
    if (loading && !validated) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Si hay error, lo mostramos en pantalla
    error?.let {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Error: $it", color = Color.Red)
        }
        return
    }

    // Si todavía no llegó la data o ya navegamos, no dibujamos nada
    val r = reservation ?: return

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
                .padding(bottom = 10.dp)
        )
        {
            BotonRegresar(navController)
            Text("Reserva")
        }

        BotonRegresar(navController)
        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Detalles de la reserva",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))

        ReservationTextField(
            title = "Tipo de campo",
            value = r.field.typeField.replaceFirstChar { it.uppercase() }
        )
        ReservationTextField(
            title = "Número de campo",
            value = "Cancha #${r.field.numberField}"
        )
        ReservationTextField(
            title = "Fecha",
            value = r.bookingDate
        )

        val timeFormatted =
            "${r.timetableStart.removeSuffix(":00")} - ${r.timetableEnd.removeSuffix(":00")}"
        ReservationTextField(
            title = "Hora",
            value = timeFormatted
        )

        val userFullName = "${r.user.name} ${r.user.surname}"
        ReservationTextField(
            title = "Usuario",
            value = userFullName
        )

        ReservationTextField(
            title = "Pago",
            value = "S/. ${r.totalPrice}"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { validateVM.validateReservation(reservationId) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Validar reserva", color = Color.White)
        }
    }
}

@Composable
fun ReservationTextField(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 14.sp, color = Color.Gray)
    }
}
