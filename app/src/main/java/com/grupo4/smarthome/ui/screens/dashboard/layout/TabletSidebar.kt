package com.grupo4.smarthome.ui.screens.dashboard.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.components.QuickAddButton
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

enum class SidebarDestination { Home, Routines, Rooms }

@Composable
fun TabletSidebar(
    callbacks: DashboardCallbacks,
    compact: Boolean,
    modifier: Modifier = Modifier,
    selected: SidebarDestination = SidebarDestination.Home,
    quickAddMenuEnabled: Boolean = true,
    quickAddMenuExpanded: Boolean = false,
    onQuickAddMenuExpandedChange: (Boolean) -> Unit = {},
) {
    val width = if (compact) 176.dp else 236.dp
    val horizontalPadding = if (compact) 14.dp else 22.dp

    Surface(
        modifier = modifier
            .width(width)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = horizontalPadding, vertical = 28.dp),
        ) {
            DashboardLogo(compact = compact)

            Spacer(modifier = Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SidebarNavItem(
                    label = stringResource(R.string.nav_home),
                    icon = SmartHomeIcons.Home,
                    selected = selected == SidebarDestination.Home,
                    compact = compact,
                    onClick = callbacks.onNavigateHome,
                )
                SidebarNavItem(
                    label = stringResource(R.string.nav_routines),
                    icon = SmartHomeIcons.Routines,
                    selected = selected == SidebarDestination.Routines,
                    compact = compact,
                    onClick = callbacks.onNavigateRoutines,
                )
                SidebarNavItem(
                    label = stringResource(R.string.nav_rooms),
                    icon = SmartHomeIcons.Rooms,
                    selected = selected == SidebarDestination.Rooms,
                    compact = compact,
                    onClick = callbacks.onNavigateRooms,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (quickAddMenuEnabled) {
                QuickAddButton(
                    onClick = { onQuickAddMenuExpandedChange(!quickAddMenuExpanded) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                QuickAddButton(
                    onClick = callbacks.onQuickAddClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun BoxScope.TabletQuickActionsOverlay(
    visible: Boolean,
    compact: Boolean,
    callbacks: DashboardCallbacks,
    onDismiss: () -> Unit,
) {
    val sidebarWidth = if (compact) 176.dp else 236.dp
    val horizontalPadding = if (compact) 14.dp else 22.dp
    val buttonWidth = sidebarWidth - horizontalPadding * 2
    val bottomPadding = 28.dp
    val actionSpacing = 10.dp
    val closeButtonHeight = 52.dp

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(160)),
        modifier = Modifier.matchParentSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismiss() },
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) +
            expandVertically(animationSpec = tween(220), expandFrom = Alignment.Bottom),
        exit = fadeOut(tween(140)) +
            shrinkVertically(animationSpec = tween(180), shrinkTowards = Alignment.Bottom),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(
                start = horizontalPadding,
                bottom = bottomPadding + closeButtonHeight + actionSpacing,
            )
            .width(buttonWidth),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(actionSpacing)) {
            TabletQuickAddItem(
                label = stringResource(R.string.quick_add_room),
                icon = SmartHomeIcons.Rooms,
                onClick = {
                    onDismiss()
                    callbacks.onCreateRoom()
                },
            )
            TabletQuickAddItem(
                label = stringResource(R.string.quick_add_device),
                icon = SmartHomeIcons.Devices,
                onClick = {
                    onDismiss()
                    callbacks.onCreateDevice()
                },
            )
            TabletQuickAddItem(
                label = stringResource(R.string.quick_add_routine),
                icon = SmartHomeIcons.Routines,
                onClick = {
                    onDismiss()
                    callbacks.onCreateRoutine()
                },
            )
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(140)),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = horizontalPadding, bottom = bottomPadding)
            .width(buttonWidth),
    ) {
        TabletQuickAddCloseButton(onClick = onDismiss)
    }
}

@Composable
private fun TabletQuickAddItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrandIndigoDark),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(21.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TabletQuickAddCloseButton(
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = BrandIndigoDark,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = SmartHomeIcons.Close,
                contentDescription = stringResource(R.string.quick_add_fab_close),
                tint = Color.White,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.quick_add_close_short),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DashboardLogo(compact: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.welcome_brand_smart),
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.welcome_brand_home),
            style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SidebarNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    height: Dp = 48.dp,
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(background, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = if (compact) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
        )
    }
}
