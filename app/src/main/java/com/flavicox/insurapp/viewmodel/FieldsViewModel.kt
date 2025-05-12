package com.flavicox.insurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.Field
import com.flavicox.insurapp.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FieldsViewModel(private val context: Context) : ViewModel() {

    private val prefs = UserPreferences(context)
    private val _fields = MutableStateFlow<List<Field>>(emptyList())
    val fields: StateFlow<List<Field>> = _fields

    private val _availableTimes = MutableStateFlow<List<String>>(emptyList())
    val availableTimes: StateFlow<List<String>> = _availableTimes


    fun loadFields() {
        viewModelScope.launch {
            try {
                val token = prefs.getToken()
                if (!token.isNullOrEmpty()) {
                    val response = RetrofitInstance.authApi.getAvailableFields("Bearer $token")
                    _fields.value = response
                }
            } catch (e: Exception) {
                println("❌ Error al cargar campos: ${e.message}")
            }
        }
    }

    fun getAvailableTimes(fieldId: Int, date: String) {
        viewModelScope.launch {
            try {
                val token = prefs.getToken()
                if (!token.isNullOrEmpty()) {
                    val response = RetrofitInstance.authApi.getAvailableTimes(fieldId, date, "Bearer $token")
                    _availableTimes.value = response
                }
            } catch (e: Exception) {
                println("❌ Error al obtener horarios: ${e.message}")
            }
        }
    }

}
