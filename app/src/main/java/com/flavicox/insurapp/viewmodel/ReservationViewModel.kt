package com.flavicox.insurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.CreateReserveRequest
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReservationViewModel(private val context: Context) : ViewModel() {
    private val prefs = UserPreferences(context)

    private val _reservation = MutableStateFlow<ReservationResponse?>(null)
    val reservation: StateFlow<ReservationResponse?> = _reservation

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun createReservation(
        bookingDate: String,
        timetableStart: String,
        timetableEnd: String,
        totalPrice: Double,
        fieldId: Int
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val token = prefs.getToken() ?: throw Exception("Token no encontrado")
                val bearer = "Bearer $token"
                val req = CreateReserveRequest(
                    bookingDate = bookingDate,
                    timetableStart = timetableStart,
                    timetableEnd = timetableEnd,
                    totalPrice = totalPrice,
                    fieldId = fieldId
                )
                val response = RetrofitInstance.authApi.createReservation(req, bearer)
                _reservation.value = response
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
    }
}

class ReservationViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReservationViewModel(context) as T
    }
}
