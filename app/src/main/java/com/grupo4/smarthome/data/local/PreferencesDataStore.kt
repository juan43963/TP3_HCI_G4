package com.grupo4.smarthome.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferences by preferencesDataStore(name = "preferences")

// Preferencias del usuario que no son datos remotos: por ahora la casa activa (ADR-0005)
class PreferencesDataStore(
    private val context: Context,
) {
    val selectedHomeId: Flow<String?> =
        context.appPreferences.data.map { it[Keys.SelectedHomeId]?.takeIf(String::isNotBlank) }

    suspend fun setSelectedHomeId(homeId: String) {
        context.appPreferences.edit { it[Keys.SelectedHomeId] = homeId }
    }

    private object Keys {
        val SelectedHomeId = stringPreferencesKey("selected_home_id")
    }
}
