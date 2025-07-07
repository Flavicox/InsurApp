package com.flavicox.insurapp.screens

// ─────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.model.*
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.notifications.scheduleNotification
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.flavicox.insurapp.viewmodel.FieldsViewModel
import com.flavicox.insurapp.viewmodel.FieldsViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────
// Pantalla: HorarioScreen
// Muestra horarios disponibles/ocupados de un campo y permite
// crear una reserva.
// ─────────────────────────────────────────────────────────────
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HorarioScreen(
    navController: NavController,
    fieldId: Int,
    fieldTitle: String,
    fieldPrice: Int,
    typeField: String,
    numberField: Int
) {
    // ─── ViewModels y estados globales ──────────────────────
    val context                 = LocalContext.current
    val authVm: AuthViewModel    = viewModel(factory = AuthViewModelFactory(context))
    val fieldsVm: FieldsViewModel= viewModel(factory = FieldsViewModelFactory(context))

    val fullName by authVm.userFullNameFlow.collectAsState(initial = "")
    val horarios  by fieldsVm.availableTimes.collectAsState(emptyList())

    // Día seleccionado (0-4) y fecha formateada
    var selectedDayIndex by remember { mutableStateOf(0) }
    val calendar    = remember { Calendar.getInstance() }
    val dateFormat  = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val selectedDate by remember(calendar) {
        derivedStateOf {
            calendar.apply { setTime(Date()); add(Calendar.DAY_OF_YEAR, selectedDayIndex) }
            dateFormat.format(calendar.time)
        }
    }

    // Al cambiar día: recargar horarios
    LaunchedEffect(selectedDayIndex) {
        fieldsVm.getAvailableTimes(fieldId, selectedDate)
    }

    // Estados para diálogos
    var showConfirm     by remember { mutableStateOf(false) }
    var pendingSlot     by remember { mutableStateOf<TimeSlot?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }
    val coroutineScope  = rememberCoroutineScope()

    // Permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, "Permiso de notificación no concedido", Toast.LENGTH_LONG).show()
    }

    // ─── UI principal ───────────────────────────────────────
    Column(modifier = Modifier.fillMaxSize()) {

        // TopBar con nombre de usuario y acceso a perfil
        TopBarCampos(nombreUsuario = fullName) {
            navController.navigate(AppScreens.ProfileScreen.route)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Título y botón volver
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                BotonRegresar(navController)
                TituloCampo(fieldTitle)
            }

            // Selector de día (hoy + 4)
            DaySelectorClassic(
                selectedIndex = selectedDayIndex,
                onDaySelected = { selectedDayIndex = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de bloques de horario
            BloquesHorario(horarios) { slot ->
                pendingSlot = slot
                showConfirm = true
            }
        }
    }

    // ─── Diálogo: Confirmar reserva ─────────────────────────
    if (showConfirm && pendingSlot != null) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title  = { Text("Confirmar reserva") },
            text   = { Text("¿Estás seguro de reservar a las ${pendingSlot!!.time}?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    coroutineScope.launch {
                        createReservationFlow(
                            navController,
                            context,
                            fieldsVm,
                            pendingSlot!!,
                            selectedDate,
                            fieldId,
                            fieldPrice,
                            typeField,
                            numberField,
                            notificationPermissionLauncher,
                            onError = { msg ->
                                errorMessage = msg
                                showErrorDialog = true
                            }
                        )
                    }
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("No") }
            }
        )
    }

    // ─── Diálogo: Error al crear reserva ────────────────────
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title  = { Text("No se pudo crear la reserva") },
            text   = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) { Text("Aceptar") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Función auxiliar: Flujo completo para crear la reserva
// Incluye notificación local y navegación a ResumeScreen.
// ─────────────────────────────────────────────────────────────
private suspend fun createReservationFlow(
    navController: NavController,
    context: Context,
    fieldsVm: FieldsViewModel,
    slot: TimeSlot,
    selectedDate: String,
    fieldId: Int,
    fieldPrice: Int,
    typeField: String,
    numberField: Int,
    notifPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    onError: (String) -> Unit
) {
    // Extrae la hora de inicio y fin del TimeSlot seleccionado.
    // Si no hay fin explícito, se calcula sumando 1 hora al inicio.
    val (start, end) = slot.time.split(" - ").let { parts ->
        Pair(
            parts.first(),
            parts.getOrNull(1) ?: calculateEndTime(parts.first())
        )
    }
    val totalPrice = fieldPrice.toDouble()

    val request = CreateReserveRequest(
        bookingDate   = selectedDate,
        timetableStart= start,
        timetableEnd  = end,
        totalPrice    = totalPrice,
        fieldId       = fieldId
    )

    try {
        val response = fieldsVm.createReservation(request)
        val fullStartDateTime = "$selectedDate $start"

        // Programar notificación (maneja permiso en Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                scheduleNotification(
                    context     = context,
                    title       = "¡Prepárate para tu partido!",
                    dateTime    = fullStartDateTime,
                    typeField   = typeField,
                    numberField = numberField
                )
            } else {
                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleNotification(
                context     = context,
                title       = "¡Prepárate para tu partido!",
                dateTime    = fullStartDateTime,
                typeField   = typeField,
                numberField = numberField
            )
        }

        // Navegar a resumen
        val json = Uri.encode(Gson().toJson(response))
        navController.navigate("${AppScreens.ResumeScreen.route}/$json")

    } catch (e: Exception) {
        val msg = when {
            e.message?.contains("409") == true ||
                    e.message?.contains("Horario no disponible", true) == true ->
                "Ese horario ya está reservado. Por favor elige otro."
            else -> e.message ?: "Ocurrió un error inesperado"
        }
        onError(msg)
    }
}

