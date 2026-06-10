package com.grupo4.smarthome.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.repository.DevicesRepository
import com.grupo4.smarthome.data.repository.RoomsRepository
import com.grupo4.smarthome.data.repository.RoutinesRepository
import com.grupo4.smarthome.ui.screens.dashboard.DashboardRoomPreview
import com.grupo4.smarthome.ui.screens.dashboard.DashboardRoutinePreview
import com.grupo4.smarthome.ui.screens.dashboard.DashboardUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val roomsRepository: RoomsRepository,
    private val devicesRepository: DevicesRepository,
    private val routinesRepository: RoutinesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        DashboardUiState(
            userName = "",
            selectedHomeName = "",
            activeDevicesCount = 0,
            roomsCount = 0,
            rooms = emptyList(),
            routines = emptyList(),
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var homeId: String? = null

    fun onHomeChanged(newHomeId: String?, homeName: String, userName: String) {
        _uiState.update { it.copy(selectedHomeName = homeName, userName = userName) }
        if (newHomeId == homeId) {
            if (newHomeId != null && _uiState.value.rooms.isEmpty() && !_uiState.value.isLoadingRooms) {
                load(newHomeId)
            }
            return
        }
        homeId = newHomeId
        _uiState.update { it.copy(rooms = emptyList(), routines = emptyList(), roomsCount = 0, activeDevicesCount = 0) }
        if (newHomeId != null) load(newHomeId)
    }

    fun refresh(homeName: String, userName: String) {
        _uiState.update { it.copy(selectedHomeName = homeName, userName = userName) }
        homeId?.let { load(it) }
    }

    private fun load(homeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRooms = true, isLoadingRoutines = true) }
            loadRoomsAndDevices(homeId)
            loadRoutines(homeId)
        }
    }

    private suspend fun loadRoomsAndDevices(homeId: String) {
        when (val result = roomsRepository.getRooms(homeId)) {
            is AppResult.Success -> {
                val rooms = result.data
                // Dispositivos por habitación en paralelo para contar los activos
                val devices = coroutineScope {
                    rooms.map { room ->
                        async {
                            when (val r = devicesRepository.getRoomDevices(room.id)) {
                                is AppResult.Success -> r.data
                                is AppResult.Error -> emptyList()
                            }
                        }
                    }.awaitAll()
                }.flatten()
                _uiState.update {
                    it.copy(
                        isLoadingRooms = false,
                        roomsCount = rooms.size,
                        activeDevicesCount = devices.count { d -> d.isActive },
                        rooms = rooms.map { room -> DashboardRoomPreview(room.id, room.name, room.isFavorite) },
                    )
                }
            }
            is AppResult.Error -> _uiState.update { it.copy(isLoadingRooms = false) }
        }
    }

    private suspend fun loadRoutines(homeId: String) {
        when (val result = routinesRepository.getRoutines(homeId)) {
            is AppResult.Success -> _uiState.update {
                it.copy(
                    isLoadingRoutines = false,
                    routines = result.data.map { r -> DashboardRoutinePreview(r.id, r.name, r.favorite) },
                )
            }
            is AppResult.Error -> _uiState.update { it.copy(isLoadingRoutines = false) }
        }
    }
}

class HomeViewModelFactory(
    private val roomsRepository: RoomsRepository,
    private val devicesRepository: DevicesRepository,
    private val routinesRepository: RoutinesRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(roomsRepository, devicesRepository, routinesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
