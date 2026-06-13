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
    val unreadNotificationCount: Int = 0,
    val recentRoomAccess: Map<String, Long> = emptyMap(),
    val recentRoutineAccess: Map<String, Long> = emptyMap(),
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
    val displayRooms: List<DashboardRoomPreview>,
    val displayRoutines: List<DashboardRoutinePreview>,
    val showingRecentRooms: Boolean,
    val showingRecentRoutines: Boolean,
    val hasNoRooms: Boolean,
    val hasRoomsWithoutFavorites: Boolean,
    val hasNoRoutines: Boolean,
    val hasRoutinesWithoutFavorites: Boolean,
    val hasFavoriteContent: Boolean,
)

private const val RECENT_WINDOW_MILLIS = 7L * 24 * 60 * 60 * 1000
private const val MAX_RECENT = 3

fun DashboardUiState.toPresentationState(): DashboardPresentationState {
    val favoriteRooms = rooms.filter { it.isFavorite }
    val favoriteRoutines = routines.filter { it.isFavorite }

    val recentRooms = rooms.recentlyUsed(recentRoomAccess) { it.id }
    val recentRoutines = routines.recentlyUsed(recentRoutineAccess) { it.id }

    val displayRooms = if (favoriteRooms.isNotEmpty()) favoriteRooms else recentRooms
    val displayRoutines = if (favoriteRoutines.isNotEmpty()) favoriteRoutines else recentRoutines

    return DashboardPresentationState(
        favoriteRooms = favoriteRooms,
        favoriteRoutines = favoriteRoutines,
        displayRooms = displayRooms,
        displayRoutines = displayRoutines,
        showingRecentRooms = favoriteRooms.isEmpty() && recentRooms.isNotEmpty(),
        showingRecentRoutines = favoriteRoutines.isEmpty() && recentRoutines.isNotEmpty(),
        hasNoRooms = rooms.isEmpty(),
        hasRoomsWithoutFavorites = rooms.isNotEmpty() && displayRooms.isEmpty(),
        hasNoRoutines = routines.isEmpty(),
        hasRoutinesWithoutFavorites = routines.isNotEmpty() && displayRoutines.isEmpty(),
        hasFavoriteContent = favoriteRooms.isNotEmpty() || favoriteRoutines.isNotEmpty(),
    )
}

private fun <T> List<T>.recentlyUsed(access: Map<String, Long>, id: (T) -> String): List<T> {
    val now = System.currentTimeMillis()
    return mapNotNull { item -> access[id(item)]?.let { item to it } }
        .filter { now - it.second <= RECENT_WINDOW_MILLIS }
        .sortedByDescending { it.second }
        .take(MAX_RECENT)
        .map { it.first }
}
