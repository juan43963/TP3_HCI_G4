package com.grupo4.smarthome.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardAvatarButton
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun SmartHomeTopBar(
    title: String?,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: (() -> Unit)? = null,
    notificationCount: Int = 0,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = SmartHomeIcons.Menu,
                contentDescription = stringResource(R.string.dashboard_menu_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = SmartHomeIcons.Search,
                contentDescription = stringResource(R.string.dashboard_search_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (onNotificationsClick != null) {
            NotificationsBellButton(
                unreadCount = notificationCount,
                onClick = onNotificationsClick,
            )
        }
        DashboardAvatarButton(onClick = onProfileClick)
    }
}
