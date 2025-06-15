package com.flavicox.insurapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.flavicox.insurapp.viewmodel.FieldDetailViewModel
import com.flavicox.insurapp.viewmodel.FieldDetailViewModelFactory

@Composable
fun ResumeScreen(navController: NavController, reservation: ReservationResponse?) {
    if (reservation == null) {
        Text("Error: datos de reserva no disponibles")
        return
    }

    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val userFullName by authViewModel.userFullNameFlow.collectAsState(initial = "")

    // Extraer datos
    val fieldInfo = reservation.fieldId
    val fecha = reservation.bookingDate
    val horaInicio = reservation.timetableStart
    val horaFin = reservation.timetableEnd
    val price = reservation.totalPrice

    Column(modifier = Modifier.fillMaxSize()) {
        // TopBar con botón atrás
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(16.dp))
            Text("Resumen de Reserva", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Cancha: ${fieldInfo.typeField} #${fieldInfo.numberField}", fontSize = 16.sp)
            Text("Fecha: $fecha", fontSize = 16.sp)
            Text("Horario: $horaInicio - $horaFin", fontSize = 16.sp)
            Text("Total: S/. $price", fontSize = 16.sp)

            Spacer(Modifier.height(32.dp))

            Text(text = "Opciones de Pago", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate(
                        "${AppScreens.PayScreen.route}/" +
                                "${fieldInfo.fieldId}/$fecha/$horaInicio/${price.toInt()}/true"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pagar 50%", color = Color.White)
            }

            Button(
                onClick = {
                    navController.navigate(
                        "${AppScreens.PayScreen.route}/" +
                                "${fieldInfo.fieldId}/$fecha/$horaInicio/${price.toInt()}/false"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000B3E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pagar 100%", color = Color.White)
            }
        }
    }
}