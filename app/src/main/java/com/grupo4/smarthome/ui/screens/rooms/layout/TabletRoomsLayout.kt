package com.grupo4.smarthome.ui.screens.rooms.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.TabletMainSidebar
import com.grupo4.smarthome.ui.components.TabletSidebarDestination
import com.grupo4.smarthome.ui.screens.rooms.RoomsCallbacks
import com.grupo4.smarthome.ui.screens.rooms.RoomsPresentationState
import com.grupo4.smarthome.ui.screens.rooms.RoomsUiState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomCard
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsEmptyState
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsFilterChips
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsHeader
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsTopBar

@Composable
fun TabletRoomsLayout(
    state: RoomsUiState,
    presentation: RoomsPresentationState,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val portraitLike = maxWidth < 900.dp
        val contentPadding = if (portraitLike) 24.dp else 32.dp

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            TabletMainSidebar(
                selectedDestination = TabletSidebarDestination.Rooms,
                compact = portraitLike,
                onNavigateHome = callbacks.onNavigateHome,
                onNavigateRoutines = callbacks.onNavigateRoutines,
                onNavigateRooms = {},
                onNavigateHistory = callbacks.onNavigateHistory,
                onAddClick = callbacks.onAddClick,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                RoomsTopBar(
                    searchQuery = state.searchQuery,
                    onSearchChange = callbacks.onSearchChange,
                    onAvatarClick = callbacks.onAvatarClick,
                    searchPlaceholder = stringResource(R.string.rooms_search_placeholder),
                    modifier = Modifier.padding(
                        start = contentPadding,
                        top = 24.dp,
                        end = contentPadding,
                        bottom = 14.dp,
                    ),
                )
                if (state.isLoading) {
                    RoomsLoadingState(modifier = Modifier.fillMaxSize())
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 226.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = contentPadding,
                            end = contentPadding,
                            top = 8.dp,
                            bottom = 28.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            RoomsHeader(
                                totalActiveDevices = presentation.totalActiveDevices,
                                activeRoomsCount = presentation.activeRoomsCount,
                                editMode = state.editRoomsMode,
                                onCreateRoomClick = callbacks.onCreateRoomClick,
                                onEditRoomsToggle = callbacks.onEditRoomsToggle,
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            RoomsFilterChips(
                                selectedRoomType = state.selectedRoomType,
                                filterCounts = presentation.filterCounts,
                                totalRooms = presentation.totalRooms,
                                onFilterSelected = callbacks.onFilterSelected,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (presentation.isEmptyHome) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                RoomsEmptyState(
                                    title = stringResource(R.string.rooms_empty_title),
                                    description = stringResource(R.string.rooms_empty_description),
                                    buttonLabel = stringResource(R.string.rooms_empty_cta),
                                    onButtonClick = callbacks.onCreateRoomClick,
                                )
                            }
                        } else if (presentation.hasNoResults) {
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
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomsLoadingState(modifier: Modifier = Modifier) {
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
