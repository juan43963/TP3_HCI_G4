package com.grupo4.smarthome.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.toMessageRes
import com.grupo4.smarthome.data.local.PreferencesDataStore
import com.grupo4.smarthome.data.model.NotificationEvent
import com.grupo4.smarthome.data.model.NotificationKind
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.categoryFor
import com.grupo4.smarthome.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val MAX_EVENTS = 100
private const val MAX_VISIBLE = 25
private const val RECENT_WINDOW_MILLIS = 7L * 24 * 60 * 60 * 1000
private const val DEDUPE_WINDOW_MILLIS = 5_000L
private const val GENERIC_DEVICE_EVENT = "statusChanged"

class NotificationsViewModel(
    private val repository: NotificationsRepository,
    private val preferencesStore: PreferencesDataStore,
) : ViewModel() {

    private data class Status(val isLoading: Boolean = false, val errorMessage: Int? = null)

    private val _events = MutableStateFlow<List<NotificationEvent>>(emptyList())
    // deviceId -> nombre canónico del tipo, que es lo que categoriza la notificación (categoryFor).
    private val _deviceTypeNames = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _status = MutableStateFlow(Status(isLoading = true))

    @Volatile
    private var isViewing = false

    val preferences: StateFlow<NotificationPreferences> =
        preferencesStore.notificationPreferences
            .stateIn(viewModelScope, SharingStarted.Eagerly, NotificationPreferences())

    val uiState: StateFlow<NotificationsUiState> =
        combine(
            _events,
            _deviceTypeNames,
            preferencesStore.notificationPreferences,
            preferencesStore.notificationsLastSeen,
            _status,
        ) { events, types, prefs, lastSeen, status ->
            val cutoff = System.currentTimeMillis() - RECENT_WINDOW_MILLIS
            val items = coalesceDeviceEvents(events)
                .asSequence()
                .filter { it.timestampMillis >= cutoff }
                .map { event ->
                    FeedNotification(
                        event = event,
                        category = event.categoryFor(types[event.deviceId]),
                        read = event.timestampMillis <= lastSeen,
                    )
                }
                .filter { prefs.enabled && prefs.isEnabled(it.category) }
                .sortedByDescending { it.event.timestampMillis }
                .take(MAX_VISIBLE)
                .toList()
            NotificationsUiState(
                items = items,
                unreadCount = items.count { !it.read },
                isLoading = status.isLoading,
                errorMessage = status.errorMessage,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, NotificationsUiState(isLoading = true))

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _status.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getRecentEvents()) {
                is AppResult.Success -> {
                    addEvents(result.data)
                    _status.update { it.copy(isLoading = false, errorMessage = null) }
                }
                is AppResult.Error -> _status.update {
                    it.copy(isLoading = false, errorMessage = result.error.kind.toMessageRes())
                }
            }
        }
    }

    suspend fun observeRealtime() {
        repository.realtimeEvents().collect { event ->
            addEvents(listOf(event))
            if (isViewing) markAllSeen()
        }
    }

    fun onRoomsChanged(rooms: List<Room>) {
        _deviceTypeNames.value = buildMap {
            rooms.forEach { room -> room.devices.forEach { put(it.id, it.type.canonicalName) } }
        }
    }

    fun onScreenVisible() {
        isViewing = true
        markAllSeen()
    }

    fun onScreenHidden() {
        isViewing = false
    }

    fun setPreferences(preferences: NotificationPreferences) {
        viewModelScope.launch { preferencesStore.setNotificationPreferences(preferences) }
    }

    fun markAllSeen() {
        viewModelScope.launch { preferencesStore.setNotificationsLastSeen(System.currentTimeMillis()) }
    }

    private fun addEvents(incoming: List<NotificationEvent>) {
        _events.update { current ->
            (incoming + current)
                .distinctBy { it.id }
                .sortedByDescending { it.timestampMillis }
                .take(MAX_EVENTS)
        }
    }
}

internal fun coalesceDeviceEvents(events: List<NotificationEvent>): List<NotificationEvent> =
    events.filterNot { it.isRedundantGenericEcho(events) }

private fun NotificationEvent.isRedundantGenericEcho(all: List<NotificationEvent>): Boolean {
    if (kind != NotificationKind.DEVICE_ACTION || actionName != GENERIC_DEVICE_EVENT) return false
    val device = deviceId ?: return false
    return all.any { other ->
        other.deviceId == device &&
            other.actionName != null &&
            other.actionName != GENERIC_DEVICE_EVENT &&
            abs(other.timestampMillis - timestampMillis) <= DEDUPE_WINDOW_MILLIS
    }
}

class NotificationsViewModelFactory(
    private val repository: NotificationsRepository,
    private val preferencesStore: PreferencesDataStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel(repository, preferencesStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
