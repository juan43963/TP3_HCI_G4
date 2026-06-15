package com.grupo4.smarthome.ui.components

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

private const val BADGE_MAX_COUNT = 9

@Composable
fun NotificationsBellButton(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasUnread = unreadCount > 0
    val description = if (hasUnread) {
        pluralStringResource(
            R.plurals.notifications_badge_count_content_description,
            unreadCount,
            unreadCount,
        )
    } else {
        stringResource(R.string.notifications_open_content_description)
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        BadgedBox(
            badge = {
                if (hasUnread) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ) {
                        Text(
                            text = if (unreadCount > BADGE_MAX_COUNT) {
                                stringResource(R.string.notifications_badge_overflow)
                            } else {
                                unreadCount.toString()
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            },
        ) {
            Icon(
                imageVector = if (hasUnread) SmartHomeIcons.NotificationsActive else SmartHomeIcons.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
