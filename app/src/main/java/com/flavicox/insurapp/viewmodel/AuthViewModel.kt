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
import kotlinx.coroutines.flow.Flow

class AuthViewModel(private val context: Context) : ViewModel() {

    private val prefs = UserPreferences(context)

    var registrationSuccess by mutableStateOf(false)
    var validationSuccess by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)

    val userFullNameFlow: Flow<String> = prefs.userFullNameFlow

    val fields = mutableStateOf<List<Field>>(emptyList())



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
                //    (Aquí obtenemos name y surname)
                val profile = RetrofitInstance.authApi.getUserProfile("Bearer ${response.token}")
                prefs.saveUserProfile(profile.name, profile.surname)

                // 4) Ahora sí, devolvemos el rol al callback
                onSuccess(response.role)
            } catch (e: Exception) {
                loginError = "Login fallido: ${e.message}"
            }
        }
    }


    /**
     * Devuelve true si existe un token guardado.
     */
    suspend fun isLoggedIn(): Boolean {
        return prefs.getToken() != null
    }

    /**
     * Nuevo: lee el rol guardado en DataStore (o null si no hay).
     */
    suspend fun getUserRole(): String? {
        return prefs.getUserRole()
    }

    /**
     * Cierra sesión (limpia token y rol).
     */
    fun logout() {
        viewModelScope.launch {
            prefs.clearToken()
        }
    }

}
