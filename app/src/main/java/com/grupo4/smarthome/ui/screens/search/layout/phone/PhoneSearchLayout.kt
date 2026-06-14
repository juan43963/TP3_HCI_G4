package com.grupo4.smarthome.ui.screens.search.layout.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCard
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCardVariant
import com.grupo4.smarthome.ui.screens.rooms.components.RoomCard
import com.grupo4.smarthome.ui.screens.rooms.components.RoomCardVariant
import com.grupo4.smarthome.ui.screens.routines.RoutineCard
import com.grupo4.smarthome.ui.screens.search.SearchCallbacks
import com.grupo4.smarthome.ui.screens.search.SearchPresentationState
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun PhoneSearchLayout(
    presentation: SearchPresentationState,
    callbacks: SearchCallbacks,
    modifier: Modifier = Modifier,
) {
    if (!presentation.hasQuery) {
        SearchEmptyState(
            title = stringResource(R.string.search_empty_hint),
            subtitle = stringResource(R.string.search_empty_subtitle),
            modifier = modifier,
        )
        return
    }
    if (!presentation.hasResults) {
        SearchEmptyState(
            title = stringResource(R.string.search_no_results),
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (presentation.matchedRooms.isNotEmpty()) {
            item { SearchSectionTitle(stringResource(R.string.search_section_rooms)) }
            items(presentation.matchedRooms, key = { "room-${it.id}" }) { room ->
                RoomCard(
                    room = room,
                    editMode = false,
                    onClick = { callbacks.onRoomClick(room.id) },
                    onToggleFavorite = { callbacks.onToggleRoomFavorite(room.id) },
                    onEdit = {},
                    onDelete = {},
                    variant = RoomCardVariant.Phone,
                )
            }
        }

        if (presentation.matchedDevices.isNotEmpty()) {
            item { SearchSectionTitle(stringResource(R.string.search_section_devices)) }
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
                        variant = DeviceCardVariant.Phone,
                        onAction = { action, args ->
                            callbacks.onDeviceAction(result.device.id, action, args)
                        },
                    )
                }
            }
        }

        if (presentation.matchedRoutines.isNotEmpty()) {
            item { SearchSectionTitle(stringResource(R.string.search_section_routines)) }
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

@Composable
internal fun SearchSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    )
}

@Composable
internal fun SearchEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
