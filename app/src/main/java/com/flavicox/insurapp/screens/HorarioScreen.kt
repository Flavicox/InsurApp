package com.flavicox.insurapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.flavicox.insurapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HorarioScreen(navController: NavController){
    Scaffold {
        HorarioBodyComponent(navController)
    }
}

@Composable
fun HorarioBodyComponent(navController: NavController){
    Column {
        HeaderCampos()
        TituloCampo()
        BotonRegresar(navController)
        var selectedDayIndex by remember { mutableStateOf(0) }

        DaySelectorClassic(
            selectedIndex = selectedDayIndex,
            onDaySelected = { selectedDayIndex = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BloquesHorario()
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
            painter = painterResource(id = R.drawable.user_icon), // usa el nombre real de tu drawable
            contentDescription = "Perfil",
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp)
        )
    }
}

@Composable
fun TituloCampo() {
    Text(
        text = "Futbol - Campo 1",
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
    TextButton(
        onClick = { navController.popBackStack() },
    ) {
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
fun BloquesHorario() {
    val bloques = listOf(
        "08:00 - 09:00",
        "09:00 - 10:00",
        "11:00 - 12:00"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        bloques.forEach { hora ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, Color(0xFF0A0A23))
            ) {
                Text(
                    text = hora,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                        .align(Alignment.CenterVertically),
                    fontSize = 14.sp
                )
                // Columna vacía a la derecha (puedes llenarla luego)
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun PreviewBloquesHorario() {
    BloquesHorario()
}