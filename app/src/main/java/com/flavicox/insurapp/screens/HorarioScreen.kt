package com.flavicox.insurapp.screens

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.flavicox.insurapp.model.CreateReserveRequest
import com.flavicox.insurapp.model.TimeSlot
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
    val context = LocalContext.current
    val fieldsViewModel: FieldsViewModel = viewModel(factory = FieldsViewModelFactory(context))
    val horarios by fieldsViewModel.availableTimes.collectAsState(emptyList())

    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val fullName by viewModel.userFullNameFlow.collectAsState(initial = "")

    var selectedDayIndex by remember { mutableStateOf(0) }
    val calendar = remember { Calendar.getInstance() }
    LaunchedEffect(selectedDayIndex) {
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, selectedDayIndex)
    }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedDate by remember(calendar) { mutableStateOf(dateFormat.format(calendar.time)) }

    LaunchedEffect(selectedDayIndex) {
        fieldsViewModel.getAvailableTimes(fieldId, selectedDate)
    }

    var showConfirm by remember { mutableStateOf(false) }
    var pendingSlot by remember { mutableStateOf<TimeSlot?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permiso de notificación no concedido", Toast.LENGTH_LONG).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarCampos(nombreUsuario = fullName) {
            navController.navigate(AppScreens.ProfileScreen.route)
        }
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                BotonRegresar(navController)
                TituloCampo(fieldTitle)
            }
            DaySelectorClassic(selectedIndex = selectedDayIndex) { selectedDayIndex = it }
            Spacer(modifier = Modifier.height(16.dp))
            BloquesHorario(horarios = horarios) { slot ->
                pendingSlot = slot
                showConfirm = true
            }
        }
    }

    if (showConfirm && pendingSlot != null) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirmar reserva") },
            text = { Text("¿Estás seguro de reservar a las ${pendingSlot!!.time}?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    coroutineScope.launch {
                        val parts = pendingSlot!!.time.split(" - ")
                        val start = parts[0]
                        val end = parts.getOrNull(1) ?: calculateEndTime(start)
                        val totalPrice = fieldPrice.toDouble()
                        val request = CreateReserveRequest(
                            bookingDate = selectedDate,
                            timetableStart = start,
                            timetableEnd = end,
                            totalPrice = totalPrice,
                            fieldId = fieldId
                        )
                        try {
                            val response = fieldsViewModel.createReservation(request)

                            val fullStartDateTime = "$selectedDate $start"

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    scheduleNotification(
                                        context = context,
                                        title = "¡Prepárate para tu partido!",
                                        dateTime = fullStartDateTime,
                                        typeField = typeField,
                                        numberField = numberField
                                    )

                                } else {
                                    notificationPermissionLauncher.launch(
                                        android.Manifest.permission.POST_NOTIFICATIONS
                                    )
                                }
                            } else {
                                scheduleNotification(
                                    context = context,
                                    title = "¡Prepárate para tu partido!",
                                    dateTime = fullStartDateTime,
                                    typeField = typeField,
                                    numberField = numberField
                                )

                            }

                            val json = Uri.encode(Gson().toJson(response))
                            navController.navigate("${AppScreens.ResumeScreen.route}/$json")
                        } catch (e: Exception) {
                            errorMessage = when {
                                e.message?.contains("409") == true || e.message?.contains("Horario no disponible", true) == true ->
                                    "Ese horario ya está reservado. Por favor elige otro."
                                else -> e.message ?: "Ocurrió un error inesperado"
                            }
                            showErrorDialog = true
                        }
                    }
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("No") }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            },
            title = { Text("No se pudo crear la reserva") },
            text = { Text(errorMessage) }
        )
    }
}

fun calculateEndTime(start: String): String {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val parsed = fmt.parse(start)!!
    return fmt.format(Calendar.getInstance().apply {
        time = parsed
        add(Calendar.HOUR_OF_DAY, 1)
    }.time)
}

@Composable
fun TituloCampo(title: String) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    )
}

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

@Composable
fun DaySelectorClassic(selectedIndex: Int, onDaySelected: (Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val fmtDay = SimpleDateFormat("dd", Locale.getDefault())
    val fmtMonth = SimpleDateFormat("MMM", Locale.getDefault())
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        repeat(5) { index ->
            val dayCal = (calendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, index) }
            val textDay = fmtDay.format(dayCal.time)
            val textMonth = fmtMonth.format(dayCal.time).uppercase()
            val isSel = index == selectedIndex
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp)
                    .padding(3.dp)
                    .background(color = if (isSel) Color(0xFF2ECC71) else Color.White, shape = RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFF2ECC71), RoundedCornerShape(4.dp))
                    .clickable { onDaySelected(index) },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = textDay, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black)
                Text(text = textMonth, fontSize = 12.sp, color = if (isSel) Color.White else Color.Black)
            }
        }
    }
}

@Composable
fun BloquesHorario(horarios: List<TimeSlot>, onSlotClick: (TimeSlot) -> Unit) {
    val scroll = rememberScrollState()
    Column(modifier = Modifier.fillMaxWidth().height(600.dp).verticalScroll(scroll)) {
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