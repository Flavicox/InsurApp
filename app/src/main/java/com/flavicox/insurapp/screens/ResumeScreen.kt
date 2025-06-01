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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.flavicox.insurapp.navigation.AppScreens

@Composable
fun ResumeScreen(
    navController: NavController,
    typeField: String,
    numberField: Int,
    selectedDate: String,
    selectedTime: String,
    price: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current  //ESTE ES PARA EL PREVIEW
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    //val userFullName by authViewModel.userFullNameFlow.collectAsState(initial = "")   PENSE QUE SERVIRIA PARA INTEGRAR

    // Datos imulados
    val phone = "987654321"
    val email = "usuario@example.com"
    val userFullName = "Jose Luyo"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Volver",
            modifier = Modifier
                .size(32.dp)
                .clickable { navController.popBackStack() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Resumen de Reserva",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(text = "Detalles de la Reserva", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(15.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Tipo de Campo", fontSize = 14.sp, color = Color.Gray)
                Text(typeField)  //VIENE DEL HORARIOSCREEN
            }
            Column (Modifier.size(width = 200.dp, height = 30.dp).padding(start = 40.dp)){
                Text("Número de Campo", fontSize = 14.sp, color = Color.Gray)
                Text("Campo $numberField") //VIENE DEL HORARIOCREEN
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Fecha", fontSize = 14.sp, color = Color.Gray)
                Text(selectedDate)  //VIENE DEL HORARIO SCREEN
            }
            Column (Modifier.size(width = 200.dp, height = 30.dp).padding(start = 40.dp)){
                Text("Hora", fontSize = 14.sp, color = Color.Gray)
                Text(selectedTime)  // VIENE DEL HORARIO SCREEN
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(text = "Información del Usuario", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column {
            Text("Nombre y Apellido", fontSize = 14.sp, color = Color.Gray)
            Text(userFullName)  // METER MAGIA
            Spacer(modifier = Modifier.height(8.dp))
            Text("Teléfono", fontSize = 14.sp, color = Color.Gray)
            Text(phone)  // METER MAGIA
            Spacer(modifier = Modifier.height(8.dp))
            Text("Correo", fontSize = 14.sp, color = Color.Gray)
            Text(email) // METER MAGIA
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Opciones de Pago", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {navController.navigate("${AppScreens.PaymentScreen.route}/$typeField - Campo $numberField/$selectedDate/$selectedTime/$price/false")},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Pagar 50%", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("${AppScreens.PaymentScreen.route}/$typeField - Campo $numberField/$selectedDate/$selectedTime/$price/${false}")},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000B3E)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Pagar 100%", color = Color.White)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewResumeScreen() {
    ResumeScreen(
        navController = NavController(LocalContext.current), // simulado, no funcional
        typeField = "Fútbol 11",
        numberField = 3,
        selectedDate = "15 de Julio, 2024",
        selectedTime = "18:00 - 20:00",
        price = 100
    )
}