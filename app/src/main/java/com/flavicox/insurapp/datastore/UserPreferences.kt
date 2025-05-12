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
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }.first()
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.clear() }
    }

    val NAME_KEY = stringPreferencesKey("user_name")
    val SURNAME_KEY = stringPreferencesKey("user_surname")

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
