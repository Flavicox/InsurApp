package com.flavicox.insurapp.screens

// ─────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────
import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.R
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.flavicox.insurapp.viewmodel.FieldsViewModel
import com.flavicox.insurapp.viewmodel.FieldsViewModelFactory

// ─────────────────────────────────────────────────────────────
// Pantalla principal de Listado de Campos Deportivos
// Esta pantalla muestra todos los campos disponibles y permite
// acceder al proceso de reserva.
// ─────────────────────────────────────────────────────────────
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListScreen(navController: NavController) {
    Scaffold {
        ListBodyComponent(navController)
    }
}

// ─────────────────────────────────────────────────────────────
// Cuerpo principal de la pantalla de campos
// Muestra el nombre del usuario, los campos y controla la lógica
// de carga y navegación.
// ─────────────────────────────────────────────────────────────
@Composable
fun ListBodyComponent(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val fieldsViewModel: FieldsViewModel = viewModel(factory = FieldsViewModelFactory(context))

    val fullName by viewModel.userFullNameFlow.collectAsState(initial = "")
    val campos by fieldsViewModel.fields.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Cargar los campos disponibles al iniciar
    LaunchedEffect(Unit) {
        fieldsViewModel.loadFields()
    }

    Column {
        TopBarCampos(
            nombreUsuario = fullName,
            onProfileClick = {
                navController.navigate(AppScreens.ProfileScreen.route)
            }
        )

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Titulo("Seleccionar Campo")

            campos.forEach { campo ->
                val nombre = "${campo.typeField} - Campo ${campo.numberField}"
                val precio = "S/. ${campo.price}"

                CampoCard(
                    nombre = nombre,
                    precio = precio,
                    onReservarClick = {
                        navController.navigate(
                            "${AppScreens.HorarioScreen.route}/${campo.fieldId}/${campo.price}/${campo.typeField}/${campo.numberField}"
                        )
                    }
                )
            }
        }
    }

    // Diálogo de cierre de sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
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

// ─────────────────────────────────────────────────────────────
// Componente: Título principal
// ─────────────────────────────────────────────────────────────
@Composable
fun Titulo(texto: String) {
    Text(
        text = texto,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    )
}

// ─────────────────────────────────────────────────────────────
// Componente: Barra superior con nombre y botón de perfil
// ─────────────────────────────────────────────────────────────
@Composable
fun TopBarCampos(
    nombreUsuario: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo La Insurgencia",
            modifier = Modifier.size(36.dp)
        )

        Text(
            text = "Bienvenido, $nombreUsuario!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Icon(
            painter = painterResource(id = R.drawable.user_icon),
            contentDescription = "Ver perfil",
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp)
                .clickable { onProfileClick() }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Componente: Tarjeta de Campo Deportivo
// Muestra datos del campo y permite iniciar la reserva
// ─────────────────────────────────────────────────────────────
@Composable
fun CampoCard(
    nombre: String,
    precio: String,
    onReservarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Duración fija
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.schedule),
                    contentDescription = "Duración",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "60 min")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título del campo
            Text(
                text = nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp))

            // Precio + Botón Reservar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Precio: ", fontSize = 14.sp)
                Text(text = precio, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onReservarClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reservar")
                }
            }
        }
    }
}
