package com.flavicox.insurapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.flavicox.insurapp.R

@Composable
fun ConfirmationScreen(navController: NavController) {

    //val imageBytes = Base64.decode(qrBase64, Base64.DEFAULT)
    //val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    //Image(bitmap = bitmap.asImageBitmap(), contentDescription = "QR")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reserva Confirmada",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "¡Tu reserva está confirmada!",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Gracias por reservar con nosotros. Aquí están los detalles de tu reserva.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Detalle de reserva con íconos
        ReservationDetailItem(Icons.Default.CheckCircle, "Tipo de Campo", "Cancha de Fútbol")
        ReservationDetailItem(Icons.Default.Tag, "Número de Campo", "Campo #3")
        ReservationDetailItem(Icons.Default.CalendarToday, "Fecha", "20 de Julio de 2024")
        ReservationDetailItem(Icons.Default.Schedule, "Hora", "10:00 AM - 11:00 AM")
        ReservationDetailItem(Icons.Default.Person, "Nombre y Apellido", "Carlos Mendoza")
        ReservationDetailItem(Icons.Default.Phone, "Número de Teléfono", "+52 55 1234 5678")


        Spacer(modifier = Modifier.height(24.dp))

        // QR DE RERSERVA
        Image(
            painter = painterResource(id = R.drawable.qr_icon), // COLOCAR EL QR QUE VIENE DEL BACKEND
            contentDescription = "Código QR",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón
        Button(
            onClick = { /* Navegar a ver reservas */ }, //LLEVA A VER RESERVAS
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

@Preview(showBackground = true)
@Composable
fun PreviewConfirmationScreen() {
    ConfirmationScreen(
        navController = NavController(LocalContext.current) // solo para preview
    )
}