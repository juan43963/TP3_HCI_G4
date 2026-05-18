package com.grupo4.smarthome.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.grupo4.smarthome.R

sealed class Screen(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector
) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object Rooms : Screen("rooms", R.string.nav_rooms, Icons.Default.Star)
    object Routines : Screen("routines", R.string.nav_routines, Icons.Default.PlayArrow)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)

    companion object {
        val items = listOf(Home, Rooms, Routines, Profile)
    }
}
