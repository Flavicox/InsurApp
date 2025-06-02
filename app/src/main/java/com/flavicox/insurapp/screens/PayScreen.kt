// File: com/flavicox/insurapp/screens/PayScreen.kt
package com.flavicox.insurapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.ReservationViewModel
import com.flavicox.insurapp.viewmodel.ReservationViewModelFactory

/**
 * VisualTransformation que agrupa cada 4 dígitos separados por espacio.
 */
class CreditCardVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        val grouped = digits.chunked(4).joinToString(" ")
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val spacesBefore = (offset / 4).coerceAtMost(grouped.length / 5)
                return offset + spacesBefore
            }
            override fun transformedToOriginal(offset: Int): Int {
                return (offset - offset / 5).coerceAtLeast(0)
            }
        }
        return TransformedText(AnnotatedString(grouped), offsetMapping)
    }
}

class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(4)
        val formatted = buildString {
            digits.forEachIndexed { index, c ->
                if (index == 2) append('/')
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> 5
                }
            }
            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> 4
                }
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun PayScreen(
    navController: NavController,
    fieldId: Int,
    selectedDate: String,
    selectedTime: String,
    price: Int,
    isHalfPayment: Boolean
) {
    val displayedPrice = remember(price, isHalfPayment) {
        if (isHalfPayment) price / 2 else price
    }

    var cardNumberRaw by remember { mutableStateOf("") }
    var expiryRaw by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }

    val reservationVM: ReservationViewModel =
        viewModel(factory = ReservationViewModelFactory(LocalContext.current))
    val loading by reservationVM.loading.collectAsState()
    val error by reservationVM.error.collectAsState()
    val reservation by reservationVM.reservation.collectAsState()

    LaunchedEffect(reservation) {
        reservation?.let { res: ReservationResponse ->
            // Ahora la data class es Serializable, así que no falla:
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("reservation", res)
            navController.navigate(AppScreens.ConfirmationScreen.route)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Monto a pagar: S/. $displayedPrice",
            fontSize = 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Text(text = "Número de Tarjeta", fontSize = 14.sp, color = Color.Gray)
        OutlinedTextField(
            value = cardNumberRaw,
            onValueChange = { input ->
                cardNumberRaw = input.filter { it.isDigit() }.take(16)
            },
            placeholder = { Text("1234 5678 9012 3456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = CreditCardVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Vencimiento (MM/AA)", fontSize = 14.sp, color = Color.Gray)
                OutlinedTextField(
                    value = expiryRaw,
                    onValueChange = { input ->
                        expiryRaw = input.filter { it.isDigit() }.take(4)
                    },
                    placeholder = { Text("MM/AA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ExpiryDateVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "CVV", fontSize = 14.sp, color = Color.Gray)
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { input ->
                        cvv = input.filter { it.isDigit() }.take(3)
                    },
                    placeholder = { Text("123") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        Text(text = "Nombre Completo (Titular)", fontSize = 14.sp, color = Color.Gray)
        OutlinedTextField(
            value = cardHolderName,
            onValueChange = { cardHolderName = it },
            placeholder = { Text("Juan Pérez") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        error?.let {
            Text(
                text = "Error: $it",
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val parts = selectedTime.split(" - ")
                val start = parts.getOrNull(0)?.plus(":00") ?: ""
                val end = parts.getOrNull(1)?.plus(":00") ?: ""
                reservationVM.createReservation(
                    bookingDate = selectedDate,
                    timetableStart = start,
                    timetableEnd = end,
                    totalPrice = displayedPrice.toDouble(),
                    fieldId = fieldId
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (loading) Color.Gray else Color(0xFF2ECC71)
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (loading) "Procesando..." else "Pagar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}
