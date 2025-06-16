package com.flavicox.insurapp.viewmodel

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

class ResumeViewModel(
    private val context: Context,
    private val apiService: AuthApiService
) : ViewModel() {

    private val _paymentResult = MutableStateFlow<PaymentResponse?>(null)
    val paymentResult: StateFlow<PaymentResponse?> = _paymentResult

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun initiatePayment(reservationId: Int, request: PaymentRequest) {
        viewModelScope.launch {
            try {
                val token = UserPreferences(context).getToken() ?: throw Exception("Token no encontrado")
                val response = apiService.initiatePayment(reservationId, request, "Bearer $token")
                _paymentResult.value = response
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}
