package com.grupo4.smarthome.ui.screens.dashboard

data class DashboardCallbacks(
    val onHomeSelectorClick: () -> Unit = {},
    val onSearchClick: () -> Unit = {},
    val onProfileClick: () -> Unit = {},
    val onNavigateHome: () -> Unit = {},
    val onNavigateRooms: () -> Unit = {},
    val onNavigateRoutines: () -> Unit = {},
    val onCreateRoom: () -> Unit = {},
    val onCreateRoutine: () -> Unit = {},
    val onQuickAddClick: () -> Unit = {},
    val onHomeSelected: (String) -> Unit = {},
    val onAddHomeClick: () -> Unit = {},
    val onSettingsClick: () -> Unit = {},
    val onLogoutClick: () -> Unit = {},
)
