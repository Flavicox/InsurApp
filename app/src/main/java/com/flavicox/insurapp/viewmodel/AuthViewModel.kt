package com.flavicox.insurapp.viewmodel

// ─────────────────────────────────────────────────────────────
// Imports
// ─────────────────────────────────────────────────────────────
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flavicox.insurapp.datastore.UserPreferences
import com.flavicox.insurapp.model.*
import com.flavicox.insurapp.network.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// ViewModel: AuthViewModel
// Encargado de gestionar la autenticación, perfil de usuario,
// validación de código, y recuperación de reservas.
// ─────────────────────────────────────────────────────────────
class AuthViewModel(private val context: Context) : ViewModel() {

    // ─── Instancias ─────────────────────────────────────────
    private val prefs = UserPreferences(context)

    // ─── Estado ─────────────────────────────────────────────
    var loginError by mutableStateOf<String?>(null)

    // ─── Flujos expuestos desde DataStore ──────────────────
    val userFullNameFlow: Flow<String>   = prefs.userFullNameFlow
    val userPhoneFlow: Flow<String?>     = prefs.userPhoneFlow
    val userEmailFlow: Flow<String?>     = prefs.userEmailFlow

    // ─────────────────────────────────────────────────────────────
    // Funciones de Autenticación
    // ─────────────────────────────────────────────────────────────

    /**
     * Realiza el registro de un nuevo usuario.
     */
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

    /**
     * Verifica el código de validación enviado por correo.
     */
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

    /**
     * Realiza el login y guarda datos del perfil si es exitoso.
     */
    fun login(email: String, password: String, onSuccess: (role: String) -> Unit) {
        viewModelScope.launch {
            try {
                // 1) Login
                val response: LoginResponse =
                    RetrofitInstance.authApi.login(LoginRequest(email, password))

                // 2) Guardar token y rol
                prefs.saveToken(response.token)
                prefs.saveUserRole(response.role)

                // 3) Obtener perfil
                val profile = RetrofitInstance.authApi.getUserProfile("Bearer ${response.token}")
                prefs.saveUserProfile(
                    profile.name,
                    profile.surname,
                    profile.phone ?: "",
                    profile.email ?: ""
                )

                // 4) Notificar rol
                onSuccess(response.role)

            } catch (e: Exception) {
                loginError = "Login fallido: ${e.message}"
            }
        }
    }

    /**
     * Cierra sesión limpiando el token.
     */
    fun logout() {
        viewModelScope.launch {
            prefs.clearToken()
        }
    }

    /**
     * Retorna true si el usuario tiene sesión activa.
     */
    suspend fun isLoggedIn(): Boolean {
        return prefs.getToken() != null
    }

    /**
     * Obtiene el rol guardado del usuario.
     */
    suspend fun getUserRole(): String? {
        return prefs.getUserRole()
    }

    // ─────────────────────────────────────────────────────────────
    // Funciones de Perfil de Usuario
    // ─────────────────────────────────────────────────────────────

    /**
     * Actualiza los datos del perfil del usuario.
     */
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

    /**
     * Carga nuevamente el perfil del usuario desde el servidor.
     */
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
            } catch (_: Exception) {
                // Silenciar error, se puede agregar logging aquí
            }
        }
    }

    /**
     * Actualiza la contraseña del usuario.
     */
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

    // ─────────────────────────────────────────────────────────────
    // Funciones relacionadas a Reservas
    // ─────────────────────────────────────────────────────────────

    /**
     * Obtiene una reserva por su ID.
     */
    fun fetchReservationById(
        id: Int,
        onResult: (ReservationByIdResponse?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = prefs.getToken() ?: return@launch onResult(null)
                val data = RetrofitInstance.authApi.getReservationById(id, "Bearer $token")
                onResult(data)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}
