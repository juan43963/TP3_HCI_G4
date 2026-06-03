package com.grupo4.smarthome.ui.screens.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.rooms.layout.TabletRoomsListDetailLayout
import com.grupo4.smarthome.ui.screens.rooms.layout.TabletRoomDetailLayout
import com.grupo4.smarthome.ui.screens.rooms.layout.TabletRoomsLayout

@Composable
fun RoomsContent(
    windowSize: WindowSizeClass?,
    state: RoomsUiState,
    callbacks: RoomsCallbacks,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val presentation = remember(state) { state.toPresentationState() }
        val useListDetail = windowSize?.widthSizeClass == WindowWidthSizeClass.Expanded &&
            maxWidth >= RoomsListDetailMinWidth &&
            maxWidth > maxHeight

        LaunchedEffect(useListDetail, state.selectedRoomId, presentation.listDetailSelectedRoom?.id) {
            if (useListDetail && state.selectedRoomId != presentation.listDetailSelectedRoom?.id) {
                callbacks.onSelectedRoomChange(presentation.listDetailSelectedRoom?.id)
            }
        }

        when {
            windowSize == null || windowSize.widthSizeClass == WindowWidthSizeClass.Compact -> {
                PhoneRoomsPlaceholder(modifier = Modifier.fillMaxSize())
            }
            useListDetail -> {
                TabletRoomsListDetailLayout(
                    state = state,
                    presentation = presentation,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            presentation.selectedRoom != null -> {
                TabletRoomDetailLayout(
                    state = state,
                    presentation = presentation,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                TabletRoomsLayout(
                    state = state,
                    presentation = presentation,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

private val RoomsListDetailMinWidth = 1200.dp

@Composable
fun PhoneRoomsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.nav_rooms),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
