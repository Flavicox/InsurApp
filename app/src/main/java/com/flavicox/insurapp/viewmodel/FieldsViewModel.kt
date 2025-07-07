package com.flavicox.insurapp.viewmodel

// ─────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.*
import com.flavicox.insurapp.network.RetrofitInstance
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// ViewModel: FieldsViewModel
// Gestiona los campos deportivos, horarios disponibles y la
// creación de reservas.
// ─────────────────────────────────────────────────────────────
class FieldsViewModel(private val context: Context) : ViewModel() {

    // ─── Dependencias y estado ──────────────────────────────
    private val prefs = UserPreferences(context)

    // Lista de campos disponibles
    private val _fields = MutableStateFlow<List<Field>>(emptyList())
    val fields: StateFlow<List<Field>> = _fields

    // Horarios disponibles para un campo y día específico
    private val _availableTimes = MutableStateFlow<List<TimeSlot>>(emptyList())
    val availableTimes: StateFlow<List<TimeSlot>> = _availableTimes

    // Última reserva creada (opcional para monitoreo)
    private val _lastReservation = MutableStateFlow<ReservationResponse?>(null)
    val lastReservation: StateFlow<ReservationResponse?> = _lastReservation

    // ─────────────────────────────────────────────────────────
    // Función: Cargar campos disponibles desde el backend
    // ─────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────
    // Función: Obtener horarios disponibles para un campo
    // ─────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────
    // Función: Crear reserva para un campo y horario
    // ─────────────────────────────────────────────────────────
    suspend fun createReservation(request: CreateReserveRequest): ReservationResponse {
        val response = RetrofitInstance.authApi.createReservation(request, "Bearer ${prefs.getToken()}")
        _lastReservation.value = response
        return response
    }
}
