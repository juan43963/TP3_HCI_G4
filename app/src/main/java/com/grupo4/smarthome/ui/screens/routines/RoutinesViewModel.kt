package com.grupo4.smarthome.ui.screens.routines

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.R
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory
import com.grupo4.smarthome.data.repository.DevicesRepository
import com.grupo4.smarthome.data.repository.RoomsRepository
import com.grupo4.smarthome.data.repository.RoutinesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutinesViewModel(
    private val routinesRepository: RoutinesRepository,
    private val roomsRepository: RoomsRepository,
    private val devicesRepository: DevicesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoutinesUiState())
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()

    // Hogar activo actual; lo setea la UI vía onHomeChanged
    private var homeId: String? = null

    fun onHomeChanged(newHomeId: String?) {
        if (newHomeId == homeId) {
            val state = _uiState.value
            if (newHomeId != null && state.routines.isEmpty() && !state.isLoading && state.errorMessage == null) {
                loadRoutines()
            } else if (newHomeId != null && state.devices.isEmpty()) {
                loadDevices(newHomeId)
            }
            return
        }
        homeId = newHomeId
        _uiState.update {
            it.copy(
                routines = emptyList(),
                devices = emptyList(),
                deviceRoomNames = emptyMap(),
                errorMessage = null,
            )
        }
        loadRoutines()
    }

    fun loadRoutines() {
        val currentHome = homeId
        if (currentHome.isNullOrBlank()) {
            _uiState.update {
                it.copy(isLoading = false, routines = emptyList(), errorMessage = NoActiveHomeMessage)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = routinesRepository.getRoutines(currentHome)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, routines = result.data, errorMessage = null) }
                    loadDevices(currentHome)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun createRoutine(
        name: String,
        category: RoutineCategory,
        time: String,
        days: List<Boolean>,
        actions: List<RoutineAction>,
    ) {
        val currentHome = homeId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            val result = routinesRepository.createRoutine(currentHome, name, category, time, days, actions)
            when (result) {
                is AppResult.Success -> refreshRoutines()
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun updateRoutine(routine: Routine) {
        viewModelScope.launch {
            when (val result = routinesRepository.updateRoutine(routine)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(
                        routines = state.routines.map { if (it.id == result.data.id) result.data else it },
                        errorMessage = null,
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    // Guarda los cambios del detalle reconstruyendo la rutina de dominio (conserva acciones/categoría/homeId)
    fun saveRoutine(routineId: String, name: String, time24h: String, days: List<Boolean>, enabled: Boolean) {
        val original = _uiState.value.routines.firstOrNull { it.id == routineId } ?: return
        updateRoutine(original.copy(name = name, time = time24h, days = days, enabled = enabled))
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            when (val result = routinesRepository.deleteRoutine(routineId)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(routines = state.routines.filterNot { it.id == routineId }, errorMessage = null)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun toggleFavorite(routineId: String) {
        viewModelScope.launch {
            when (val result = routinesRepository.toggleFavorite(routineId)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(
                        routines = state.routines.map { if (it.id == result.data.id) result.data else it },
                        errorMessage = null,
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun executeRoutine(routineId: String) {
        val routine = _uiState.value.routines.firstOrNull { it.id == routineId } ?: return
        _uiState.update { it.copy(executingRoutineId = routineId, errorMessage = null) }
        viewModelScope.launch {
            when (val result = routinesRepository.executeRoutine(routineId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(executingRoutineId = null, infoMessage = routine.name) }
                    homeId?.let { currentHome -> loadDevices(currentHome) }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        executingRoutineId = null,
                        errorMessage = messageFor(result.error.kind),
                    )
                }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    fun dismissInfo() = _uiState.update { it.copy(infoMessage = null) }

    // --- Helpers ---

    private suspend fun refreshRoutines() {
        val currentHome = homeId ?: return
        when (val result = routinesRepository.getRoutines(currentHome)) {
            is AppResult.Success -> _uiState.update { it.copy(routines = result.data, errorMessage = null) }
            is AppResult.Error -> _uiState.update {
                it.copy(errorMessage = messageFor(result.error.kind))
            }
        }
    }

    // Carga los dispositivos del hogar (rooms -> devices) y las acciones de cada tipo presente
    private fun loadDevices(homeId: String) {
        viewModelScope.launch {
            val rooms = when (val result = roomsRepository.getRooms(homeId)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return@launch
            }
            val deviceEntries = coroutineScope {
                rooms.map { room ->
                    async {
                        when (val result = devicesRepository.getRoomDevices(room.id)) {
                            is AppResult.Success -> result.data.map { it to room.name }
                            is AppResult.Error -> emptyList()
                        }
                    }
                }.awaitAll()
            }.flatten()
            val devices = deviceEntries.map { it.first }
            val deviceRoomNames = deviceEntries.associate { (device, roomName) -> device.id to roomName }
            _uiState.update { it.copy(devices = devices, deviceRoomNames = deviceRoomNames) }
            loadDeviceActions(devices)
        }
    }

    private suspend fun loadDeviceActions(devices: List<Device>) {
        val types = devices.map { it.type }.distinct()
        val actions = mutableMapOf<DeviceType, List<DeviceActionInfo>>()
        for (type in types) {
            when (val result = devicesRepository.getActionsFor(type)) {
                is AppResult.Success -> actions[type] = result.data
                is AppResult.Error -> Unit
            }
        }
        _uiState.update { it.copy(deviceActions = actions) }
    }

    @StringRes
    private fun messageFor(kind: ErrorKind): Int =
        when (kind) {
            ErrorKind.Unauthorized -> R.string.error_session_expired
            ErrorKind.Network -> R.string.error_network
            ErrorKind.Validation -> R.string.error_validation
            ErrorKind.NotFound -> R.string.error_not_found
            ErrorKind.Server -> R.string.error_server_generic
            else -> R.string.error_unknown
        }

    private companion object {
        val NoActiveHomeMessage = R.string.routines_no_active_home
    }
}

class RoutinesViewModelFactory(
    private val routinesRepository: RoutinesRepository,
    private val roomsRepository: RoomsRepository,
    private val devicesRepository: DevicesRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutinesViewModel::class.java)) {
            return RoutinesViewModel(routinesRepository, roomsRepository, devicesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
