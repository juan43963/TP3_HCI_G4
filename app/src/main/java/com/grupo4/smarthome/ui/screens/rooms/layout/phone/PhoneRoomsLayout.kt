package com.grupo4.smarthome.ui.screens.rooms.layout.phone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.SmartHomeTopBar
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardAvatarButton
import com.grupo4.smarthome.ui.screens.rooms.RoomsCallbacks
import com.grupo4.smarthome.ui.screens.rooms.RoomsPresentationState
import com.grupo4.smarthome.ui.screens.rooms.RoomsUiState
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCard
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCardVariant
import com.grupo4.smarthome.ui.screens.rooms.components.RoomCard
import com.grupo4.smarthome.ui.screens.rooms.components.RoomCardVariant
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsEditIconButton
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsFilterChips
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsHomeEmptyState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsNoDevicesEmptyState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsSummaryMetrics
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsSoftBorder
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun PhoneRoomsLayout(
    state: RoomsUiState,
    presentation: RoomsPresentationState,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalOpenDrawer.current
    val selectedRoom = presentation.selectedRoom
    var isSearchExpanded by rememberSaveable { mutableStateOf(state.searchQuery.isNotBlank()) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .animateContentSize(animationSpec = tween(durationMillis = 220))
                    .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 4.dp),
            ) {
                if (selectedRoom == null) {
                    SmartHomeTopBar(
                        title = stringResource(R.string.nav_rooms),
                        onMenuClick = openDrawer,
                        onSearchClick = { isSearchExpanded = true },
                        onProfileClick = callbacks.onAvatarClick,
                        onNotificationsClick = callbacks.onNotificationsClick,
                        notificationCount = state.unreadNotificationCount,
                    )
                } else {
                    PhoneRoomDetailTopBar(
                        roomName = selectedRoom.name,
                        onBack = {
                            callbacks.onSearchChange("")
                            callbacks.onBackToRooms()
                        },
                        onSearchClick = { isSearchExpanded = true },
                        onProfileClick = callbacks.onAvatarClick,
                    )
                }
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = fadeIn(animationSpec = tween(140)) +
                        expandVertically(
                            animationSpec = tween(220),
                            expandFrom = Alignment.Top,
                        ),
                    exit = fadeOut(animationSpec = tween(120)) +
                        shrinkVertically(
                            animationSpec = tween(180),
                            shrinkTowards = Alignment.Top,
                        ),
                ) {
                    PhoneRoomsSearchRow(
                        query = state.searchQuery,
                        onQueryChange = callbacks.onSearchChange,
                        placeholder = stringResource(
                            if (selectedRoom == null) {
                                R.string.rooms_search_placeholder
                            } else {
                                R.string.rooms_devices_search_placeholder
                            },
                        ),
                        onClose = {
                            callbacks.onSearchChange("")
                            isSearchExpanded = false
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            PhoneRoomsFab(
                onClick = {
                    val room = selectedRoom
                    if (room == null) callbacks.onAddClick()
                    else callbacks.onAddDeviceClick(room.id)
                },
            )
        },
    ) { innerPadding ->
        if (selectedRoom == null) {
            PhoneRoomsList(
                state = state,
                presentation = presentation,
                callbacks = callbacks,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            PhoneRoomDetail(
                state = state,
                presentation = presentation,
                room = selectedRoom,
                callbacks = callbacks,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun PhoneRoomsList(
    state: RoomsUiState,
    presentation: RoomsPresentationState,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = PhoneContentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PhoneRoomsHeader(
                totalActiveDevices = presentation.totalActiveDevices,
                roomsCount = presentation.totalRooms,
                editMode = state.editRoomsMode,
                onEditRoomsToggle = callbacks.onEditRoomsToggle,
                showEditButton = !presentation.isEmptyHome,
            )
        }
        item {
            RoomsFilterChips(
                selectedRoomType = state.selectedRoomType,
                filterCounts = presentation.filterCounts,
                totalRooms = presentation.totalRooms,
                onFilterSelected = callbacks.onFilterSelected,
                compact = true,
                showCounts = true,
                showCategoryLabel = true,
            )
        }
        when {
            presentation.isEmptyHome -> {
                item {
                    RoomsHomeEmptyState(
                        buttonLabel = stringResource(R.string.rooms_empty_cta),
                        onCreateRoomClick = callbacks.onCreateRoomClick,
                        compact = true,
                    )
                }
            }
            presentation.hasNoResults -> {
                item {
                    PhoneRoomsEmptyState(
                        title = stringResource(R.string.rooms_phone_no_results_title),
                        description = stringResource(R.string.rooms_phone_no_results_description),
                        buttonLabel = stringResource(R.string.rooms_clear_search),
                        onButtonClick = { callbacks.onSearchChange("") },
                    )
                }
            }
            else -> {
                items(
                    items = presentation.visibleRooms,
                    key = { it.id },
                ) { room ->
                    RoomCard(
                        room = room,
                        editMode = state.editRoomsMode,
                        onClick = { callbacks.onRoomClick(room.id) },
                        onToggleFavorite = { callbacks.onToggleRoomFavorite(room.id) },
                        onEdit = { callbacks.onEditRoom(room.id) },
                        onDelete = { callbacks.onDeleteRoom(room.id) },
                        variant = RoomCardVariant.Phone,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneRoomDetail(
    state: RoomsUiState,
    presentation: RoomsPresentationState,
    room: Room,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = PhoneContentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PhoneRoomDetailHeader(
                activeDevices = presentation.selectedRoomActiveDevices,
                devicesTotal = presentation.selectedRoomDevicesTotal,
                editMode = state.editDevicesMode,
                showEditButton = presentation.hasDevicesInSelectedRoom,
                onEditDevicesToggle = callbacks.onEditDevicesToggle,
            )
        }
        when {
            !presentation.hasDevicesInSelectedRoom -> {
                item {
                    RoomsNoDevicesEmptyState(
                        compact = true,
                        onAddDevice = { callbacks.onAddDeviceClick(room.id) },
                        addButtonLabel = stringResource(R.string.rooms_add_device),
                    )
                }
            }
            presentation.hasNoDeviceResults -> {
                item {
                    PhoneRoomsEmptyState(
                        title = stringResource(R.string.rooms_phone_no_device_results_title),
                        description = stringResource(R.string.rooms_phone_no_device_results_description),
                        buttonLabel = stringResource(R.string.rooms_clear_search),
                        onButtonClick = { callbacks.onSearchChange("") },
                    )
                }
            }
            else -> {
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
                        variant = DeviceCardVariant.Phone,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneRoomsHeader(
    totalActiveDevices: Int,
    roomsCount: Int,
    editMode: Boolean,
    onEditRoomsToggle: () -> Unit,
    showEditButton: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.rooms_header_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.rooms_header_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (showEditButton) {
                PhoneEditAction(
                    editMode = editMode,
                    onClick = onEditRoomsToggle,
                )
            }
        }
        RoomsSummaryMetrics(
            totalActiveDevices = totalActiveDevices,
            activeRoomsCount = roomsCount,
        )
    }
}

@Composable
private fun PhoneRoomDetailHeader(
    activeDevices: Int,
    devicesTotal: Int,
    editMode: Boolean,
    showEditButton: Boolean,
    onEditDevicesToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.rooms_detail_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
                Text(
                    text = stringResource(R.string.rooms_detail_summary, activeDevices, devicesTotal),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showEditButton) {
            RoomsEditIconButton(
                onClick = onEditDevicesToggle,
                contentDescription = stringResource(
                    if (editMode) R.string.rooms_finish_editing_short else R.string.rooms_edit_short,
                ),
            )
        }
    }
}

@Composable
private fun PhoneEditAction(
    editMode: Boolean,
    onClick: () -> Unit,
) {
    RoomsEditIconButton(
        onClick = onClick,
        contentDescription = stringResource(
            if (editMode) R.string.rooms_finish_editing else R.string.rooms_edit_rooms,
        ),
    )
}

@Composable
private fun PhoneRoomDetailTopBar(
    roomName: String,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = SmartHomeIcons.Back,
                contentDescription = stringResource(R.string.rooms_back_to_rooms),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = roomName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = SmartHomeIcons.Search,
                contentDescription = stringResource(R.string.dashboard_search_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        DashboardAvatarButton(onClick = onProfileClick)
    }
}

@Composable
private fun PhoneRoomsSearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PhoneRoomsSearchField(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = SmartHomeIcons.Close,
                contentDescription = stringResource(R.string.dashboard_search_close_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PhoneRoomsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                )
            }
        }
    }
}

@Composable
private fun PhoneRoomsEmptyState(
    title: String,
    description: String,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RoomsSoftBorder),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Rooms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigoDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(text = buttonLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PhoneRoomsFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = BrandIndigoDark,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(
            imageVector = SmartHomeIcons.Add,
            contentDescription = stringResource(R.string.rooms_add_content_description),
        )
    }
}

private val PhoneContentPadding = PaddingValues(
    start = 16.dp,
    top = 12.dp,
    end = 16.dp,
    bottom = 28.dp,
)
