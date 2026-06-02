package com.grupo4.smarthome.ui.screens.dashboard.layout.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardUiState
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun PhoneHomeDrawer(
    state: DashboardUiState,
    callbacks: DashboardCallbacks,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .width(304.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        PhoneHomeDrawerContent(
            state = state,
            callbacks = callbacks,
            onClose = onClose,
        )
    }
}

@Composable
fun PhoneHomeDrawerContent(
    state: DashboardUiState,
    callbacks: DashboardCallbacks,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val homes = listOf(
        DrawerHomeItem(
            name = stringResource(R.string.dashboard_home_main),
            icon = SmartHomeIcons.Home,
        ),
        DrawerHomeItem(
            name = stringResource(R.string.dashboard_home_apartment),
            icon = SmartHomeIcons.Apartment,
        ),
        DrawerHomeItem(
            name = stringResource(R.string.dashboard_home_office),
            icon = SmartHomeIcons.Office,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        DrawerUserHeader(userName = state.userName)

        Spacer(modifier = Modifier.height(30.dp))

        DrawerSectionLabel(text = stringResource(R.string.dashboard_my_homes))
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            homes.forEach { home ->
                DrawerActionRow(
                    label = home.name,
                    icon = home.icon,
                    selected = home.name == state.selectedHomeName,
                    onClick = {
                        callbacks.onHomeSelected(home.name)
                        onClose()
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        DrawerSectionLabel(text = stringResource(R.string.dashboard_quick_access))
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DrawerActionRow(
                label = stringResource(R.string.dashboard_add_home),
                icon = SmartHomeIcons.Add,
                selected = false,
                onClick = {
                    callbacks.onAddHomeClick()
                    onClose()
                },
            )
            DrawerActionRow(
                label = stringResource(R.string.dashboard_settings),
                icon = SmartHomeIcons.Settings,
                selected = false,
                onClick = {
                    callbacks.onSettingsClick()
                    onClose()
                },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        DrawerActionRow(
            label = stringResource(R.string.dashboard_logout),
            icon = SmartHomeIcons.Logout,
            selected = false,
            onClick = {
                callbacks.onLogoutClick()
                onClose()
            },
            contentColor = ErrorRed,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun DrawerUserHeader(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = SmartHomeIcons.Profile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = userName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DrawerActionRow(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: androidx.compose.ui.graphics.Color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    },
    backgroundColor: androidx.compose.ui.graphics.Color = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) {
            Icon(
                imageVector = SmartHomeIcons.Selected,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private data class DrawerHomeItem(
    val name: String,
    val icon: ImageVector,
)
