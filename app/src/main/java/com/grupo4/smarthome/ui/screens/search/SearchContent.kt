package com.grupo4.smarthome.ui.screens.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.ui.screens.search.layout.TabletSearchLayout
import com.grupo4.smarthome.ui.screens.search.layout.phone.PhoneSearchLayout

@Composable
fun SearchContent(
    windowSize: WindowSizeClass,
    state: SearchUiState,
    rooms: List<Room>,
    routines: List<Routine>,
    callbacks: SearchCallbacks,
    modifier: Modifier = Modifier,
) {
    val presentation = remember(state, rooms, routines) {
        state.toPresentationState(rooms, routines)
    }

    if (windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        TabletSearchLayout(
            presentation = presentation,
            callbacks = callbacks,
            modifier = modifier,
        )
    } else {
        PhoneSearchLayout(
            presentation = presentation,
            callbacks = callbacks,
            modifier = modifier,
        )
    }
}
