package com.grupo4.smarthome.ui.screens.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.data.repository.RoomsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomsViewModel(
    private val roomsRepository: RoomsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RoomsUiState(
            rooms = emptyList(),
            availableDeviceTypes = RoomsPreviewData.availableDeviceTypes,
        )
    )
    val uiState: StateFlow<RoomsUiState> = _uiState.asStateFlow()

    // Hogar activo actual; lo setea la UI vía onHomeChanged según SelectHome/Preferences
    private var homeId: String? = null

    // La UI llama esto cuando cambia el hogar activo. Recarga solo si cambió el id.
    fun onHomeChanged(newHomeId: String?) {
        if (newHomeId == homeId) {
            // Primer arranque del mismo hogar: cargar si todavía no hay datos ni error
            val state = _uiState.value
            if (newHomeId != null && state.rooms.isEmpty() && !state.isLoading && state.errorMessage == null) {
                loadRooms()
            }
            return
        }
        homeId = newHomeId
        // Al cambiar de hogar reseteamos selección y filtros para no arrastrar contexto
        _uiState.update {
            it.copy(
                rooms = emptyList(),
                selectedRoomId = null,
                searchQuery = "",
                selectedRoomType = null,
                editRoomsMode = false,
                editDevicesMode = false,
                errorMessage = null,
            )
        }
        loadRooms()
    }

    fun loadRooms() {
        val currentHome = homeId
        if (currentHome.isNullOrBlank()) {
            _uiState.update {
                it.copy(isLoading = false, rooms = emptyList(), errorMessage = NoActiveHomeMessage)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = roomsRepository.getRooms(currentHome)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, rooms = result.data, errorMessage = null)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(result.error.kind, result.error.description))
                }
            }
        }
    }

    // --- Habitaciones (API real) ---

    fun createRoom(name: String, type: RoomType) {
        val currentHome = homeId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (val result = roomsRepository.createRoom(currentHome, name, type)) {
                is AppResult.Success -> refreshRooms()
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = messageFor(result.error.kind, result.error.description))
                }
            }
        }
    }

    fun renameRoom(roomId: String, name: String, type: RoomType) {
        viewModelScope.launch {
            when (val result = roomsRepository.renameRoom(roomId, name, type)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(
                        rooms = state.rooms.map {
                            // Conservamos los dispositivos locales (la API aún no los devuelve en v1)
                            if (it.id == result.data.id) result.data.copy(devices = it.devices) else it
                        },
                        errorMessage = null,
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = messageFor(result.error.kind, result.error.description))
                }
            }
        }
    }

    fun deleteRoom(roomId: String) {
        val currentHome = homeId ?: return
        viewModelScope.launch {
            when (val result = roomsRepository.deleteRoom(currentHome, roomId)) {
                is AppResult.Success -> {
                    if (_uiState.value.selectedRoomId == roomId) {
                        _uiState.update { it.copy(selectedRoomId = null, editDevicesMode = false) }
                    }
                    refreshRooms()
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = messageFor(result.error.kind, result.error.description))
                }
            }
        }
    }

    fun toggleRoomFavorite(roomId: String) {
        viewModelScope.launch {
            when (val result = roomsRepository.toggleRoomFavorite(roomId)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(
                        rooms = state.rooms.map {
                            if (it.id == result.data.id) result.data.copy(devices = it.devices) else it
                        },
                        errorMessage = null,
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = messageFor(result.error.kind, result.error.description))
                }
            }
        }
    }

    // Refetch silencioso tras crear/borrar: no muestra el loader de pantalla completa
    private suspend fun refreshRooms() {
        val currentHome = homeId ?: return
        when (val result = roomsRepository.getRooms(currentHome)) {
            is AppResult.Success -> _uiState.update {
                it.copy(rooms = result.data, errorMessage = null)
            }
            is AppResult.Error -> _uiState.update {
                it.copy(errorMessage = messageFor(result.error.kind, result.error.description))
            }
        }
    }

    // --- Estado de UI (filtros, búsqueda, selección, modos de edición) ---

    fun onSearchChange(value: String) = _uiState.update { it.copy(searchQuery = value) }

    fun onFilterSelected(type: RoomType?) = _uiState.update { it.copy(selectedRoomType = type) }

    fun onRoomClick(roomId: String) = _uiState.update { it.copy(selectedRoomId = roomId, searchQuery = "") }

    fun onSelectedRoomChange(roomId: String?) = _uiState.update {
        it.copy(selectedRoomId = roomId, editDevicesMode = if (roomId == null) false else it.editDevicesMode)
    }

    fun onBackToRooms() = _uiState.update { it.copy(selectedRoomId = null, editDevicesMode = false) }

    fun onEditRoomsToggle() = _uiState.update { it.copy(editRoomsMode = !it.editRoomsMode) }

    fun onEditDevicesToggle() = _uiState.update { it.copy(editDevicesMode = !it.editDevicesMode) }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    // --- Dispositivos (v1: control local en memoria; la API de dispositivos llega en fase 2) ---

    fun onToggleDevice(deviceId: String) =
        _uiState.update { it.copy(rooms = it.rooms.withDeviceToggled(deviceId)) }

    fun onDeviceValueChange(deviceId: String, value: Int) =
        _uiState.update { it.copy(rooms = it.rooms.withDeviceValueChanged(deviceId, value)) }

    fun addDevices(roomId: String, devices: List<PendingDevice>) =
        _uiState.update { it.copy(rooms = it.rooms.withAddedDevices(roomId, devices)) }

    fun editDevice(deviceId: String, name: String) =
        _uiState.update { it.copy(rooms = it.rooms.withEditedDevice(deviceId, name)) }

    fun deleteDevice(deviceId: String) =
        _uiState.update { it.copy(rooms = it.rooms.withoutDevice(deviceId)) }

    private fun messageFor(kind: ErrorKind, fallback: String): String =
        when (kind) {
            ErrorKind.Unauthorized -> "Tu sesión expiró. Volvé a iniciar sesión."
            ErrorKind.Network -> "No se pudo conectar con el servidor."
            ErrorKind.Validation -> "Revisá los datos ingresados."
            ErrorKind.NotFound -> "No se encontró la habitación."
            ErrorKind.Server -> "El servidor no pudo completar la operación."
            else -> fallback
        }

    private companion object {
        const val NoActiveHomeMessage = "Seleccioná un hogar para ver sus habitaciones."
    }
}

class RoomsViewModelFactory(
    private val roomsRepository: RoomsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomsViewModel::class.java)) {
            return RoomsViewModel(roomsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
