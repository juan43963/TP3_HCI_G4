package com.grupo4.smarthome.ui.screens.dashboard

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DashboardScreen(
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    state: DashboardUiState,
    callbacks: DashboardCallbacks = DashboardCallbacks(),
) {
    DashboardContent(
        windowSize = windowSize,
        state = state,
        callbacks = callbacks,
        modifier = modifier,
    )
}
