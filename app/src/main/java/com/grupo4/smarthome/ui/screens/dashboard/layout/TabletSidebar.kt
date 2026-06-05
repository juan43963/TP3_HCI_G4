package com.grupo4.smarthome.ui.screens.dashboard.layout

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import com.grupo4.smarthome.ui.components.TabletMainSidebar
import com.grupo4.smarthome.ui.components.TabletSidebarDestination
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks

@Composable
fun TabletSidebar(
    callbacks: DashboardCallbacks,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    TabletMainSidebar(
        selectedDestination = TabletSidebarDestination.Home,
        compact = compact,
        onNavigateHome = callbacks.onNavigateHome,
        onNavigateRoutines = callbacks.onNavigateRoutines,
        onNavigateRooms = callbacks.onNavigateRooms,
        onNavigateHistory = callbacks.onHistoryClick,
        onAddClick = callbacks.onQuickAddClick,
        modifier = modifier,
    )
}
