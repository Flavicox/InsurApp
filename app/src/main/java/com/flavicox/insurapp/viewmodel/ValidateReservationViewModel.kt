package com.flavicox.insurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.ValidateReservationResponse
import com.flavicox.insurapp.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ValidateReservationViewModel(private val context: Context) : ViewModel() {
    private val prefs = UserPreferences(context)

    // Flujo con los datos de la reserva
    private val _reservation = MutableStateFlow<ValidateReservationResponse?>(null)
    val reservation: StateFlow<ValidateReservationResponse?> = _reservation

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Nuevo flujo que indicará cuándo la validación fue exitosa
    private val _validationSuccess = MutableStateFlow(false)
    val validationSuccess: StateFlow<Boolean> = _validationSuccess

    fun loadReservation(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _validationSuccess.value = false
            try {
                val token = prefs.getToken() ?: throw Exception("Token no encontrado")
                val bearer = "Bearer $token"
                val response = RetrofitInstance.authApi.validateReservation(id, bearer)
                _reservation.value = response
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun validateReservation(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val token = prefs.getToken() ?: throw Exception("Token no encontrado")
                val bearer = "Bearer $token"
                val updated = RetrofitInstance.authApi.validateReservationPatch(id, bearer)
                _reservation.value = updated
                _validationSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun clear() {
        _reservation.value = null
        _error.value = null
        _loading.value = false
        _validationSuccess.value = false
    }
}

class ValidateReservationViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ValidateReservationViewModel(context) as T
    }
}