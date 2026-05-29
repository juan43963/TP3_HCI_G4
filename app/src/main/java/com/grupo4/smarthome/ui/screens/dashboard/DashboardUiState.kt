package com.grupo4.smarthome.ui.screens.dashboard

data class DashboardUiState(
    val userName: String,
    val selectedHomeName: String,
    val activeDevicesCount: Int,
    val roomsCount: Int,
    val rooms: List<DashboardRoomPreview>,
    val routines: List<DashboardRoutinePreview>,
    val isLoadingRooms: Boolean = false,
    val isLoadingRoutines: Boolean = false,
)

data class DashboardRoomPreview(
    val id: String,
    val name: String,
    val isFavorite: Boolean,
)

data class DashboardRoutinePreview(
    val id: String,
    val name: String,
    val isFavorite: Boolean,
)

data class DashboardPresentationState(
    val favoriteRooms: List<DashboardRoomPreview>,
    val favoriteRoutines: List<DashboardRoutinePreview>,
    val hasNoRooms: Boolean,
    val hasRoomsWithoutFavorites: Boolean,
    val hasNoRoutines: Boolean,
    val hasRoutinesWithoutFavorites: Boolean,
    val hasFavoriteContent: Boolean,
)

fun DashboardUiState.toPresentationState(): DashboardPresentationState {
    val favoriteRooms = rooms.filter { it.isFavorite }
    val favoriteRoutines = routines.filter { it.isFavorite }

    return DashboardPresentationState(
        favoriteRooms = favoriteRooms,
        favoriteRoutines = favoriteRoutines,
        hasNoRooms = rooms.isEmpty(),
        hasRoomsWithoutFavorites = rooms.isNotEmpty() && favoriteRooms.isEmpty(),
        hasNoRoutines = routines.isEmpty(),
        hasRoutinesWithoutFavorites = routines.isNotEmpty() && favoriteRoutines.isEmpty(),
        hasFavoriteContent = favoriteRooms.isNotEmpty() || favoriteRoutines.isNotEmpty(),
    )
}
