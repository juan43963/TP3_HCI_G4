package com.grupo4.smarthome.ui.screens.search

import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.ui.screens.routines.RoutineUi
import com.grupo4.smarthome.ui.screens.routines.toRoutineUi

data class SearchUiState(
    val searchQuery: String = "",
)

data class SearchDeviceResult(
    val device: Device,
    val roomName: String,
)

data class SearchRoutineResult(
    val routine: Routine,
    val ui: RoutineUi,
)

data class SearchPresentationState(
    val query: String,
    val matchedRooms: List<Room>,
    val matchedDevices: List<SearchDeviceResult>,
    val matchedRoutines: List<SearchRoutineResult>,
) {
    val hasQuery: Boolean get() = query.isNotBlank()
    val hasResults: Boolean
        get() = matchedRooms.isNotEmpty() || matchedDevices.isNotEmpty() || matchedRoutines.isNotEmpty()
}

fun SearchUiState.toPresentationState(
    rooms: List<Room>,
    routines: List<Routine>,
): SearchPresentationState {
    val query = searchQuery.trim().lowercase()
    if (query.isBlank()) {
        return SearchPresentationState(
            query = query,
            matchedRooms = emptyList(),
            matchedDevices = emptyList(),
            matchedRoutines = emptyList(),
        )
    }

    val deviceNamesById = rooms
        .flatMap { it.devices }
        .associate { it.id to it.name }

    return SearchPresentationState(
        query = query,
        matchedRooms = rooms.filter { room -> room.matchesSearch(query) },
        matchedDevices = rooms.flatMap { room ->
            room.devices.mapNotNull { device ->
                if (device.matchesSearch(query)) {
                    SearchDeviceResult(device = device, roomName = room.name)
                } else {
                    null
                }
            }
        },
        matchedRoutines = routines
            .filter { routine -> routine.name.lowercase().contains(query) }
            .map { routine ->
                SearchRoutineResult(
                    routine = routine,
                    ui = routine.toRoutineUi(deviceNamesById),
                )
            },
    )
}

private fun Room.matchesSearch(query: String): Boolean =
    name.lowercase().contains(query) || devices.any { it.matchesSearch(query) }

private fun Device.matchesSearch(query: String): Boolean =
    name.lowercase().contains(query) || type.name.lowercase().contains(query)
