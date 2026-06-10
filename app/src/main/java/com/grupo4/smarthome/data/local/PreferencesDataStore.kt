package com.grupo4.smarthome.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.grupo4.smarthome.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferences by preferencesDataStore(name = "preferences")

// Preferencias del usuario que no son datos remotos: casa activa y tema
class PreferencesDataStore(
    private val context: Context,
) {
    val selectedHomeId: Flow<String?> =
        context.appPreferences.data.map { it[Keys.SelectedHomeId]?.takeIf(String::isNotBlank) }

    suspend fun setSelectedHomeId(homeId: String) {
        context.appPreferences.edit { it[Keys.SelectedHomeId] = homeId }
    }

    val themePreference: Flow<ThemePreference> =
        context.appPreferences.data.map { prefs ->
            prefs[Keys.ThemePreference]
                ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
                ?: ThemePreference.SYSTEM
        }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.appPreferences.edit { it[Keys.ThemePreference] = theme.name }
    }

    private object Keys {
        val SelectedHomeId = stringPreferencesKey("selected_home_id")
        val ThemePreference = stringPreferencesKey("theme_preference")
    }
}
