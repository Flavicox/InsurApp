package com.flavicox.insurapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@Composable
fun ValidateReservationScreen(
    navController: NavController,
    fieldType: String,
    fieldNumber: Int,
    date: String,
    time: String,
    userFullName: String,
    paymentPercentage: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reserva",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 30.dp)
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Detalles de la reserva",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))

        ReservationTextField("Tipo de campo", fieldType)
        ReservationTextField("Número de campo", "Cancha #$fieldNumber")
        ReservationTextField("Fecha", date)
        ReservationTextField("Hora", time)
        ReservationTextField("Usuario", "Juan Perez") // Simulado, vendrá del backend
        ReservationTextField("Pago", paymentPercentage)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // TODO: Validar la reserva al hacer clic
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Validar reserva", color = Color.White)
        }
    }
}

@Composable
fun ReservationTextField(title: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 14.sp, color = Color.Gray)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewValidateReservationScreen() {
    ValidateReservationScreen(
        navController = rememberNavController(),
        fieldType = "Cancha de Fútbol",
        fieldNumber = 3,
        date = "2024-07-20",
        time = "18:00 - 19:00",
        userFullName = "Juan Perez",
        paymentPercentage = "50%"
    )
}
