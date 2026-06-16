package com.grupo4.smarthome.ui.screens.rooms

import androidx.annotation.StringRes
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionNames
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.util.DeviceValueRanges

data class RoomsUiState(
    val rooms: List<Room>,
    val availableDeviceTypes: List<DeviceTypeInfo>,
    val searchQuery: String = "",
    val selectedRoomType: RoomType? = null,
    val selectedRoomId: String? = null,
    val editRoomsMode: Boolean = false,
    val editDevicesMode: Boolean = false,
    val openedFromDashboard: Boolean = false,
    val isLoading: Boolean = false,
    val devicesLoaded: Boolean = false,
    val unreadNotificationCount: Int = 0,
    @StringRes val errorMessage: Int? = null,
    val actionResult: String? = null,
)

data class RoomsPresentationState(
    val visibleRooms: List<Room>,
    val visibleDevices: List<Device>,
    val selectedRoom: Room?,
    val listDetailSelectedRoom: Room?,
    val listDetailVisibleDevices: List<Device>,
    val filterCounts: Map<RoomType, Int>,
    val totalRooms: Int,
    val totalActiveDevices: Int,
    val activeRoomsCount: Int,
    val isEmptyHome: Boolean,
    val hasNoResults: Boolean,
    val selectedRoomActiveDevices: Int,
    val selectedRoomDevicesTotal: Int,
    val hasDevicesInSelectedRoom: Boolean,
    val hasNoDeviceResults: Boolean,
    val listDetailSelectedRoomActiveDevices: Int,
    val listDetailSelectedRoomDevicesTotal: Int,
    val hasDevicesInListDetailSelectedRoom: Boolean,
    val hasNoListDetailDeviceResults: Boolean,
)

data class PendingDevice(
    val id: String,
    val type: DeviceTypeInfo,
    val name: String,
    val qrId: String? = null,
)

sealed interface ScannedDeviceRoomTarget {
    data class Existing(val roomId: String) : ScannedDeviceRoomTarget
    data class New(val name: String, val type: RoomType) : ScannedDeviceRoomTarget
}

fun RoomsUiState.toPresentationState(): RoomsPresentationState {
    val normalizedQuery = searchQuery.trim().lowercase()
    val filteredByType = selectedRoomType?.let { type ->
        rooms.filter { it.type == type }
    } ?: rooms
    val visibleRooms = if (normalizedQuery.isBlank()) {
        filteredByType
    } else {
        filteredByType.filter { room ->
            room.matchesRoomSearch(normalizedQuery)
        }
    }
    val selectedRoom = rooms.firstOrNull { it.id == selectedRoomId }
    val listDetailSelectedRoom = visibleRooms.firstOrNull { it.id == selectedRoomId }
        ?: visibleRooms.firstOrNull()
    val totalActiveDevices = rooms.sumOf { it.devicesActive }
    val selectedDevices = selectedRoom?.devices.orEmpty()
    val visibleDevices = if (normalizedQuery.isBlank()) {
        selectedDevices
    } else {
        selectedDevices.filter { device ->
            device.matchesDeviceSearch(normalizedQuery)
        }
    }
    val listDetailSelectedDevices = listDetailSelectedRoom?.devices.orEmpty()
    val listDetailVisibleDevices = when {
        normalizedQuery.isBlank() -> listDetailSelectedDevices
        listDetailSelectedRoom?.name?.lowercase()?.contains(normalizedQuery) == true -> listDetailSelectedDevices
        else -> listDetailSelectedDevices.filter { device -> device.matchesDeviceSearch(normalizedQuery) }
    }

    return RoomsPresentationState(
        visibleRooms = visibleRooms,
        visibleDevices = visibleDevices,
        selectedRoom = selectedRoom,
        listDetailSelectedRoom = listDetailSelectedRoom,
        listDetailVisibleDevices = listDetailVisibleDevices,
        filterCounts = RoomType.entries.associateWith { type -> rooms.count { it.type == type } },
        totalRooms = rooms.size,
        totalActiveDevices = totalActiveDevices,
        activeRoomsCount = rooms.count { it.devicesActive > 0 },
        isEmptyHome = rooms.isEmpty(),
        hasNoResults = rooms.isNotEmpty() && visibleRooms.isEmpty(),
        selectedRoomActiveDevices = selectedDevices.count { it.isActive },
        selectedRoomDevicesTotal = selectedDevices.size,
        hasDevicesInSelectedRoom = selectedDevices.isNotEmpty(),
        hasNoDeviceResults = selectedDevices.isNotEmpty() && visibleDevices.isEmpty(),
        listDetailSelectedRoomActiveDevices = listDetailSelectedDevices.count { it.isActive },
        listDetailSelectedRoomDevicesTotal = listDetailSelectedDevices.size,
        hasDevicesInListDetailSelectedRoom = listDetailSelectedDevices.isNotEmpty(),
        hasNoListDetailDeviceResults = listDetailSelectedDevices.isNotEmpty() && listDetailVisibleDevices.isEmpty(),
    )
}

