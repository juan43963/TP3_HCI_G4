package com.grupo4.smarthome.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.grupo4.smarthome.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionPreferences by preferencesDataStore(name = "session")

class SessionDataStore(
    private val context: Context,
) {
    val session: Flow<SessionData?> =
        context.sessionPreferences.data.map { preferences ->
            val token = preferences[Keys.Token].orEmpty()
            val email = preferences[Keys.Email].orEmpty()
            val name = preferences[Keys.Name].orEmpty()

            if (token.isBlank() || email.isBlank() || name.isBlank()) {
                null
            } else {
                SessionData(
                    token = token,
                    user = User(
                        id = preferences[Keys.UserId],
                        email = email,
                        name = name,
                        isVerified = preferences[Keys.IsVerified] ?: false,
                        createdAt = preferences[Keys.CreatedAt],
                    ),
                )
            }
        }

    suspend fun saveSession(token: String, user: User) {
        context.sessionPreferences.edit { preferences ->
            preferences[Keys.Token] = token
            user.id?.let { preferences[Keys.UserId] = it } ?: preferences.remove(Keys.UserId)
            preferences[Keys.Email] = user.email
            preferences[Keys.Name] = user.name
            preferences[Keys.IsVerified] = user.isVerified
            user.createdAt?.let { preferences[Keys.CreatedAt] = it } ?: preferences.remove(Keys.CreatedAt)
        }
    }

    suspend fun clearSession() {
        context.sessionPreferences.edit { it.clear() }
    }

    private object Keys {
        val Token = stringPreferencesKey("token")
        val UserId = stringPreferencesKey("user_id")
        val Email = stringPreferencesKey("email")
        val Name = stringPreferencesKey("name")
        val IsVerified = booleanPreferencesKey("is_verified")
        val CreatedAt = stringPreferencesKey("created_at")
    }
}