// ─────────────────────────────────────────────────────────────
// Utilidad: Calcula hora fin +1 h dado un “HH:mm”
// ─────────────────────────────────────────────────────────────
fun calculateEndTime(start: String): String {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = fmt.parse(start)!!
    return fmt.format(Calendar.getInstance().apply {
        time = date
        add(Calendar.HOUR_OF_DAY, 1)
    }.time)
}

// ─────────────────────────────────────────────────────────────
// Componente: Título del campo
// ─────────────────────────────────────────────────────────────
@Composable
fun TituloCampo(title: String) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    )
}

// ─────────────────────────────────────────────────────────────
// Componente: Botón regresar
// ─────────────────────────────────────────────────────────────
@Composable
fun BotonRegresar(navController: NavController) {
    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = "Volver",
        modifier = Modifier
            .size(32.dp)
            .clickable { navController.popBackStack() }
    )
}

// ─────────────────────────────────────────────────────────────
// Componente: Selector de día (hoy + próximos 4 días)
// ─────────────────────────────────────────────────────────────
@Composable
fun DaySelectorClassic(selectedIndex: Int, onDaySelected: (Int) -> Unit) {
    val calendar  = Calendar.getInstance()
    val fmtDay    = remember { SimpleDateFormat("dd", Locale.getDefault()) }
    val fmtMonth  = remember { SimpleDateFormat("MMM", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(5) { idx ->
            val date = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, idx) }
            val textDay   = fmtDay.format(date.time)
            val textMonth = fmtMonth.format(date.time).uppercase()
            val selected  = idx == selectedIndex

            Column(
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp)
                    .padding(3.dp)
                    .background(
                        color = if (selected) Color(0xFF2ECC71) else Color.White,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(1.dp, Color(0xFF2ECC71), RoundedCornerShape(4.dp))
                    .clickable { onDaySelected(idx) },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = textDay, fontWeight = FontWeight.Bold, color = if (selected) Color.White else Color.Black)
                Text(text = textMonth, fontSize = 12.sp, color = if (selected) Color.White else Color.Black)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Componente: Bloques de horario (reservado / disponible)
// ─────────────────────────────────────────────────────────────
@Composable
fun BloquesHorario(horarios: List<TimeSlot>, onSlotClick: (TimeSlot) -> Unit) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .verticalScroll(scroll)
    ) {
        horarios.forEach { slot ->
            if (slot.reserved) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF2ECC71))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(slot.time, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(1f))
                    Text(slot.client.orEmpty(), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color(0xFF0A0A23))
                        .clickable { onSlotClick(slot) }
                        .padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(slot.time, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
