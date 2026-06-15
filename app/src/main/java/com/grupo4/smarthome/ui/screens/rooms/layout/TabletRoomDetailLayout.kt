package com.grupo4.smarthome.ui.screens.rooms.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.TabletMainSidebar
import com.grupo4.smarthome.ui.util.isPhoneLandscape
import com.grupo4.smarthome.ui.components.TabletSidebarDestination
import com.grupo4.smarthome.ui.screens.rooms.RoomsCallbacks
import com.grupo4.smarthome.ui.screens.rooms.RoomsPresentationState
import com.grupo4.smarthome.ui.screens.rooms.RoomsUiState
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCard
import com.grupo4.smarthome.ui.screens.rooms.components.RoomOption
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsEditIconButton
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsEmptyState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsNoDevicesEmptyState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsTopBar
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun TabletRoomDetailLayout(
    state: RoomsUiState,
    presentation: RoomsPresentationState,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    val room = presentation.selectedRoom ?: return
    val openDrawer = LocalOpenDrawer.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val portraitLike = maxWidth < 900.dp
        val phoneLandscape = isPhoneLandscape(maxWidth, maxHeight)
        val railCompact = portraitLike || phoneLandscape
        val contentPadding = if (portraitLike) 24.dp else 32.dp
        val deviceCardMinWidth = if (portraitLike) 340.dp else 420.dp
        val deviceCardHeight = 272.dp

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            TabletMainSidebar(
                selectedDestination = TabletSidebarDestination.Rooms,
                compact = railCompact,
                onNavigateHome = callbacks.onNavigateHome,
                onNavigateRoutines = callbacks.onNavigateRoutines,
                onNavigateRooms = callbacks.onBackToRooms,
                onNavigateHistory = callbacks.onNavigateHistory,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                RoomsTopBar(
                    onSearchClick = callbacks.onSearchClick,
                    onAvatarClick = callbacks.onAvatarClick,
                    onNotificationsClick = callbacks.onNotificationsClick,
                    notificationCount = state.unreadNotificationCount,
                    searchPlaceholder = stringResource(R.string.search_placeholder),
                    showHomeSelector = true,
                    onHomeSelectorClick = openDrawer,
                    useCompactHomeMenu = true,
                    modifier = Modifier.padding(
                        start = contentPadding,
                        top = 24.dp,
                        end = contentPadding,
                        bottom = 14.dp,
                    ),
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = deviceCardMinWidth),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = contentPadding,
                        end = contentPadding,
                        top = 8.dp,
                        bottom = 28.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RoomDetailHeader(
                            roomName = room.name,
                            activeDevices = presentation.selectedRoomActiveDevices,
                            devicesTotal = presentation.selectedRoomDevicesTotal,
                            editMode = state.editDevicesMode,
                            onBack = callbacks.onBackToRooms,
                            onAddDevice = { callbacks.onAddDeviceClick(room.id) },
                            onEditDevicesToggle = callbacks.onEditDevicesToggle,
                        )
                    }
                    if (!presentation.hasDevicesInSelectedRoom) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            RoomsNoDevicesEmptyState(
                                onAddDevice = { callbacks.onAddDeviceClick(room.id) },
                                addButtonLabel = stringResource(R.string.rooms_add_device),
                            )
                        }
                    } else if (presentation.hasNoDeviceResults) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            RoomsEmptyState(
                                title = stringResource(R.string.rooms_no_results_title),
                                description = stringResource(R.string.rooms_no_results_description),
                                buttonLabel = stringResource(R.string.rooms_clear_search),
                                onButtonClick = { callbacks.onSearchChange("") },
                            )
                        }
                    } else {
                        items(
                            items = presentation.visibleDevices,
                            key = { it.id },
                        ) { device ->
                            DeviceCard(
                                device = device,
                                editMode = state.editDevicesMode,
                                onToggle = { callbacks.onToggleDevice(device.id) },
                                onValueChange = { value -> callbacks.onDeviceValueChange(device.id, value) },
                                onEdit = { callbacks.onEditDevice(device.id) },
                                onDelete = { callbacks.onDeleteDevice(device.id) },
                                onAction = { name, args -> callbacks.onDeviceAction(device.id, name, args) },
                                modifier = Modifier.height(deviceCardHeight),
                                roomOptions = state.rooms.map { RoomOption(it.id, it.name) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomDetailHeader(
    roomName: String,
    activeDevices: Int,
    devicesTotal: Int,
    editMode: Boolean,
    onBack: () -> Unit,
    onAddDevice: () -> Unit,
    onEditDevicesToggle: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compactActions = maxWidth < 760.dp

        if (compactActions) {
            RoomDetailHeaderCopy(
                roomName = roomName,
                activeDevices = activeDevices,
                devicesTotal = devicesTotal,
                compact = true,
                onBack = onBack,
                titleTrailing = {
                    RoomDetailActions(
                        editMode = editMode,
                        compact = true,
                        onAddDevice = onAddDevice,
                        onEditDevicesToggle = onEditDevicesToggle,
                    )
                },
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                RoomDetailHeaderCopy(
                    roomName = roomName,
                    activeDevices = activeDevices,
                    devicesTotal = devicesTotal,
                    compact = false,
                    onBack = onBack,
                    modifier = Modifier.weight(1f),
                )
                RoomDetailActions(
                    editMode = editMode,
                    compact = false,
                    onAddDevice = onAddDevice,
                    onEditDevicesToggle = onEditDevicesToggle,
                )
            }
        }
    }
}

@Composable
private fun RoomDetailHeaderCopy(
    roomName: String,
    activeDevices: Int,
    devicesTotal: Int,
    compact: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    titleTrailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.height(if (compact) 40.dp else 48.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = headerButtonPadding(compact),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Back,
                contentDescription = null,
                modifier = Modifier.height(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.rooms_back_to_rooms),
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(if (compact) 12.dp else 14.dp))
        val roomNameText = @Composable { weightModifier: Modifier ->
            Text(
                text = roomName,
                modifier = weightModifier,
                style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (titleTrailing != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                roomNameText(Modifier.weight(1f))
                titleTrailing()
            }
        } else {
            roomNameText(Modifier)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.rooms_detail_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = pluralStringResource(
                R.plurals.rooms_detail_summary,
                devicesTotal,
                activeDevices,
                devicesTotal,
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun RoomDetailActions(
    editMode: Boolean,
    compact: Boolean,
    onAddDevice: () -> Unit,
    onEditDevicesToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomsEditIconButton(
            onClick = onEditDevicesToggle,
            contentDescription = stringResource(
                if (editMode) R.string.rooms_finish_editing else R.string.rooms_edit_devices,
            ),
            size = if (compact) 40.dp else 48.dp,
        )
        Button(
            onClick = onAddDevice,
            modifier = Modifier.height(if (compact) 40.dp else 48.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = headerButtonPadding(compact),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandIndigoDark,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Add,
                contentDescription = null,
                modifier = Modifier.height(if (compact) 18.dp else 20.dp),
            )
            Spacer(modifier = Modifier.width(if (compact) 6.dp else 8.dp))
            Text(
                text = stringResource(
                    if (compact) R.string.rooms_add_device_short else R.string.rooms_add_device,
                ),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun headerButtonPadding(compact: Boolean) =
    PaddingValues(horizontal = if (compact) 12.dp else 18.dp, vertical = 0.dp)
