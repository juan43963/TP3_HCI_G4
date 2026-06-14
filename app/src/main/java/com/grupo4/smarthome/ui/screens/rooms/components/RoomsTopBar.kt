package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.ui.components.NotificationsBellButton
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardAvatarButton
import com.grupo4.smarthome.ui.screens.dashboard.components.HomeSelector
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun RoomsTopBar(
    onSearchClick: () -> Unit,
    onAvatarClick: () -> Unit,
    searchPlaceholder: String,
    modifier: Modifier = Modifier,
    onNotificationsClick: (() -> Unit)? = null,
    notificationCount: Int = 0,
    showHomeSelector: Boolean = false,
    onHomeSelectorClick: () -> Unit = {},
    useCompactHomeMenu: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showHomeSelector) {
            HomeSelector(
                homeName = "",
                onClick = onHomeSelectorClick,
                useCompactMenu = useCompactHomeMenu,
            )
        }
        RoomsSearchField(
            onClick = onSearchClick,
            placeholder = searchPlaceholder,
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 620.dp),
        )
        if (onNotificationsClick != null) {
            NotificationsBellButton(
                unreadCount = notificationCount,
                onClick = onNotificationsClick,
            )
        }
        DashboardAvatarButton(onClick = onAvatarClick)
    }
}

@Composable
private fun RoomsSearchField(
    onClick: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = placeholder
            },
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
