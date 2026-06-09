package com.grupo4.smarthome.ui.screens.rooms

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddDevicesToRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddEditRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.DeleteDeviceDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.DeleteRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.EditDeviceDialog

private enum class RoomsDialogKind {
    None,
    AddRoom,
    EditRoom,
    DeleteRoom,
    AddDevices,
    EditDevice,
    DeleteDevice,
}

@Composable
fun RoomsScreen(
    windowSize: WindowSizeClass? = null,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRoutines: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
) {
    var rooms by remember { mutableStateOf(RoomsPreviewData.populated.rooms) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedRoomTypeName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedRoomId by rememberSaveable { mutableStateOf<String?>(null) }
    var editRoomsMode by rememberSaveable { mutableStateOf(false) }
    var editDevicesMode by rememberSaveable { mutableStateOf(false) }
    var dialogKindName by rememberSaveable { mutableStateOf(RoomsDialogKind.None.name) }
    var dialogRoomId by rememberSaveable { mutableStateOf<String?>(null) }
    var dialogDeviceId by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedRoomType = selectedRoomTypeName?.let(RoomType::valueOf)
    val dialogKind = RoomsDialogKind.valueOf(dialogKindName)
    val dialogRoom = dialogRoomId?.let { id -> rooms.firstOrNull { it.id == id } }
    val dialogDevice = dialogDeviceId?.let { id ->
        rooms.flatMap { it.devices }.firstOrNull { it.id == id }
    }
    val state = RoomsUiState(
        rooms = rooms,
        availableDeviceTypes = RoomsPreviewData.availableDeviceTypes,
        searchQuery = searchQuery,
        selectedRoomType = selectedRoomType,
        selectedRoomId = selectedRoomId,
        editRoomsMode = editRoomsMode,
        editDevicesMode = editDevicesMode,
        unreadNotificationCount = unreadNotificationCount,
    )

    RoomsContent(
        windowSize = windowSize,
        state = state,
        callbacks = RoomsCallbacks(
            onBackToRooms = {
                selectedRoomId = null
                editDevicesMode = false
            },
            onRoomClick = { roomId ->
                selectedRoomId = roomId
                searchQuery = ""
            },
            onSelectedRoomChange = { roomId ->
                selectedRoomId = roomId
                if (roomId == null) {
                    editDevicesMode = false
                }
            },
            onToggleRoomFavorite = { roomId ->
                rooms = rooms.withRoomFavoriteToggled(roomId)
            },
            onCreateRoomClick = {
                dialogRoomId = null
                dialogKindName = RoomsDialogKind.AddRoom.name
            },
            onEditRoomsToggle = { editRoomsMode = !editRoomsMode },
            onEditRoom = { roomId ->
                dialogRoomId = roomId
                dialogKindName = RoomsDialogKind.EditRoom.name
            },
            onDeleteRoom = { roomId ->
                dialogRoomId = roomId
                dialogKindName = RoomsDialogKind.DeleteRoom.name
            },
            onFilterSelected = { type -> selectedRoomTypeName = type?.name },
            onSearchChange = { searchQuery = it },
            onAddDeviceClick = { roomId ->
                dialogRoomId = roomId
                dialogDeviceId = null
                dialogKindName = RoomsDialogKind.AddDevices.name
            },
            onEditDevicesToggle = { editDevicesMode = !editDevicesMode },
            onEditDevice = { deviceId ->
                dialogDeviceId = deviceId
                dialogKindName = RoomsDialogKind.EditDevice.name
            },
            onDeleteDevice = { deviceId ->
                dialogDeviceId = deviceId
                dialogKindName = RoomsDialogKind.DeleteDevice.name
            },
            onToggleDevice = { deviceId -> rooms = rooms.withDeviceToggled(deviceId) },
            onDeviceValueChange = { deviceId, value -> rooms = rooms.withDeviceValueChanged(deviceId, value) },
            onAvatarClick = onProfileClick,
            onAddClick = {
                dialogRoomId = selectedRoomId
                dialogKindName = if (selectedRoomId == null) {
                    RoomsDialogKind.AddRoom.name
                } else {
                    RoomsDialogKind.AddDevices.name
                }
            },
            onNavigateHome = onNavigateHome,
            onNavigateRoutines = onNavigateRoutines,
            onNavigateHistory = onNavigateHistory,
            onNotificationsClick = onNotificationsClick,
        ),
        modifier = modifier,
    )

    when (dialogKind) {
        RoomsDialogKind.None -> Unit
        RoomsDialogKind.AddRoom -> {
            AddEditRoomDialog(
                room = null,
                onDismiss = { dialogKindName = RoomsDialogKind.None.name },
                onConfirm = { name, type ->
                    rooms = rooms.withCreatedRoom(name, type)
                    dialogKindName = RoomsDialogKind.None.name
                },
            )
        }
        RoomsDialogKind.EditRoom -> {
            dialogRoom?.let { room ->
                AddEditRoomDialog(
                    room = room,
                    onDismiss = { dialogKindName = RoomsDialogKind.None.name },
                    onConfirm = { name, type ->
                        rooms = rooms.withEditedRoom(room.id, name, type)
                        dialogKindName = RoomsDialogKind.None.name
                    },
                )
            }
        }
        RoomsDialogKind.DeleteRoom -> {
            dialogRoom?.let { room ->
                DeleteRoomDialog(
                    roomName = room.name,
                    onDismiss = { dialogKindName = RoomsDialogKind.None.name },
                    onConfirm = {
                        rooms = rooms.withoutRoom(room.id)
                        if (selectedRoomId == room.id) {
                            selectedRoomId = null
                        }
                        dialogKindName = RoomsDialogKind.None.name
                    },
                )
            }
        }
        RoomsDialogKind.AddDevices -> {
            dialogRoom?.let { room ->
                AddDevicesToRoomDialog(
                    roomName = room.name,
                    availableDeviceTypes = RoomsPreviewData.availableDeviceTypes,
                    onDismiss = { dialogKindName = RoomsDialogKind.None.name },
                    onConfirm = { pendingDevices ->
                        rooms = rooms.withAddedDevices(room.id, pendingDevices)
                        dialogKindName = RoomsDialogKind.None.name
                    },
                )
            }
        }
        RoomsDialogKind.EditDevice -> {
            dialogDevice?.let { device ->
                EditDeviceDialog(
                    device = device,
                    onDismiss = { dialogKindName = RoomsDialogKind.None.name },
                    onConfirm = { name ->
                        rooms = rooms.withEditedDevice(device.id, name)
                        dialogKindName = RoomsDialogKind.None.name
                    },
                )
            }
        }
        RoomsDialogKind.DeleteDevice -> {
            dialogDevice?.let { device ->
                DeleteDeviceDialog(
                    deviceName = device.name,
                    onDismiss = { dialogKindName = RoomsDialogKind.None.name },
                    onConfirm = {
                        rooms = rooms.withoutDevice(device.id)
                        dialogKindName = RoomsDialogKind.None.name
                    },
                )
            }
        }
    }
}
