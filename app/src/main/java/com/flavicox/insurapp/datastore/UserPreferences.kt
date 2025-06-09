package com.flavicox.insurapp.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(val context: Context) {
    companion object {
        val TOKEN_KEY       = stringPreferencesKey("jwt_token")
        val USER_ROLE_KEY   = stringPreferencesKey("user_role")
        val NAME_KEY        = stringPreferencesKey("user_name")
        val SURNAME_KEY     = stringPreferencesKey("user_surname")
        val PHONE_KEY       = stringPreferencesKey("user_phone")   // nuevo
        val EMAIL_KEY       = stringPreferencesKey("user_email")   // nuevo
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

    // Limpia todas las preferencias (token, rol, perfil, teléfono, correo)
    suspend fun clearToken() {
        context.dataStore.edit { it.clear() }
    }

    // --- Rol de usuario ---
    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ROLE_KEY] = role
        }
    }

    suspend fun getUserRole(): String? {
        return context.dataStore.data
            .map { prefs -> prefs[USER_ROLE_KEY] }
            .first()
    }

    val userRoleFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ROLE_KEY]
    }

    // --- Perfil de usuario (nombre, apellido, teléfono, correo) ---
    suspend fun saveUserProfile(name: String, surname: String, phone: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[NAME_KEY]    = name
            prefs[SURNAME_KEY] = surname
            prefs[PHONE_KEY]   = phone
            prefs[EMAIL_KEY]   = email
        }
    }

    val userFullNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val name    = prefs[NAME_KEY]    ?: ""
        val surname = prefs[SURNAME_KEY] ?: ""
        "$name $surname".trim()
    }

    // Flujo para teléfono y correo
    val userPhoneFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PHONE_KEY]
    }
    val userEmailFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[EMAIL_KEY]
    }

    // Métodos por si se necesitan en suspend
    suspend fun getUserPhone(): String? {
        return context.dataStore.data.map { it[PHONE_KEY] }.first()
    }
    suspend fun getUserEmail(): String? {
        return context.dataStore.data.map { it[EMAIL_KEY] }.first()
    }
    suspend fun getUserName(): String? {
        return context.dataStore.data.map { it[NAME_KEY] }.first()
    }
}
