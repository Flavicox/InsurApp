package com.flavicox.insurapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.flavicox.insurapp.model.ReservationResponse
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = {
                navController.navigate(AppScreens.ListScreen.route) {
                    popUpTo(0)
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }

            Text("Mi Perfil", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                modifier = Modifier.clickable {
                    navController.navigate(AppScreens.EditProfileScreen.route)
                }
            )
        }

        Text("Información del Usuario", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        ProfileInfo(label = "Nombre y Apellido", value = userFullName)
        ProfileInfo(label = "Teléfono", value = userPhone ?: "No disponible")
        ProfileInfo(label = "Correo", value = userEmail ?: "No disponible")

        Text("Mis Reservas", style = MaterialTheme.typography.titleLarge)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // Altura fija para las reservas
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(reservations) { reservation ->
                        ReservationCard(reservation = reservation, navController = navController)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(AppScreens.ChangePasswordScreen.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cambiar Contraseña", color = Color.White)
        }

        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F54)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión", color = Color.White)
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
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
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

@Composable
fun ReservationCard(reservation: ReservationResponse, navController: NavController) {
    val type = reservation.field?.typeField?.replaceFirstChar { it.uppercase() } ?: "Desconocido"
    val number = reservation.field?.numberField?.toString() ?: "?"
    val price = reservation.totalPrice.toString()
    val start = reservation.timetableStart.dropLast(3)
    val end = reservation.timetableEnd.dropLast(3)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate("${AppScreens.ConfirmationScreen.route}/${reservation.reserveId}")
            },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Reserva #${reservation.reserveId}", fontWeight = FontWeight.Bold)
            Text("Campo $number ($type)", color = Color.Gray)
            Text("Fecha: ${reservation.bookingDate}")
            Text("Hora: $start - $end")
            Text("Total: S/. $price")
        }
    }
}
