package com.grupo4.smarthome.ui.screens.dashboard.layout.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.ui.components.HomeActionMenuPopup
import com.grupo4.smarthome.ui.components.SmartHomeWordmark
import com.grupo4.smarthome.ui.components.SmartHomeWordmarkSize
import com.grupo4.smarthome.ui.screens.selecthome.SelectableHome
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

private val DrawerChromeReserve = 400.dp
private val ExpandedHomesVisibleHeight = 160.dp
@Composable
fun PhoneHomeDrawer(
    homes: List<SelectableHome>,
    activeHomeId: String?,
    onHomeSelected: (String) -> Unit,
    onEditHome: (SelectableHome) -> Unit,
    onShareHome: (SelectableHome) -> Unit,
    onAddHome: () -> Unit,
    onHistory: () -> Unit,
    onLogout: () -> Unit,
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
            homes = homes,
            activeHomeId = activeHomeId,
            onHomeSelected = onHomeSelected,
            onEditHome = onEditHome,
            onShareHome = onShareHome,
            onAddHome = onAddHome,
            onHistory = onHistory,
            onLogout = onLogout,
            onClose = onClose,
        )
    }
}

@Composable
fun PhoneHomeDrawerContent(
    homes: List<SelectableHome>,
    activeHomeId: String?,
    onHomeSelected: (String) -> Unit,
    onEditHome: (SelectableHome) -> Unit,
    onShareHome: (SelectableHome) -> Unit,
    onAddHome: () -> Unit,
    onHistory: () -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val expandedMaxHeight = (maxHeight - DrawerChromeReserve).coerceIn(140.dp, 360.dp)

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            SmartHomeWordmark(
                modifier = Modifier.fillMaxWidth(),
                size = SmartHomeWordmarkSize.Large,
            )

            Spacer(modifier = Modifier.height(30.dp))

            DrawerSectionLabel(text = stringResource(R.string.dashboard_my_homes))
            Spacer(modifier = Modifier.height(10.dp))

            val homesListMaxHeight = ExpandedHomesVisibleHeight.coerceAtMost(expandedMaxHeight)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = homesListMaxHeight),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = homes, key = { it.id }) { home ->
                    DrawerHomeRow(
                        home = home,
                        selected = home.id == activeHomeId,
                        onSelect = {
                            onHomeSelected(home.id)
                            onClose()
                        },
                        onEdit = { onEditHome(home) },
                        onShare = { onShareHome(home) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DrawerActionRow(
                label = stringResource(R.string.dashboard_add_home),
                icon = SmartHomeIcons.Add,
                selected = false,
                onClick = {
                    onAddHome()
                    onClose()
                },
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            DrawerActionRow(
                label = stringResource(R.string.nav_history),
                icon = SmartHomeIcons.History,
                selected = false,
                onClick = {
                    onHistory()
                    onClose()
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            DrawerActionRow(
                label = stringResource(R.string.dashboard_logout),
                icon = SmartHomeIcons.Logout,
                selected = false,
                onClick = {
                    onLogout()
                    onClose()
                },
                contentColor = ErrorRed,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                centerContent = true,
            )
        }
    }
}

private fun HomeType.toDrawerIcon(): ImageVector =
    when (this) {
        HomeType.CASA -> SmartHomeIcons.Home
        HomeType.DEPARTAMENTO -> SmartHomeIcons.Apartment
        HomeType.OFICINA -> SmartHomeIcons.Office
        HomeType.OTRO -> SmartHomeIcons.Home
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
private fun DrawerHomeRow(
    home: SelectableHome,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor, RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onSelect)
                .padding(start = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = home.type.toDrawerIcon(),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = home.name,
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

        var menuExpanded by remember { mutableStateOf(false) }
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(
                        R.string.select_home_card_menu_content_description
                    ),
                    tint = if (selected) {
                        contentColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
            HomeActionMenuPopup(
                expanded = menuExpanded,
                onDismiss = { menuExpanded = false },
                onEdit = {
                    menuExpanded = false
                    onEdit()
                },
                onShare = {
                    menuExpanded = false
                    onShare()
                },
            )
        }
    }
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
    centerContent: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centerContent) {
            Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        } else {
            Arrangement.spacedBy(12.dp)
        },
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
            modifier = if (centerContent) Modifier else Modifier.weight(1f),
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
