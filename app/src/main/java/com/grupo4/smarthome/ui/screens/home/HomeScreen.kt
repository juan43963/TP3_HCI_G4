package com.grupo4.smarthome.ui.screens.home

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardPreviewData
import com.grupo4.smarthome.ui.screens.dashboard.DashboardScreen

@Composable
fun HomeScreen(
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    onNavigateRooms: () -> Unit = {},
    onNavigateRoutines: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onQuickActionsOpen: () -> Unit = {},
    onCreateRoutine: () -> Unit = {},
    showQuickAddFab: Boolean = true,
) {
    DashboardScreen(
        windowSize = windowSize,
        modifier = modifier,
        state = DashboardPreviewData.populated.copy(
            unreadNotificationCount = unreadNotificationCount,
        ),
        callbacks = DashboardCallbacks(
            onNavigateRooms = onNavigateRooms,
            onNavigateRoutines = onNavigateRoutines,
            onProfileClick = onNavigateProfile,
            onHistoryClick = onNavigateHistory,
            onNotificationsClick = onNavigateNotifications,
            onCreateRoutine = onCreateRoutine,
            onQuickAddClick = onQuickActionsOpen,
            showQuickAddFab = showQuickAddFab,
        ),
    )
}
