package com.grupo4.smarthome.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.ui.screens.notifications.NotificationPreferences
import com.grupo4.smarthome.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

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

    val notificationPreferences: Flow<NotificationPreferences> =
        context.appPreferences.data.map { prefs ->
            NotificationPreferences(
                enabled = prefs[Keys.NotifEnabled] ?: true,
                security = prefs[Keys.NotifSecurity] ?: true,
                devices = prefs[Keys.NotifDevices] ?: true,
                showInAppToast = prefs[Keys.NotifToast] ?: true,
            )
        }

    suspend fun setNotificationPreferences(preferences: NotificationPreferences) {
        context.appPreferences.edit { prefs ->
            prefs[Keys.NotifEnabled] = preferences.enabled
            prefs[Keys.NotifSecurity] = preferences.security
            prefs[Keys.NotifDevices] = preferences.devices
            prefs[Keys.NotifToast] = preferences.showInAppToast
        }
    }

    val notificationsLastSeen: Flow<Long> =
        context.appPreferences.data.map { it[Keys.NotificationsLastSeen] ?: 0L }

    suspend fun setNotificationsLastSeen(timestampMillis: Long) {
        context.appPreferences.edit { it[Keys.NotificationsLastSeen] = timestampMillis }
    }

    val recentRoomAccess: Flow<Map<String, Long>> =
        context.appPreferences.data.map { decodeAccessMap(it[Keys.RoomAccessTimes]) }

    suspend fun recordRoomAccess(roomId: String) {
        context.appPreferences.edit { prefs ->
            prefs[Keys.RoomAccessTimes] = encodeAccessMap(decodeAccessMap(prefs[Keys.RoomAccessTimes]), roomId)
        }
    }

    val recentRoutineAccess: Flow<Map<String, Long>> =
        context.appPreferences.data.map { decodeAccessMap(it[Keys.RoutineAccessTimes]) }

    suspend fun recordRoutineAccess(routineId: String) {
        context.appPreferences.edit { prefs ->
            prefs[Keys.RoutineAccessTimes] = encodeAccessMap(decodeAccessMap(prefs[Keys.RoutineAccessTimes]), routineId)
        }
    }

    private fun decodeAccessMap(json: String?): Map<String, Long> =
        json?.let { runCatching { ApiJson.Instance.decodeFromString<Map<String, Long>>(it) }.getOrNull() }
            ?: emptyMap()

    private fun encodeAccessMap(current: Map<String, Long>, id: String): String {
        val updated = (current + (id to System.currentTimeMillis()))
            .entries
            .sortedByDescending { it.value }
            .take(20)
            .associate { it.key to it.value }
        return ApiJson.Instance.encodeToString(updated)
    }

    private object Keys {
        val SelectedHomeId = stringPreferencesKey("selected_home_id")
        val ThemePreference = stringPreferencesKey("theme_preference")
        val NotifEnabled = booleanPreferencesKey("notif_enabled")
        val NotifSecurity = booleanPreferencesKey("notif_security")
        val NotifDevices = booleanPreferencesKey("notif_devices")
        val NotifToast = booleanPreferencesKey("notif_toast")
        val NotificationsLastSeen = longPreferencesKey("notifications_last_seen")
        val RoomAccessTimes = stringPreferencesKey("room_access_times")
        val RoutineAccessTimes = stringPreferencesKey("routine_access_times")
    }
}
