package com.grupo4.smarthome.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.MainShellScaffold
import com.grupo4.smarthome.ui.components.TabletMainSidebar
import com.grupo4.smarthome.ui.util.isPhoneLandscape
import com.grupo4.smarthome.ui.components.TabletSidebarDestination
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardTopBar
import com.grupo4.smarthome.ui.screens.dashboard.layout.DashboardScaffold
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun NotificationsScreen(
    windowSize: WindowSizeClass,
    notifications: List<NotificationUi> = sampleNotifications,
    onNotificationClick: (String) -> Unit = {},
    onMarkAllRead: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRoutines: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onHomeSelectorClick: () -> Unit = {},
    onBack: () -> Unit = {},
    selectedHomeName: String = "Casa Principal",
    modifier: Modifier = Modifier,
) {
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val unread = remember(notifications) { notifications.filter { !it.read } }
    val earlier = remember(notifications) { notifications.filter { it.read } }

    if (isTablet) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val portraitLike = maxWidth < 900.dp
            val railCompact = portraitLike || isPhoneLandscape(maxWidth, maxHeight)
            val contentPadding = if (portraitLike) 24.dp else 32.dp

            DashboardScaffold(
                sidebar = {
                    TabletMainSidebar(
                        selectedDestination = TabletSidebarDestination.Notifications,
                        compact = railCompact,
                        onNavigateHome = onNavigateHome,
                        onNavigateRoutines = onNavigateRoutines,
                        onNavigateRooms = onNavigateRooms,
                        onNavigateHistory = onNavigateHistory,
                    )
                },
                topBar = {
                    DashboardTopBar(
                        selectedHomeName = selectedHomeName,
                        onHomeSelectorClick = onHomeSelectorClick,
                        onSearchClick = onSearchClick,
                        onProfileClick = onProfileClick,
                        useCompactHomeMenu = true,
                        modifier = Modifier.padding(
                            start = contentPadding,
                            top = 24.dp,
                            end = contentPadding,
                            bottom = 14.dp,
                        ),
                    )
                },
                content = {
                    NotificationsFeed(
                        unread = unread,
                        earlier = earlier,
                        onNotificationClick = onNotificationClick,
                        onMarkAllRead = onMarkAllRead,
                        contentPadding = PaddingValues(
                            start = contentPadding,
                            end = contentPadding,
                            top = 8.dp,
                            bottom = 28.dp,
                        ),
                        itemModifier = Modifier.widthIn(max = 720.dp),
                    )
                },
            )
        }
    } else {
        MainShellScaffold(
            title = stringResource(R.string.notifications_title),
            onSearchClick = onSearchClick,
            onProfileClick = onProfileClick,
            onBackClick = onBack,
            modifier = modifier,
        ) { innerPadding ->
            NotificationsFeed(
                unread = unread,
                earlier = earlier,
                onNotificationClick = onNotificationClick,
                onMarkAllRead = onMarkAllRead,
                showHeaderTitle = false,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 32.dp,
                ),
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun NotificationsFeed(
    unread: List<NotificationUi>,
    earlier: List<NotificationUi>,
    onNotificationClick: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    contentPadding: PaddingValues,
    showHeaderTitle: Boolean = true,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
) {
    val isEmpty = unread.isEmpty() && earlier.isEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            NotificationsHeader(
                hasUnread = unread.isNotEmpty(),
                onMarkAllRead = onMarkAllRead,
                showTitle = showHeaderTitle,
                modifier = itemModifier,
            )
        }

        if (isEmpty) {
            item { EmptyNotifications(modifier = itemModifier) }
        } else {
            if (unread.isNotEmpty()) {
                item {
                    NotificationGroup(
                        title = stringResource(R.string.notifications_section_new),
                        notifications = unread,
                        onNotificationClick = onNotificationClick,
                        modifier = itemModifier,
                    )
                }
            }
            if (earlier.isNotEmpty()) {
                item {
                    NotificationGroup(
                        title = stringResource(R.string.notifications_section_earlier),
                        notifications = earlier,
                        onNotificationClick = onNotificationClick,
                        modifier = itemModifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader(
    hasUnread: Boolean,
    onMarkAllRead: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showTitle) {
            Text(
                text = stringResource(R.string.notifications_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = stringResource(R.string.notifications_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (hasUnread) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.notifications_mark_all_read),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onMarkAllRead)
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun NotificationGroup(
    title: String,
    notifications: List<NotificationUi>,
    onNotificationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
        ) {
            Column {
                notifications.forEachIndexed { index, notification ->
                    NotificationRow(
                        notification = notification,
                        onClick = { onNotificationClick(notification.id) },
                    )
                    if (index != notifications.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: NotificationUi,
    onClick: () -> Unit,
) {
    val highlight = if (notification.read) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlight)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = notification.category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (notification.read) FontWeight.SemiBold else FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(
                    R.string.notifications_metadata,
                    stringResource(notification.category.labelRes),
                    notification.timeLabel,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!notification.read) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
            )
        }
    }
}

@Composable
private fun EmptyNotifications(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.NotificationsOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = stringResource(R.string.notifications_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.notifications_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NotificationsPreviewContent() {
    NotificationsFeed(
        unread = sampleNotifications.filter { !it.read },
        earlier = sampleNotifications.filter { it.read },
        onNotificationClick = {},
        onMarkAllRead = {},
        contentPadding = PaddingValues(16.dp),
    )
}
