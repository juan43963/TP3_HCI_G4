package com.grupo4.smarthome.ui.screens.rooms.layout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.TabletMainSidebar
import com.grupo4.smarthome.ui.components.TabletPanelSurface
import com.grupo4.smarthome.ui.components.TabletSidebarDestination
import com.grupo4.smarthome.ui.screens.rooms.RoomsCallbacks
import com.grupo4.smarthome.ui.screens.rooms.RoomsPresentationState
import com.grupo4.smarthome.ui.screens.rooms.RoomsUiState
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCard
import com.grupo4.smarthome.ui.screens.rooms.components.RoomCard
import com.grupo4.smarthome.ui.screens.rooms.components.RoomCardVariant
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsEditIconButton
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsEmptyState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsFilterChips
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsHeader
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsHomeEmptyState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsNoDevicesEmptyState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsTopBar
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.BorderDefault
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TabletRoomsListDetailLayout(
    state: RoomsUiState,
    presentation: RoomsPresentationState,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    val openDrawer = LocalOpenDrawer.current
    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    val listCallbacks = remember(callbacks, navigator, scope) {
        callbacks.copy(
            onSelectedRoomChange = { id ->
                callbacks.onSelectedRoomChange(id)
                if (id != null) {
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id) }
                }
            },
        )
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TabletMainSidebar(
            selectedDestination = TabletSidebarDestination.Rooms,
            compact = false,
            onNavigateHome = callbacks.onNavigateHome,
            onNavigateRoutines = callbacks.onNavigateRoutines,
            onNavigateRooms = {},
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
                    start = 28.dp,
                    top = 22.dp,
                    end = 28.dp,
                    bottom = 14.dp,
                ),
            )

            if (state.isLoading) {
                ListDetailLoadingState(modifier = Modifier.fillMaxSize())
            } else {
                ListDetailPaneScaffold(
                    directive = navigator.scaffoldDirective,
                    value = navigator.scaffoldValue,
                    listPane = {
                        AnimatedPane {
                            RoomsListPanel(
                                state = state,
                                presentation = presentation,
                                callbacks = listCallbacks,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    },
                    detailPane = {
                        AnimatedPane {
                            RoomDetailPanel(
                                state = state,
                                presentation = presentation,
                                callbacks = callbacks,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 28.dp, end = 28.dp, bottom = 28.dp),
                )
            }
        }
    }
}

@Composable
private fun RoomsListPanel(
    state: RoomsUiState,
    presentation: RoomsPresentationState,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    ListDetailPanelSurface(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RoomsHeader(
                totalActiveDevices = presentation.totalActiveDevices,
                activeRoomsCount = presentation.activeRoomsCount,
                editMode = state.editRoomsMode,
                onCreateRoomClick = callbacks.onCreateRoomClick,
                onEditRoomsToggle = callbacks.onEditRoomsToggle,
                showEditButton = !presentation.isEmptyHome,
            )
            RoomsFilterChips(
                selectedRoomType = state.selectedRoomType,
                filterCounts = presentation.filterCounts,
                totalRooms = presentation.totalRooms,
                onFilterSelected = callbacks.onFilterSelected,
                compact = true,
                showCategoryLabel = true,
            )
            when {
                presentation.isEmptyHome -> {
                    RoomsHomeEmptyState(
                        buttonLabel = stringResource(R.string.rooms_empty_cta),
                        onCreateRoomClick = callbacks.onCreateRoomClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
                presentation.hasNoResults -> {
                    RoomsEmptyState(
                        title = stringResource(R.string.rooms_no_results_title),
                        description = stringResource(R.string.rooms_no_results_description),
                        buttonLabel = stringResource(R.string.rooms_clear_search),
                        onButtonClick = { callbacks.onSearchChange("") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 4.dp),
                    ) {
                        items(
                            items = presentation.visibleRooms,
                            key = { it.id },
                        ) { room ->
                            RoomCard(
                                room = room,
                                editMode = state.editRoomsMode,
                                onClick = { callbacks.onSelectedRoomChange(room.id) },
                                onToggleFavorite = { callbacks.onToggleRoomFavorite(room.id) },
                                onEdit = { callbacks.onEditRoom(room.id) },
                                onDelete = { callbacks.onDeleteRoom(room.id) },
                                variant = RoomCardVariant.Compact,
                                selected = room.id == presentation.listDetailSelectedRoom?.id,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomDetailPanel(
    state: RoomsUiState,
    presentation: RoomsPresentationState,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    val room = presentation.listDetailSelectedRoom

    ListDetailPanelSurface(modifier = modifier) {
        if (room == null) {
            if (presentation.isEmptyHome) {
                RoomsNoDevicesEmptyState(
                    card = false,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                NoRoomSelectedState(
                    isEmptyHome = false,
                    onCreateRoomClick = callbacks.onCreateRoomClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            return@ListDetailPanelSurface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 26.dp, top = 26.dp, end = 26.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ListDetailRoomHeader(
                room = room,
                activeDevices = presentation.listDetailSelectedRoomActiveDevices,
                devicesTotal = presentation.listDetailSelectedRoomDevicesTotal,
                editMode = state.editDevicesMode,
                onAddDevice = { callbacks.onAddDeviceClick(room.id) },
                onEditDevicesToggle = callbacks.onEditDevicesToggle,
            )

            when {
                !presentation.hasDevicesInListDetailSelectedRoom -> {
                    RoomsNoDevicesEmptyState(
                        onAddDevice = { callbacks.onAddDeviceClick(room.id) },
                        addButtonLabel = stringResource(R.string.rooms_add_device),
                    )
                }
                presentation.hasNoListDetailDeviceResults -> {
                    RoomsEmptyState(
                        title = stringResource(R.string.rooms_no_results_title),
                        description = stringResource(R.string.rooms_no_results_description),
                        buttonLabel = stringResource(R.string.rooms_clear_search),
                        onButtonClick = { callbacks.onSearchChange("") },
                    )
                }
                else -> {
                    val deviceCardHeight = 248.dp
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 250.dp),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 4.dp),
                    ) {
                        gridItems(
                            items = presentation.listDetailVisibleDevices,
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
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListDetailRoomHeader(
    room: Room,
    activeDevices: Int,
    devicesTotal: Int,
    editMode: Boolean,
    onAddDevice: () -> Unit,
    onEditDevicesToggle: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compactActions = maxWidth < 620.dp

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = room.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                RoomDetailActions(
                    editMode = editMode,
                    compact = compactActions,
                    onAddDevice = onAddDevice,
                    onEditDevicesToggle = onEditDevicesToggle,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.rooms_detail_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.rooms_detail_summary, activeDevices, devicesTotal),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
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
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomsEditIconButton(
            onClick = onEditDevicesToggle,
            contentDescription = stringResource(
                if (editMode) R.string.rooms_finish_editing else R.string.rooms_edit_devices,
            ),
            size = if (compact) 40.dp else 44.dp,
        )
        Button(
            onClick = onAddDevice,
            modifier = Modifier.height(if (compact) 40.dp else 44.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = if (compact) 12.dp else 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandIndigoDark,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.rooms_add_device),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun NoRoomSelectedState(
    isEmptyHome: Boolean,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Rooms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                text = stringResource(
                    if (isEmptyHome) R.string.rooms_select_or_create_room_title
                    else R.string.rooms_no_room_selected_title,
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    if (isEmptyHome) R.string.rooms_select_or_create_room_description
                    else R.string.rooms_no_room_selected_description,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isEmptyHome) {
                Button(
                    onClick = onCreateRoomClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandIndigoDark,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(text = stringResource(R.string.rooms_create_room), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ListDetailPanelSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TabletPanelSurface(modifier = modifier, content = content)
}

@Composable
private fun ListDetailLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.rooms_loading),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
