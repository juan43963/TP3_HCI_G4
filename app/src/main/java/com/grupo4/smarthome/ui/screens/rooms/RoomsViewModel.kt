package com.grupo4.smarthome.ui.screens.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.R
import com.grupo4.smarthome.core.result.ApiError
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.toMessageRes
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.data.local.PreferencesDataStore
import com.grupo4.smarthome.data.remote.socket.SocketEvent
import com.grupo4.smarthome.data.repository.DevicesRepository
import com.grupo4.smarthome.data.repository.RoomsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomsViewModel(
    private val roomsRepository: RoomsRepository,
    private val devicesRepository: DevicesRepository,
    private val deviceEvents: Flow<SocketEvent>,
    private val preferences: PreferencesDataStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RoomsUiState(
            rooms = emptyList(),
            availableDeviceTypes = DeviceType.entries,
        )
    )
    val uiState: StateFlow<RoomsUiState> = _uiState.asStateFlow()

    // Hogar activo actual; lo setea la UI vía onHomeChanged según SelectHome/Preferences
    private var homeId: String? = null

    init {
        observeDeviceEvents()
    }

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
                is AppResult.Success -> {
                    // Preservamos los dispositivos ya cargados para que no desaparezcan si el refetch falla
                    val currentDevicesByRoom = _uiState.value.rooms.associate { it.id to it.devices }
                    val roomsWithCurrentDevices = result.data.map { room ->
                        room.copy(devices = currentDevicesByRoom[room.id].orEmpty())
                    }
                    _uiState.update {
                        it.copy(isLoading = false, rooms = roomsWithCurrentDevices, errorMessage = null)
                    }
                    loadAllRoomDevices()
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.kind.toMessageRes())
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
                    it.copy(errorMessage = result.error.kind.toMessageRes())
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
                    it.copy(errorMessage = result.error.kind.toMessageRes())
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
                    it.copy(errorMessage = result.error.kind.toMessageRes())
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
                    it.copy(errorMessage = result.error.kind.toMessageRes())
                }
            }
        }
    }

    // Refetch silencioso tras crear/borrar: no muestra el loader de pantalla completa
    private suspend fun refreshRooms() {
        val currentHome = homeId ?: return
        when (val result = roomsRepository.getRooms(currentHome)) {
            is AppResult.Success -> {
                // Preservamos los dispositivos ya cargados para que no desaparezcan si el refetch falla
                val currentDevicesByRoom = _uiState.value.rooms.associate { it.id to it.devices }
                val roomsWithCurrentDevices = result.data.map { room ->
                    room.copy(devices = currentDevicesByRoom[room.id].orEmpty())
                }
                _uiState.update { it.copy(rooms = roomsWithCurrentDevices, errorMessage = null) }
                loadAllRoomDevices()
            }
            is AppResult.Error -> _uiState.update {
                it.copy(errorMessage = result.error.kind.toMessageRes())
            }
        }
    }

    // --- Estado de UI (filtros, búsqueda, selección, modos de edición) ---

    fun onSearchChange(value: String) = _uiState.update { it.copy(searchQuery = value) }

    fun onFilterSelected(type: RoomType?) = _uiState.update { it.copy(selectedRoomType = type) }

    fun onRoomClick(roomId: String) = selectRoom(roomId, fromDashboard = false)

    fun onRoomOpenedFromDashboard(roomId: String) = selectRoom(roomId, fromDashboard = true)

    private fun selectRoom(roomId: String, fromDashboard: Boolean) {
        _uiState.update { it.copy(selectedRoomId = roomId, searchQuery = "", openedFromDashboard = fromDashboard) }
        viewModelScope.launch { preferences.recordRoomAccess(roomId) }
    }

    fun onSelectedRoomChange(roomId: String?) {
        _uiState.update {
            it.copy(selectedRoomId = roomId, editDevicesMode = if (roomId == null) false else it.editDevicesMode)
        }
        if (roomId != null) viewModelScope.launch { preferences.recordRoomAccess(roomId) }
    }

    fun onBackToRooms() = _uiState.update {
        it.copy(selectedRoomId = null, editDevicesMode = false, openedFromDashboard = false)
    }

    fun onEditRoomsToggle() = _uiState.update { it.copy(editRoomsMode = !it.editRoomsMode) }

    fun onEditDevicesToggle() = _uiState.update { it.copy(editDevicesMode = !it.editDevicesMode) }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    // --- Dispositivos (API real) ---

    // Carga los dispositivos de cada habitación en paralelo. Si una habitación falla, conserva sus
    // datos actuales en vez de romper el listado; solo aplica si las habitaciones no cambiaron.
    private fun loadAllRoomDevices() {
        val rooms = _uiState.value.rooms
        if (rooms.isEmpty()) return
        viewModelScope.launch {
            val updated = coroutineScope {
                rooms.map { room ->
                    async {
                        when (val result = devicesRepository.getRoomDevices(room.id)) {
                            is AppResult.Success -> room.copy(devices = result.data)
                            is AppResult.Error -> room // fallo parcial: mantenemos la room tal cual
                        }
                    }
                }.awaitAll()
            }
            _uiState.update { state ->
                // Si el listado de habitaciones cambió mientras cargábamos, descartamos el resultado
                if (state.rooms.map { it.id } != updated.map { it.id }) state
                else state.copy(rooms = updated)
            }
        }
    }

    fun onToggleDevice(deviceId: String) {
        val device = findDevice(deviceId) ?: return
        // Update optimista para no frenar el switch; si la API falla, resincronizamos vía refetch
        _uiState.update { it.copy(rooms = it.rooms.withDeviceToggled(deviceId)) }
        viewModelScope.launch {
            when (val result = devicesRepository.toggleDevice(device)) {
                is AppResult.Success -> updateDeviceInState(result.data)
                is AppResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.error.kind.toMessageRes()) }
                    reloadRoomDevices(device.roomId)
                }
            }
        }
    }

    fun onDeviceValueChange(deviceId: String, value: Int) {
        val device = findDevice(deviceId) ?: return
        // Update optimista para que el slider responda; resincronizamos si la acción falla
        _uiState.update { it.copy(rooms = it.rooms.withDeviceValueChanged(deviceId, value)) }
        viewModelScope.launch {
            when (val result = devicesRepository.setDeviceValue(device, value)) {
                is AppResult.Success -> updateDeviceInState(result.data)
                is AppResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.error.kind.toMessageRes()) }
                    reloadRoomDevices(device.roomId)
                }
            }
        }
    }

    fun addDevices(roomId: String, devices: List<PendingDevice>) {
        if (devices.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            var lastError: ApiError? = null
            for (pending in devices) {
                val result = devicesRepository.createDevice(roomId, pending.type, pending.name, pending.qrId)
                if (result is AppResult.Error) lastError = result.error
            }
            // Refetch de la habitación para reflejar lo realmente persistido en el backend
            reloadRoomDevices(roomId)
            lastError?.let { err ->
                _uiState.update { it.copy(errorMessage = err.kind.toMessageRes()) }
            }
        }
    }

    // Alta de un dispositivo escaneado: resuelve la habitación (existente o nueva) y crea el device
    // con su qrId. Si hay que crear la room, espera su id antes de crear el dispositivo.
    fun addScannedDevice(
        target: ScannedDeviceRoomTarget,
        type: DeviceType,
        name: String,
        qrId: String?,
    ) {
        val currentHome = homeId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            val roomId = when (target) {
                is ScannedDeviceRoomTarget.Existing -> target.roomId
                is ScannedDeviceRoomTarget.New -> {
                    when (val created = roomsRepository.createRoom(currentHome, target.name, target.type)) {
                        is AppResult.Success -> created.data.id
                        is AppResult.Error -> {
                            _uiState.update { it.copy(errorMessage = created.error.kind.toMessageRes()) }
                            return@launch
                        }
                    }
                }
            }
            when (val result = devicesRepository.createDevice(roomId, type, name, qrId)) {
                is AppResult.Success -> Unit
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = result.error.kind.toMessageRes())
                }
            }
            refreshRooms()
        }
    }

    fun editDevice(deviceId: String, name: String) {
        viewModelScope.launch {
            when (val result = devicesRepository.renameDevice(deviceId, name)) {
                is AppResult.Success -> updateDeviceInState(result.data)
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = result.error.kind.toMessageRes())
                }
            }
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            when (val result = devicesRepository.deleteDevice(deviceId)) {
                // Solo removemos del estado si la API confirmó el borrado
                is AppResult.Success -> _uiState.update { it.copy(rooms = it.rooms.withoutDevice(deviceId)) }
                is AppResult.Error -> _uiState.update {
                    it.copy(errorMessage = result.error.kind.toMessageRes())
                }
            }
        }
    }

    private fun findDevice(deviceId: String): Device? =
        _uiState.value.rooms.flatMap { it.devices }.firstOrNull { it.id == deviceId }

    // Reemplaza un dispositivo puntual en el estado (tras una acción/edición exitosa)
    private fun updateDeviceInState(device: Device) = _uiState.update { state ->
        state.copy(
            rooms = state.rooms.map { room ->
                if (room.devices.none { it.id == device.id }) {
                    room
                } else {
                    room.copy(devices = room.devices.map { if (it.id == device.id) device else it })
                }
            },
        )
    }

    // Refetch silencioso de los dispositivos de una sola habitación (tras mutaciones o para resincronizar)
    private suspend fun reloadRoomDevices(roomId: String) {
        when (val result = devicesRepository.getRoomDevices(roomId)) {
            is AppResult.Success -> _uiState.update { state ->
                state.copy(rooms = state.rooms.map { if (it.id == roomId) it.copy(devices = result.data) else it })
            }
            is AppResult.Error -> Unit // error silencioso en refetch; el error principal ya se mostró
        }
    }

    private fun observeDeviceEvents() {
        viewModelScope.launch {
            deviceEvents.collect { event ->
                when (event) {
                    is SocketEvent.DeviceEvent -> onDeviceTouched(event.deviceId)
                    is SocketEvent.DeviceUpdated -> onDeviceTouched(event.deviceId)
                    is SocketEvent.DeviceCreated -> onDeviceStructureChanged(event.deviceId)
                    is SocketEvent.DeviceDeleted -> onDeviceStructureChanged(event.deviceId)
                    is SocketEvent.Connected -> Unit
                }
            }
        }
    }

    private suspend fun onDeviceTouched(deviceId: String?) {
        val roomId = roomIdForDevice(deviceId) ?: return
        reloadRoomDevices(roomId)
    }

    private suspend fun onDeviceStructureChanged(deviceId: String?) {
        val roomId = roomIdForDevice(deviceId)
        if (roomId != null) reloadRoomDevices(roomId) else refreshRooms()
    }

    private fun roomIdForDevice(deviceId: String?): String? {
        if (deviceId.isNullOrBlank()) return null
        return _uiState.value.rooms.firstOrNull { room -> room.devices.any { it.id == deviceId } }?.id
    }

    private companion object {
        val NoActiveHomeMessage = R.string.rooms_no_active_home
    }
}

class RoomsViewModelFactory(
    private val roomsRepository: RoomsRepository,
    private val devicesRepository: DevicesRepository,
    private val deviceEvents: Flow<SocketEvent>,
    private val preferences: PreferencesDataStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomsViewModel::class.java)) {
            return RoomsViewModel(roomsRepository, devicesRepository, deviceEvents, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
