package com.flavicox.insurapp.viewmodel

// ─────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.PaymentRequest
import com.flavicox.insurapp.model.PaymentResponse
import com.flavicox.insurapp.network.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// ViewModel: ResumeViewModel
// Encargado de gestionar el proceso de pago con Stripe:
// - Llamar al backend para iniciar el PaymentIntent
// - Exponer el resultado y posibles errores
// ─────────────────────────────────────────────────────────────
class ResumeViewModel(
    private val context: Context,
    private val apiService: AuthApiService
) : ViewModel() {

    // ─── Resultado exitoso del intento de pago ──────────────
    private val _paymentResult = MutableStateFlow<PaymentResponse?>(null)
    val paymentResult: StateFlow<PaymentResponse?> = _paymentResult

    // ─── Mensaje de error si la petición falla ──────────────
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ───────────────────────────────────────────────────────
    // Inicia el flujo de pago llamando al backend
    // con el ID de reserva y los datos de pago
    // ───────────────────────────────────────────────────────
    fun initiatePayment(reservationId: Int, request: PaymentRequest) {
        viewModelScope.launch {
            try {
                val token = UserPreferences(context).getToken()
                    ?: throw Exception("Token no encontrado")

                val response = apiService.initiatePayment(
                    reservationId = reservationId,
                    body = request,
                    token = "Bearer $token"
                )

                _paymentResult.value = response

            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}
