package com.flavicox.insurapp.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    // Guarda el JWT en DataStore
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    // Recupera el JWT (o null si no existe)
    suspend fun getToken(): String? {
        return context.dataStore.data
            .map { prefs -> prefs[TOKEN_KEY] }
            .first()
    }

    // Limpia todas las preferencias (token, rol, perfil)
    suspend fun clearToken() {
        context.dataStore.edit { it.clear() }
    }

    // --- Rol de usuario ---
    // Guarda el rol ("ROLE_USER" o "ROLE_ADMIN") en DataStore
    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ROLE_KEY] = role
        }
    }

    // Recupera el rol almacenado (o null si no existe)
    suspend fun getUserRole(): String? {
        return context.dataStore.data
            .map { prefs -> prefs[USER_ROLE_KEY] }
            .first()
    }

    // Flow para observar cambios en el rol (si lo necesitas en Compose)
    val userRoleFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ROLE_KEY]
    }

    // --- Perfil de usuario (nombre y apellido) ---
    private val NAME_KEY = stringPreferencesKey("user_name")
    private val SURNAME_KEY = stringPreferencesKey("user_surname")

    suspend fun saveUserProfile(name: String, surname: String) {
        context.dataStore.edit { prefs ->
            prefs[NAME_KEY] = name
            prefs[SURNAME_KEY] = surname
        }
    }

    val userFullNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val name = prefs[NAME_KEY] ?: ""
        val surname = prefs[SURNAME_KEY] ?: ""
        "$name $surname".trim()
    }
}
