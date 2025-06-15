package com.flavicox.insurapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.R
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PaymentScreen(
    navController: NavController,
    fieldLabel: String,
    date: String,
    time: String,
    price: Int,  //VIENE DESDE EL LISTSCREEN Y DE ACUERDO AL BOTON SE CALCULA EL 100% O 50%
    isHalfPayment: Boolean
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val userFullName by authViewModel.userFullNameFlow.collectAsState(initial = "")

    val finalAmount = if (isHalfPayment) price / 2 else price    //  50%

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Box (modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp))
        {
            BotonRegresar(navController)
            Titulo("Pagar Reserva")
        }
        Spacer(modifier = Modifier.height(30.dp))

        Spacer(modifier = Modifier.height(28.dp))

        Text(text = fieldLabel, fontSize = 16.sp, modifier = Modifier.padding(bottom = 5.dp))
        Text(text = "Fecha: $date", fontSize = 16.sp, modifier = Modifier.padding(bottom = 5.dp))
        Text(text = "Hora: $time", fontSize = 16.sp, modifier = Modifier.padding(bottom = 5.dp))
        Text(text = "Precio: S/. $finalAmount", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Nombre: $userFullName", fontSize = 16.sp, modifier = Modifier.padding(bottom = 5.dp))
        Text(text = "Teléfono: 987654321", fontSize = 16.sp, modifier = Modifier.padding(bottom = 5.dp)) // reemplazar por real - FLAVIO METE MAGIA
        Text(text = "Correo: usuario@email.com", fontSize = 16.sp, modifier = Modifier.padding(bottom = 5.dp)) // reemplazar por real - FLAVIO METE MAGIA

        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.paypal),
            contentDescription = "PayPal",
            modifier = Modifier
                .height(80.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* Lógica futura de PayPal - URL para que se abra el navegador para pagar con paypal*/ },
            modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Pagar con PayPal", color = Color.White)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewPaymentScreen() {
    PaymentScreen(
        navController = NavController(LocalContext.current), // Simulado, no funcional
        fieldLabel = "Campo: Fútbol - Campo 1",
        date = "05 MAY",
        time = "08:00 pm",
        price = 100,
        isHalfPayment = false
    )
}