package com.grupo4.smarthome.ui.screens.rooms

import com.grupo4.smarthome.data.model.ActionArg
import com.grupo4.smarthome.data.model.RoomType

data class RoomsCallbacks(
    val onBackToRooms: () -> Unit = {},
    val onRoomClick: (String) -> Unit = {},
    val onSelectedRoomChange: (String?) -> Unit = {},
    val onToggleRoomFavorite: (String) -> Unit = {},
    val onCreateRoomClick: () -> Unit = {},
    val onEditRoomsToggle: () -> Unit = {},
    val onEditRoom: (String) -> Unit = {},
    val onDeleteRoom: (String) -> Unit = {},
    val onFilterSelected: (RoomType?) -> Unit = {},
    val onSearchChange: (String) -> Unit = {},
    val onAddDeviceClick: (String) -> Unit = {},
    val onEditDevicesToggle: () -> Unit = {},
    val onEditDevice: (String) -> Unit = {},
    val onDeleteDevice: (String) -> Unit = {},
    val onToggleDevice: (String) -> Unit = {},
    val onDeviceValueChange: (String, Int) -> Unit = { _, _ -> },
    // Ejecuta cualquier acción del catálogo desde el panel "Más acciones": (deviceId, actionName, args)
    val onDeviceAction: (String, String, List<ActionArg>) -> Unit = { _, _, _ -> },
    val onAvatarClick: () -> Unit = {},
    val onAddClick: () -> Unit = {},
    val onNavigateHome: () -> Unit = {},
    val onNavigateRoutines: () -> Unit = {},
    val onNavigateHistory: () -> Unit = {},
    val onNotificationsClick: () -> Unit = {},
)