private fun Room.matchesRoomSearch(query: String): Boolean =
    name.lowercase().contains(query) || devices.any { it.matchesDeviceSearch(query) }

private fun Device.matchesDeviceSearch(query: String): Boolean =
    name.lowercase().contains(query) || type.name.lowercase().contains(query)

fun List<Room>.withCreatedRoom(
    name: String,
    type: RoomType,
): List<Room> {
    val nextId = name
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "habitacion-${size + 1}" }

    return this + Room(
        id = uniqueRoomId(nextId),
        name = name.trim(),
        type = type,
    )
}

fun List<Room>.withEditedRoom(
    roomId: String,
    name: String,
    type: RoomType,
): List<Room> = map { room ->
    if (room.id == roomId) room.copy(name = name.trim(), type = type) else room
}

fun List<Room>.withoutRoom(roomId: String): List<Room> = filterNot { it.id == roomId }

fun List<Room>.withRoomFavoriteToggled(roomId: String): List<Room> = map { room ->
    if (room.id == roomId) room.copy(isFavorite = !room.isFavorite) else room
}

fun List<Room>.withDeviceToggled(deviceId: String): List<Room> = map { room ->
    room.copy(
        devices = room.devices.map { device ->
            when {
                device.id != deviceId -> device
                device.type.isDoor -> {
                    val nowLocked = !(device.locked ?: false)
                    device.copy(state = device.state.withString("lock", if (nowLocked) "locked" else "unlocked"))
                }
                else -> {
                    val nowOn = !device.isOn
                    device.copy(state = device.state.withString("status", if (nowOn) "on" else "off"))
                }
            }
        },
    )
}

fun List<Room>.withDeviceValueChanged(deviceId: String, value: Int): List<Room> = map { room ->
    room.copy(
        devices = room.devices.map { device ->
            if (device.id != deviceId) {
                device
            } else when (device.type.canonicalName) {
                DeviceTypeNames.LIGHT ->
                    device.copy(state = device.state.withInt("brightness", value.coerceIn(device.optimisticValueRange(DeviceValueRanges.LIGHT_BRIGHTNESS))))
                DeviceTypeNames.AC ->
                    device.copy(state = device.state.withInt("temperature", value.coerceIn(device.optimisticValueRange(DeviceValueRanges.AC_TEMPERATURE))))
                DeviceTypeNames.SPEAKER ->
                    device.copy(state = device.state.withInt("volume", value.coerceIn(device.optimisticValueRange(DeviceValueRanges.SPEAKER_VOLUME))))
                else -> device
            }
        },
    )
}

private fun Device.optimisticValueRange(fallback: IntRange): IntRange {
    val actionName = DeviceActionNames.primaryValueAction(type.canonicalName) ?: return fallback
    val param = type.action(actionName)?.params?.firstOrNull()
    val min = param?.min
    val max = param?.max
    return if (min != null && max != null) min..max else fallback
}

fun List<Room>.withoutDevice(deviceId: String): List<Room> = map { room ->
    room.copy(devices = room.devices.filterNot { it.id == deviceId })
}

private fun List<Room>.uniqueRoomId(baseId: String): String {
    if (none { it.id == baseId }) return baseId
    var suffix = 2
    while (any { it.id == "$baseId-$suffix" }) {
        suffix += 1
    }
    return "$baseId-$suffix"
}
