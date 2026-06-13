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
            title = if (presentation.showingRecentRooms) {
                stringResource(R.string.dashboard_recent_rooms_title)
            } else {
                stringResource(R.string.dashboard_favorite_rooms_title)
            },
            subtitle = if (presentation.showingRecentRooms) {
                stringResource(R.string.dashboard_recent_rooms_subtitle)
            } else {
                stringResource(R.string.dashboard_favorite_rooms_subtitle)
            },
        ) {
            if (presentation.hasRoomsWithoutFavorites) {
                InfoEmptyStateCard(
                    messagePrefix = stringResource(R.string.dashboard_empty_favorite_rooms_prefix),
                    highlightedTarget = stringResource(R.string.dashboard_empty_favorite_rooms_target),
                    messageSuffix = stringResource(R.string.dashboard_empty_favorite_rooms_suffix),
                    actionLabel = stringResource(R.string.dashboard_create_room),
                    tabletActionLabel = stringResource(R.string.dashboard_go_to_rooms),
                    onActionClick = callbacks.onCreateRoom,
                    onTabletActionClick = callbacks.onNavigateRooms,
                )
            } else {
                DashboardAdaptiveGrid(
                    itemCount = presentation.displayRooms.size,
                    itemContent = { index ->
                        FavoriteRoomPlaceholderCard(
                            room = presentation.displayRooms[index],
                            onClick = { callbacks.onRoomClick(presentation.displayRooms[index].id) },
                            onToggleFavorite = { callbacks.onToggleRoomFavorite(presentation.displayRooms[index].id) },
                        )
                    },
                )
            }
        }

        DashboardSection(
            title = if (presentation.showingRecentRoutines) {
                stringResource(R.string.dashboard_recent_routines_title)
            } else {
                stringResource(R.string.dashboard_favorite_routines_title)
            },
            subtitle = if (presentation.showingRecentRoutines) {
                stringResource(R.string.dashboard_recent_routines_subtitle)
            } else {
                stringResource(R.string.dashboard_favorite_routines_subtitle)
            },
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
                    InfoEmptyStateCard(
                        messagePrefix = stringResource(R.string.dashboard_empty_favorite_routines_prefix),
                        highlightedTarget = stringResource(R.string.dashboard_empty_favorite_routines_target),
                        messageSuffix = stringResource(R.string.dashboard_empty_favorite_routines_suffix),
                        actionLabel = stringResource(R.string.dashboard_create_routine),
                        tabletActionLabel = stringResource(R.string.dashboard_go_to_routines),
                        onActionClick = callbacks.onCreateRoutine,
                        onTabletActionClick = callbacks.onNavigateRoutines,
                    )
                }

                else -> {
                    DashboardAdaptiveGrid(
                        itemCount = presentation.displayRoutines.size,
                        itemContent = { index ->
                            FavoriteRoutinePlaceholderCard(
                                routine = presentation.displayRoutines[index],
                                onRun = { callbacks.onRunRoutine(presentation.displayRoutines[index].id) },
                                onToggleFavorite = {
                                    callbacks.onToggleRoutineFavorite(presentation.displayRoutines[index].id)
                                },
                            )
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
