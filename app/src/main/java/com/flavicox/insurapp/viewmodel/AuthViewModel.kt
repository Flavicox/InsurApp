package com.flavicox.insurapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.model.RegisterRequest
import com.flavicox.insurapp.model.LoginRequest
import com.flavicox.insurapp.network.RetrofitInstance
import com.flavicox.insurapp.datastore.UserPreferences
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AuthViewModel(private val context: Context) : ViewModel() {

    private val prefs = UserPreferences(context)

    var registrationSuccess by mutableStateOf(false)
    var validationSuccess by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)

    fun registerUser(data: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.authApi.registerUser(data)
                if (response.isSuccessful) {
                    registrationSuccess = true
                    onSuccess()
                } else {
                    loginError = "Error al registrar"
                }
            } catch (e: Exception) {
                loginError = "Error: ${e.message}"
                println("❌ Error de red: ${e.message}")
            }
        }
    }

    fun validateCode(code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.authApi.validateCode(code)
                if (response.isSuccessful) {
                    validationSuccess = true
                    onSuccess()
                } else {
                    loginError = "Código inválido"
                }
            } catch (e: Exception) {
                loginError = "Error: ${e.message}"
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.authApi.login(LoginRequest(email, password))
                prefs.saveToken(response.token)
                onSuccess()
            } catch (e: Exception) {
                loginError = "Login fallido: ${e.message}"
                println("❌ Login error: ${e.message}")
            }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return prefs.getToken() != null
    }

    fun logout() {
        viewModelScope.launch {
            prefs.clearToken()
        }
    }
}
