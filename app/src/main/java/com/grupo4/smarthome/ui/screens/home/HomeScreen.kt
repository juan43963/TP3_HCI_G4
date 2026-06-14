package com.grupo4.smarthome.ui.screens.home

import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardScreen
import com.grupo4.smarthome.ui.screens.dashboard.DashboardUiState

@Composable
fun HomeScreen(
    windowSize: WindowSizeClass,
    state: DashboardUiState,
    modifier: Modifier = Modifier,
    onNavigateRooms: () -> Unit = {},
    onNavigateRoutines: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onRoomClick: (String) -> Unit = {},
    onToggleRoomFavorite: (String) -> Unit = {},
    onToggleRoutineFavorite: (String) -> Unit = {},
    onRunRoutine: (String) -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onQuickActionsOpen: () -> Unit = {},
    onCreateRoom: () -> Unit = {},
    onCreateDevice: () -> Unit = {},
    onCreateRoutine: () -> Unit = {},
    showQuickAddFab: Boolean = true,
) {
    DashboardScreen(
        windowSize = windowSize,
        modifier = modifier,
        state = state.copy(
            unreadNotificationCount = unreadNotificationCount,
        ),
        callbacks = DashboardCallbacks(
            onNavigateRooms = onNavigateRooms,
            onNavigateRoutines = onNavigateRoutines,
            onSearchClick = onSearchClick,
            onRoomClick = onRoomClick,
            onToggleRoomFavorite = onToggleRoomFavorite,
            onToggleRoutineFavorite = onToggleRoutineFavorite,
            onRunRoutine = onRunRoutine,
            onProfileClick = onNavigateProfile,
            onHistoryClick = onNavigateHistory,
            onNotificationsClick = onNavigateNotifications,
            onCreateRoom = onCreateRoom,
            onCreateDevice = onCreateDevice,
            onCreateRoutine = onCreateRoutine,
            onQuickAddClick = onQuickActionsOpen,
            showQuickAddFab = showQuickAddFab,
        ),
    )
}
