package com.grupo4.smarthome.ui.screens.rooms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.DeviceType
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
    viewModel: RoomsViewModel,
    activeHomeId: String?,
    windowSize: WindowSizeClass? = null,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRoutines: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onQuickActionsOpen: () -> Unit = {},
) {
    val baseState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = baseState.copy(unreadNotificationCount = unreadNotificationCount)

    // Cargar/recargar cuando cambia el hogar activo (desde SelectHome / Preferences)
    LaunchedEffect(activeHomeId) { viewModel.onHomeChanged(activeHomeId) }

    // Estado de los diálogos: efímero y propio de la UI, vive en la screen
    var dialogKindName by rememberSaveable { mutableStateOf(RoomsDialogKind.None.name) }
    var dialogRoomId by rememberSaveable { mutableStateOf<String?>(null) }
    var dialogDeviceId by rememberSaveable { mutableStateOf<String?>(null) }

    val dialogKind = RoomsDialogKind.valueOf(dialogKindName)
    val dialogRoom = dialogRoomId?.let { id -> state.rooms.firstOrNull { it.id == id } }
    val dialogDevice = dialogDeviceId?.let { id ->
        state.rooms.flatMap { it.devices }.firstOrNull { it.id == id }
    }

    val callbacks = RoomsCallbacks(
        onBackToRooms = viewModel::onBackToRooms,
        onRoomClick = viewModel::onRoomClick,
        onSelectedRoomChange = viewModel::onSelectedRoomChange,
        onToggleRoomFavorite = viewModel::toggleRoomFavorite,
        onCreateRoomClick = {
            dialogRoomId = null
            dialogKindName = RoomsDialogKind.AddRoom.name
        },
        onEditRoomsToggle = viewModel::onEditRoomsToggle,
        onEditRoom = { roomId ->
            dialogRoomId = roomId
            dialogKindName = RoomsDialogKind.EditRoom.name
        },
        onDeleteRoom = { roomId ->
            dialogRoomId = roomId
            dialogKindName = RoomsDialogKind.DeleteRoom.name
        },
        onFilterSelected = viewModel::onFilterSelected,
        onSearchChange = viewModel::onSearchChange,
        onAddDeviceClick = { roomId ->
            dialogRoomId = roomId
            dialogDeviceId = null
            dialogKindName = RoomsDialogKind.AddDevices.name
        },
        onEditDevicesToggle = viewModel::onEditDevicesToggle,
        onEditDevice = { deviceId ->
            dialogDeviceId = deviceId
            dialogKindName = RoomsDialogKind.EditDevice.name
        },
        onDeleteDevice = { deviceId ->
            dialogDeviceId = deviceId
            dialogKindName = RoomsDialogKind.DeleteDevice.name
        },
        onToggleDevice = viewModel::onToggleDevice,
        onDeviceValueChange = viewModel::onDeviceValueChange,
        onAvatarClick = onProfileClick,
        onAddClick = {
            onQuickActionsOpen()
        },
        onNavigateHome = onNavigateHome,
        onNavigateRoutines = onNavigateRoutines,
        onNavigateHistory = onNavigateHistory,
        onNotificationsClick = onNotificationsClick,
    )

    Box(modifier = modifier.fillMaxSize()) {
        val errorText = state.errorMessage?.let { stringResource(it) }
        when {
            // Sin contenido + error (incluye "no hay hogar activo"): estado de error accionable
            state.rooms.isEmpty() && errorText != null -> {
                RoomsErrorState(
                    message = errorText,
                    onRetry = viewModel::loadRooms,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            // Carga inicial sin contenido todavía: loader simple
            state.rooms.isEmpty() && state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            else -> {
                RoomsContent(
                    windowSize = windowSize,
                    state = state,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxSize(),
                )
                // Errores no bloqueantes (fallos en mutaciones con contenido ya visible)
                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(errorText) {
                    if (errorText != null) {
                        snackbarHostState.showSnackbar(errorText)
                        viewModel.dismissError()
                    }
                }
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )
            }
        }
    }

    when (dialogKind) {
        RoomsDialogKind.None -> Unit
        RoomsDialogKind.AddRoom -> {
            AddEditRoomDialog(
                room = null,
                onDismiss = { dialogKindName = RoomsDialogKind.None.name },
                onConfirm = { name, type ->
                    viewModel.createRoom(name, type)
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
                        viewModel.renameRoom(room.id, name, type)
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
                        viewModel.deleteRoom(room.id)
                        dialogKindName = RoomsDialogKind.None.name
                    },
                )
            }
        }
        RoomsDialogKind.AddDevices -> {
            dialogRoom?.let { room ->
                AddDevicesToRoomDialog(
                    roomName = room.name,
                    availableDeviceTypes = DeviceType.entries,
                    onDismiss = { dialogKindName = RoomsDialogKind.None.name },
                    onConfirm = { pendingDevices ->
                        viewModel.addDevices(room.id, pendingDevices)
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
                        viewModel.editDevice(device.id, name)
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
                        viewModel.deleteDevice(device.id)
                        dialogKindName = RoomsDialogKind.None.name
                    },
                )
            }
        }
    }
}

// Estado de error mínimo y accionable, compatible con el resto de la pantalla
@Composable
private fun RoomsErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.rooms_retry))
            }
        }
    }
}
