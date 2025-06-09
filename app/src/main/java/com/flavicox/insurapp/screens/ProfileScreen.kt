package com.flavicox.insurapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.flavicox.insurapp.viewmodel.MyReservationsViewModel
import com.flavicox.insurapp.viewmodel.MyReservationsViewModelFactory

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val myReservationsVM: MyReservationsViewModel = viewModel(factory = MyReservationsViewModelFactory(context))

    val userFullName by viewModel.userFullNameFlow.collectAsState(initial = "")
    val userPhone by viewModel.userPhoneFlow.collectAsState(initial = "")
    val userEmail by viewModel.userEmailFlow.collectAsState(initial = "")

    val reservations by myReservationsVM.reservations.collectAsState()
    val loading by myReservationsVM.loading.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        myReservationsVM.loadMyReservations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            BotonRegresar(navController)
            Titulo("Mi Perfil")
        }

        Spacer(modifier = Modifier.height(32.dp))
        ProfileInfo(label = "Nombre y Apellido", value = userFullName)
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfo(label = "Teléfono", value = userPhone ?: "No disponible")
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfo(label = "Correo", value = userEmail ?: "No disponible")

        Spacer(modifier = Modifier.height(24.dp))
        Text("Mis Reservas", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (reservations.isEmpty()) {
                Text("No tienes reservas registradas", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(reservations) { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Campo: ${r.fieldId.typeField} #${r.fieldId.numberField}")
                                Text("Fecha: ${r.bookingDate}")
                                Text("Hora: ${r.timetableStart} - ${r.timetableEnd}")
                                Text("Total: S/. ${r.totalPrice}")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Cerrar sesión", color = Color.White)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    navController.navigate(AppScreens.LoginScreen.route) {
                        popUpTo(0)
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProfileInfo(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
