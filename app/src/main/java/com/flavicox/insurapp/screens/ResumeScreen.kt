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
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.flavicox.insurapp.viewmodel.FieldDetailViewModel
import com.flavicox.insurapp.viewmodel.FieldDetailViewModelFactory

@Composable
fun ResumeScreen(
    navController: NavController,
    fieldId: Int,
    selectedDate: String,
    selectedTime: String
) {
    val context = LocalContext.current

    // ViewModel para detalles del campo
    val fieldDetailVM: FieldDetailViewModel =
        viewModel(factory = FieldDetailViewModelFactory(context))
    val fieldDetail by fieldDetailVM.fieldDetail.collectAsState()

    // ViewModel de autenticación para obtener nombre, teléfono y correo
    val authViewModel: AuthViewModel =
        viewModel(factory = AuthViewModelFactory(context))
    val userFullName by authViewModel.userFullNameFlow.collectAsState(initial = "")
    val userPhone    by authViewModel.userPhoneFlow.collectAsState(initial = "")
    val userEmail    by authViewModel.userEmailFlow.collectAsState(initial = "")

    // Cargar datos del campo al iniciarse
    LaunchedEffect(fieldId) {
        fieldDetailVM.loadField(fieldId)
    }

    Column() {
        TopBarCampos(
            nombreUsuario = userFullName,
            onProfileClick = {
                navController.navigate(AppScreens.ProfileScreen.route)
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ){
            Box (modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp))
            {
                BotonRegresar(navController)
                Titulo("Resumen de Reserva")
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(text = "Detalles de la Reserva", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(15.dp))

            // Si aún no llegó fieldDetail, mostramos placeholders
            val typeFieldText   = fieldDetail?.typeField ?: "Cargando..."
            val numberFieldText = fieldDetail?.numberField ?: 0
            val priceText       = fieldDetail?.price ?: 0

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Tipo de Campo", fontSize = 14.sp, color = Color.Gray)
                    Text(typeFieldText)
                }
                Column(
                    Modifier
                        .width(200.dp)
                        .height(30.dp)
                        .padding(start = 40.dp)
                ) {
                    Text("Número de Campo", fontSize = 14.sp, color = Color.Gray)
                    Text("Campo $numberFieldText")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Fecha", fontSize = 14.sp, color = Color.Gray)
                    Text(selectedDate)
                }
                Column(
                    Modifier
                        .width(200.dp)
                        .height(30.dp)
                        .padding(start = 40.dp)
                ) {
                    Text("Hora", fontSize = 14.sp, color = Color.Gray)
                    Text(selectedTime)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Column {
                    Text("Precio", fontSize = 14.sp, color = Color.Gray)
                    Text("S/. $priceText")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(text = "Información del Usuario", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text("Nombre y Apellido", fontSize = 14.sp, color = Color.Gray)
                Text(userFullName)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Teléfono", fontSize = 14.sp, color = Color.Gray)
                Text(userPhone ?: "—")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Correo", fontSize = 14.sp, color = Color.Gray)
                Text(userEmail ?: "—")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Opciones de Pago", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Botón “Pagar 50%” → ahora pasamos 5 parámetros
            Button(
                onClick = {
                    navController.navigate(
                        "${AppScreens.PayScreen.route}/$fieldId/$selectedDate/$selectedTime/$priceText/true"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pagar 50%", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón “Pagar 100%” → idem
            Button(
                onClick = {
                    navController.navigate(
                        "${AppScreens.PayScreen.route}/$fieldId/$selectedDate/$selectedTime/$priceText/false"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000B3E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Pagar 100%", color = Color.White)
            }


        }

    }
}
