package com.grupo4.smarthome.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardPresentationState

@Composable
fun DashboardBodyContent(
    presentation: DashboardPresentationState,
    callbacks: DashboardCallbacks,
    modifier: Modifier = Modifier,
) {
    if (presentation.hasNoRooms) {
        EmptyStateCard(
            title = stringResource(R.string.dashboard_empty_home_title),
            description = stringResource(R.string.dashboard_empty_home_description),
            buttonLabel = stringResource(R.string.dashboard_create_room),
            onButtonClick = callbacks.onCreateRoom,
            note = stringResource(R.string.dashboard_empty_home_note),
            visual = DashboardEmptyVisual.Home,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        DashboardSection(
            title = stringResource(R.string.dashboard_favorite_rooms_title),
            subtitle = stringResource(R.string.dashboard_favorite_rooms_subtitle),
            actionLabel = stringResource(R.string.dashboard_create_room),
            onActionClick = callbacks.onCreateRoom,
        ) {
            if (presentation.hasRoomsWithoutFavorites) {
                InfoEmptyStateCard(text = stringResource(R.string.dashboard_empty_favorite_rooms))
            } else {
                DashboardAdaptiveGrid(
                    itemCount = presentation.favoriteRooms.size,
                    itemContent = { index ->
                        FavoriteRoomPlaceholderCard(room = presentation.favoriteRooms[index])
                    },
                )
            }
        }

        DashboardSection(
            title = stringResource(R.string.dashboard_favorite_routines_title),
            subtitle = stringResource(R.string.dashboard_favorite_routines_subtitle),
            actionLabel = stringResource(R.string.dashboard_create_routine),
            onActionClick = callbacks.onCreateRoutine,
        ) {
            when {
                presentation.hasNoRoutines -> {
                    EmptyStateCard(
                        title = stringResource(R.string.dashboard_empty_routines_title),
                        description = stringResource(R.string.dashboard_empty_routines_description),
                        example = stringResource(R.string.dashboard_empty_routines_example),
                        helpText = stringResource(R.string.dashboard_empty_routines_help),
                        buttonLabel = stringResource(R.string.dashboard_create_routine),
                        onButtonClick = callbacks.onCreateRoutine,
                        visual = DashboardEmptyVisual.Routine,
                    )
                }

                presentation.hasRoutinesWithoutFavorites -> {
                    InfoEmptyStateCard(text = stringResource(R.string.dashboard_empty_favorite_routines))
                }

                else -> {
                    DashboardAdaptiveGrid(
                        itemCount = presentation.favoriteRoutines.size,
                        itemContent = { index ->
                            FavoriteRoutinePlaceholderCard(routine = presentation.favoriteRoutines[index])
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardAdaptiveGrid(
    itemCount: Int,
    modifier: Modifier = Modifier,
    itemContent: @Composable (Int) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth >= 760.dp -> 3
            maxWidth >= 520.dp -> 2
            else -> 1
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            (0 until itemCount).chunked(columns).forEach { rowIndexes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    rowIndexes.forEach { index ->
                        Box(modifier = Modifier.weight(1f)) {
                            itemContent(index)
                        }
                    }
                    repeat(columns - rowIndexes.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
