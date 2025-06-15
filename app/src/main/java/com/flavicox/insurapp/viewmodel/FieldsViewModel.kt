package com.flavicox.insurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.CreateReserveRequest
import com.flavicox.insurapp.model.Field
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.model.TimeSlot
import com.flavicox.insurapp.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class FieldsViewModel(private val context: Context) : ViewModel() {

    private val prefs = UserPreferences(context)
    private val _fields = MutableStateFlow<List<Field>>(emptyList())
    val fields: StateFlow<List<Field>> = _fields

    private val _availableTimes = MutableStateFlow<List<TimeSlot>>(emptyList())
    val availableTimes: StateFlow<List<TimeSlot>> = _availableTimes

    // 1. Carga campos disponibles
    fun loadFields() {
        viewModelScope.launch {
            try {
                val token = prefs.getToken()
                if (!token.isNullOrEmpty()) {
                    _fields.value = RetrofitInstance.authApi.getAvailableFields("Bearer $token")
                }
            } catch (e: Exception) {
                println("❌ Error al cargar campos: ${e.message}")
            }
        }
    }

    // 2. Obtiene horarios disponibles
    fun getAvailableTimes(fieldId: Int, date: String) {
        viewModelScope.launch {
            try {
                val token = prefs.getToken()
                if (!token.isNullOrEmpty()) {
                    _availableTimes.value = RetrofitInstance.authApi
                        .getAvailableTimes(fieldId, date, "Bearer $token")
                }
            } catch (e: Exception) {
                println("❌ Error al obtener horarios: ${e.message}")
                _availableTimes.value = emptyList()
            }
        }
    }

    // 3. Crea reserva
    private val _lastReservation = MutableStateFlow<ReservationResponse?>(null)
    val lastReservation: StateFlow<ReservationResponse?> = _lastReservation

    suspend fun createReservation(request: CreateReserveRequest): ReservationResponse {
        val resp = RetrofitInstance.authApi.createReservation(request, "Bearer ${prefs.getToken()}")
        _lastReservation.value = resp
        return resp
    }
}
