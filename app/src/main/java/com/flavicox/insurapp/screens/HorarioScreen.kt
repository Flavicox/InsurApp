package com.flavicox.insurapp.screens

import android.annotation.SuppressLint
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.model.TimeSlot
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
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


    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val fullName by viewModel.userFullNameFlow.collectAsState(initial = "")

    LaunchedEffect(selectedDayIndex) {
        fieldsViewModel.getAvailableTimes(fieldId, selectedDate)
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
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp))
            {
                BotonRegresar(navController)
                Titulo(fieldTitle)
            }
            DaySelectorClassic(
                selectedIndex = selectedDayIndex,
                onDaySelected = { selectedDayIndex = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            BloquesHorario(
                fieldId = fieldId,
                horarios = horarios,
                fieldType = typeField,
                fieldNumber = numberField,
                selectedDate = selectedDate,
                navController = navController
            )
        }
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
            .padding(bottom = 10.dp)
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
fun DaySelectorClassic(
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    val formatterDay = SimpleDateFormat("dd", Locale.getDefault())
    val formatterMonth = SimpleDateFormat("MMM", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth(),
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
                    .padding(3.dp)
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
    fieldId: Int,
    horarios: List<TimeSlot>,
    fieldType: String,
    fieldNumber: Int,
    selectedDate: String,
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .verticalScroll(scrollState)
    ) {
        horarios.forEach { slot ->
            if (slot.reserved) {
                // Bloque reservado (fondo verde, nombre del cliente)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF2ECC71))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = slot.time,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = slot.client.orEmpty(),
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Bloque libre: clicable para ir a ResumeScreen con fieldId, fecha y hora
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color(0xFF0A0A23))
                        .clickable {
                            // Navegar sin diálogo, directamente a ResumeScreen
                            navController.navigate(
                                "${AppScreens.ResumeScreen.route}/$fieldId/$selectedDate/${slot.time}"
                            )
                        }
                        .padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = slot.time, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
