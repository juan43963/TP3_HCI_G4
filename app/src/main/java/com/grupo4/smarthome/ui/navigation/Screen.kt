package com.grupo4.smarthome.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

sealed class Screen(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector
) {
    object Home : Screen("home", R.string.nav_home, SmartHomeIcons.Home)
    object Rooms : Screen("rooms", R.string.nav_rooms, SmartHomeIcons.Rooms)
    object Routines : Screen("routines", R.string.nav_routines, SmartHomeIcons.Routines)
    object Profile : Screen("profile", R.string.nav_profile, SmartHomeIcons.Profile)

    companion object {
        val items: List<Screen> by lazy { listOf(Home, Routines, Rooms) }
        const val SELECT_HOME_ROUTE = "select_home"
    }
}
