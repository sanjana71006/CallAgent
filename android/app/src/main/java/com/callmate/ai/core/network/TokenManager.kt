package com.callmate.ai.core.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "callmate_secure_tokens")

class TokenManager(private val context: Context) {

    private val KEY_TOKEN = stringPreferencesKey("jwt_session_token")
    private val KEY_USER_ID = stringPreferencesKey("auth_user_id")
    private val KEY_EMAIL = stringPreferencesKey("auth_user_email")
    private val KEY_NAME = stringPreferencesKey("auth_user_name")

    val tokenFlow: Flow<String?> = context.tokenDataStore.data.map { preferences ->
        preferences[KEY_TOKEN]
    }

    val isLoggedInFlow: Flow<Boolean> = context.tokenDataStore.data.map { preferences ->
        !preferences[KEY_TOKEN].isNullOrBlank()
    }

    val userEmailFlow: Flow<String> = context.tokenDataStore.data.map { preferences ->
        preferences[KEY_EMAIL] ?: ""
    }

    val userNameFlow: Flow<String> = context.tokenDataStore.data.map { preferences ->
        preferences[KEY_NAME] ?: ""
    }

    val userIdFlow: Flow<String> = context.tokenDataStore.data.map { preferences ->
        preferences[KEY_USER_ID] ?: ""
    }

    suspend fun saveSession(token: String, userId: String, email: String, name: String) {
        context.tokenDataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
            preferences[KEY_USER_ID] = userId
            preferences[KEY_EMAIL] = email
            preferences[KEY_NAME] = name
        }
    }

    suspend fun updateName(name: String) {
        context.tokenDataStore.edit { preferences ->
            preferences[KEY_NAME] = name
        }
    }

    suspend fun clearSession() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_EMAIL)
            preferences.remove(KEY_NAME)
        }
    }

    suspend fun getToken(): String? {
        return context.tokenDataStore.data.first()[KEY_TOKEN]
    }

    suspend fun getBearerToken(): String? {
        val token = getToken()
        return if (!token.isNullOrBlank()) "Bearer $token" else null
    }

    fun getBearerTokenBlocking(): String? {
        return runBlocking { getBearerToken() }
    }
}
