package com.grupo4.smarthome.ui.screens.search.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCard
import com.grupo4.smarthome.ui.screens.rooms.components.RoomCard
import com.grupo4.smarthome.ui.screens.routines.RoutineCard
import com.grupo4.smarthome.ui.screens.search.SearchCallbacks
import com.grupo4.smarthome.ui.screens.search.SearchPresentationState
import com.grupo4.smarthome.ui.screens.search.layout.phone.SearchEmptyState
import com.grupo4.smarthome.ui.screens.search.layout.phone.SearchSectionTitle

@Composable
fun TabletSearchLayout(
    presentation: SearchPresentationState,
    callbacks: SearchCallbacks,
    modifier: Modifier = Modifier,
) {
    if (!presentation.hasQuery) {
        SearchEmptyState(
            message = stringResource(R.string.search_empty_hint),
            modifier = modifier,
        )
        return
    }
    if (!presentation.hasResults) {
        SearchEmptyState(
            message = stringResource(R.string.search_no_results),
            modifier = modifier,
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 20.dp, bottom = 36.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (presentation.matchedRooms.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SearchSectionTitle(stringResource(R.string.search_section_rooms))
            }
            items(presentation.matchedRooms, key = { "room-${it.id}" }) { room ->
                RoomCard(
                    room = room,
                    editMode = false,
                    onClick = { callbacks.onRoomClick(room.id) },
                    onToggleFavorite = { callbacks.onToggleRoomFavorite(room.id) },
                    onEdit = {},
                    onDelete = {},
                )
            }
        }

        if (presentation.matchedDevices.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SearchSectionTitle(stringResource(R.string.search_section_devices))
            }
            items(presentation.matchedDevices, key = { "device-${it.device.id}" }) { result ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.search_device_room, result.roomName),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DeviceCard(
                        device = result.device,
                        editMode = false,
                        onToggle = { callbacks.onToggleDevice(result.device.id) },
                        onValueChange = { callbacks.onDeviceValueChange(result.device.id, it) },
                        onEdit = {},
                        onDelete = {},
                        onAction = { action, args ->
                            callbacks.onDeviceAction(result.device.id, action, args)
                        },
                    )
                }
            }
        }

        if (presentation.matchedRoutines.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SearchSectionTitle(stringResource(R.string.search_section_routines))
            }
            items(presentation.matchedRoutines, key = { "routine-${it.routine.id}" }) { result ->
                RoutineCard(
                    routine = result.ui,
                    onExecute = { callbacks.onRunRoutine(result.routine.id) },
                    onToggleFavorite = { callbacks.onToggleRoutineFavorite(result.routine.id) },
                )
            }
        }
    }
}
