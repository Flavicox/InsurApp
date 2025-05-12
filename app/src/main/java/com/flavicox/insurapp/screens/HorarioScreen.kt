package com.flavicox.insurapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.R
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.FieldsViewModel
import com.flavicox.insurapp.viewmodel.FieldsViewModelFactory
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
    val horarios by fieldsViewModel.availableTimes.collectAsState()
    var selectedDayIndex by remember { mutableStateOf(0) }

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, selectedDayIndex)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedDate = dateFormat.format(calendar.time)

    LaunchedEffect(selectedDayIndex) {
        fieldsViewModel.getAvailableTimes(fieldId, selectedDate)
    }

    Column {
        HeaderCampos()
        TituloCampo(fieldTitle)
        BotonRegresar(navController)
        DaySelectorClassic(
            selectedIndex = selectedDayIndex,
            onDaySelected = { selectedDayIndex = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        BloquesHorario(
            horarios = horarios,
            fieldType = fieldTitle.split(" - ")[0],
            fieldNumber = fieldTitle.split(" - ")[1].split(" ")[1].toInt(),
            selectedDate = selectedDate,
            navController = navController
        )
    }
}

@Composable
fun HeaderCampos(nombreUsuario: String = "Jose Luyo") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            contentDescription = "Perfil",
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp)
        )
    }
}

@Composable
fun TituloCampo(title: String) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun BotonRegresar(navController: NavController) {
    TextButton(onClick = { navController.popBackStack() }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Regresar",
            tint = Color(0xFF2ECC71)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Regresar",
            color = Color(0xFF2ECC71),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DaySelectorClassic(
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    val formatterDay = SimpleDateFormat("dd", Locale.getDefault())
    val formatterMonth = SimpleDateFormat("MMM", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(5) { index ->
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.add(Calendar.DAY_OF_YEAR, index)

            val dayText = formatterDay.format(dayCalendar.time)
            val monthText = formatterMonth.format(dayCalendar.time).uppercase()

            val isSelected = selectedIndex == index

            Column(
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp)
                    .padding(4.dp)
                    .background(
                        color = if (isSelected) Color(0xFF2ECC71) else Color.White,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF2ECC71),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onDaySelected(index) },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayText,
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = monthText,
                    color = if (isSelected) Color.White else Color.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun BloquesHorario(
    horarios: List<String>,
    fieldType: String,
    fieldNumber: Int,
    selectedDate: String,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    var horarioSeleccionado by remember { mutableStateOf<String?>(null) }

    // Modal
    if (horarioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { horarioSeleccionado = null },
            title = { Text("Confirmar Reserva") },
            text = {
                Text("¿Deseas hacer la reserva del Campo $fieldNumber de $fieldType el día $selectedDate en el horario de ${horarioSeleccionado}?")
            },
            confirmButton = {
                TextButton(onClick = {
//                    navController.navigate(
//                        "${AppScreens.ResumenReservaScreen.route}/$fieldType/$fieldNumber/$selectedDate/${horarioSeleccionado}"
//                    )
                    horarioSeleccionado = null
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { horarioSeleccionado = null }) {
                    Text("No")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(600.dp)
            .verticalScroll(scrollState)
    ) {
        horarios.forEach { hora ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, Color(0xFF0A0A23))
                    .clickable { horarioSeleccionado = hora },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hora,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    fontSize = 14.sp
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

