package com.grupo4.smarthome.ui.screens.dashboard

object DashboardPreviewData {
    val emptyHome = DashboardUiState(
        userName = "Usuario",
        selectedHomeName = "Casa Principal",
        activeDevicesCount = 0,
        roomsCount = 0,
        rooms = emptyList(),
        routines = emptyList(),
    )

    val roomsWithoutFavoritesAndNoRoutines = DashboardUiState(
        userName = "Usuario",
        selectedHomeName = "Casa Principal",
        activeDevicesCount = 0,
        roomsCount = 3,
        rooms = listOf(
            DashboardRoomPreview(id = "living", name = "Sala de estar", isFavorite = false),
            DashboardRoomPreview(id = "bedroom", name = "Habitación principal", isFavorite = false),
            DashboardRoomPreview(id = "kitchen", name = "Cocina", isFavorite = false),
        ),
        routines = emptyList(),
    )

    val roomsWithFavoritesNoRoutineFavorites = DashboardUiState(
        userName = "Usuario",
        selectedHomeName = "Casa Principal",
        activeDevicesCount = 4,
        roomsCount = 3,
        rooms = listOf(
            DashboardRoomPreview(id = "living", name = "Sala de estar", isFavorite = true),
            DashboardRoomPreview(id = "bedroom", name = "Habitación principal", isFavorite = true),
            DashboardRoomPreview(id = "kitchen", name = "Cocina", isFavorite = false),
        ),
        routines = listOf(
            DashboardRoutinePreview(id = "morning", name = "Buenos días", isFavorite = false),
            DashboardRoutinePreview(id = "night", name = "Noche", isFavorite = false),
        ),
    )

    val populated = DashboardUiState(
        userName = "Usuario",
        selectedHomeName = "Casa Principal",
        activeDevicesCount = 9,
        roomsCount = 3,
        rooms = listOf(
            DashboardRoomPreview(id = "living", name = "Sala de estar", isFavorite = true),
            DashboardRoomPreview(id = "bedroom", name = "Habitación principal", isFavorite = true),
            DashboardRoomPreview(id = "kitchen", name = "Cocina", isFavorite = true),
        ),
        routines = listOf(
            DashboardRoutinePreview(id = "morning", name = "Buenos días", isFavorite = true),
            DashboardRoutinePreview(id = "night", name = "Noche", isFavorite = true),
            DashboardRoutinePreview(id = "movie", name = "Cine en casa", isFavorite = false),
        ),
    )
}
