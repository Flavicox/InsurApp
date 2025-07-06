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
import com.flavicox.insurapp.model.Field
import com.flavicox.insurapp.model.LoginResponse
import com.flavicox.insurapp.model.ReservationByIdResponse
import com.flavicox.insurapp.model.ReservationResponse
import kotlinx.coroutines.flow.Flow

class AuthViewModel(private val context: Context) : ViewModel() {

    private val prefs = UserPreferences(context)

    var loginError by mutableStateOf<String?>(null)

    // Flujos expuestos
    val userFullNameFlow: Flow<String> = prefs.userFullNameFlow
    val userPhoneFlow: Flow<String?> = prefs.userPhoneFlow
    val userEmailFlow: Flow<String?> = prefs.userEmailFlow

    fun registerUser(data: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.authApi.registerUser(data)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    loginError = "Error al registrar"
                }
            } catch (e: Exception) {
                loginError = "Error: ${e.message}"
            }
        }
    }

    fun validateCode(code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.authApi.validateCode(code)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    loginError = "Código inválido"
                }
            } catch (e: Exception) {
                loginError = "Error: ${e.message}"
            }
        }
    }

    fun login(email: String, password: String, onSuccess: (role: String) -> Unit) {
        viewModelScope.launch {
            try {
                // 1) Llamada al endpoint de login (devuelve token + role)
                val response: LoginResponse =
                    RetrofitInstance.authApi.login(LoginRequest(email, password))

                // 2) Guardar token y rol en DataStore
                prefs.saveToken(response.token)
                prefs.saveUserRole(response.role)

                // 3) Recuperar perfil completo usando el token
                //    (Aquí esperamos que UserProfile incluya phone y email)
                val profile = RetrofitInstance.authApi.getUserProfile("Bearer ${response.token}")
                prefs.saveUserProfile(
                    profile.name,
                    profile.surname,
                    profile.phone ?: "",
                    profile.email ?: ""
                )

                // 4) Devolver rol al callback
                onSuccess(response.role)
            } catch (e: Exception) {
                loginError = "Login fallido: ${e.message}"
            }
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return prefs.getToken() != null
    }

    suspend fun getUserRole(): String? {
        return prefs.getUserRole()
    }

    fun logout() {
        viewModelScope.launch {
            prefs.clearToken()
        }
    }

    fun updateProfile(name: String, surname: String, phone: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val token = prefs.getToken() ?: return@launch onResult(false)

                val body = mapOf(
                    "name" to name,
                    "surname" to surname,
                    "phone" to phone
                )

                val response = RetrofitInstance.authApi.updateUserProfile(body, "Bearer $token")
                if (response.isSuccessful) {
                    val currentEmail = prefs.getUserEmail() ?: ""
                    prefs.saveUserProfile(name, surname, phone, currentEmail)
                }

                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val token = prefs.getToken() ?: return@launch
                val profile = RetrofitInstance.authApi.getUserProfile("Bearer $token")
                prefs.saveUserProfile(
                    profile.name,
                    profile.surname,
                    profile.phone ?: "",
                    profile.email ?: ""
                )
            } catch (e: Exception) {
            }
        }
    }

    fun updatePassword(password: String, confirmPassword: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val token = prefs.getToken() ?: return@launch onResult(false)

                val body = mapOf(
                    "password" to password,
                    "confirmPassword" to confirmPassword
                )

                val response = RetrofitInstance.authApi.updatePassword(body, "Bearer $token")
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun fetchReservationById(
        id: Int,
        onResult: (ReservationByIdResponse?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = prefs.getToken() ?: return@launch onResult(null)
                val data  = RetrofitInstance.authApi.getReservationById(id, "Bearer $token")
                onResult(data)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

}
