package com.grupo4.smarthome.ui.screens.dashboard.layout.phone

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun PhoneBottomNavigation(
    callbacks: DashboardCallbacks,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        NavigationBarItem(
            selected = true,
            onClick = callbacks.onNavigateHome,
            icon = { Icon(SmartHomeIcons.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
        )
        NavigationBarItem(
            selected = false,
            onClick = callbacks.onNavigateRoutines,
            icon = { Icon(SmartHomeIcons.Routines, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_routines)) },
        )
        NavigationBarItem(
            selected = false,
            onClick = callbacks.onNavigateRooms,
            icon = { Icon(SmartHomeIcons.Rooms, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_rooms)) },
        )
    }
}
