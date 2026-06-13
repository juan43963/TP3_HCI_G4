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
    object History : Screen("history", R.string.nav_history, SmartHomeIcons.History)
    object Notifications : Screen("notifications", R.string.nav_notifications, SmartHomeIcons.Notifications)
    object NotificationSettings : Screen("notification_settings", R.string.notification_settings_title, SmartHomeIcons.Notifications)

    companion object {
        val items: List<Screen> by lazy { listOf(Home, Routines, Rooms) }
        private val mainRoutes: Set<String> by lazy { items.map { it.route }.toSet() }

        fun isMainDestination(route: String?): Boolean = route != null && route in mainRoutes

        const val SELECT_HOME_ROUTE = "select_home"
        const val CREATE_FIRST_HOME_ROUTE = "create_first_home"
        const val ROUTINE_DETAIL_ROUTE = "routine_detail"
        const val CHANGE_PASSWORD_ROUTE = "change_password_auth"
        const val SCAN_QR_ROUTE = "scan_qr"
        const val DEVICE_DETAIL_ROUTE = "device_detail"
    }
}
