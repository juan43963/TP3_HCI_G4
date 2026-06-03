package com.grupo4.smarthome.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

enum class TabletSidebarDestination {
    Home,
    Routines,
    Rooms,
}

@Composable
fun TabletMainSidebar(
    selectedDestination: TabletSidebarDestination,
    compact: Boolean,
    onNavigateHome: () -> Unit,
    onNavigateRoutines: () -> Unit,
    onNavigateRooms: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val width = if (compact) 76.dp else 236.dp
    val horizontalPadding = if (compact) 12.dp else 22.dp

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
            SmartHomeSidebarLogo(compact = compact)

            Spacer(modifier = Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TabletSidebarNavItem(
                    label = stringResource(R.string.nav_home),
                    icon = SmartHomeIcons.Home,
                    selected = selectedDestination == TabletSidebarDestination.Home,
                    compact = compact,
                    onClick = onNavigateHome,
                )
                TabletSidebarNavItem(
                    label = stringResource(R.string.nav_routines),
                    icon = SmartHomeIcons.Routines,
                    selected = selectedDestination == TabletSidebarDestination.Routines,
                    compact = compact,
                    onClick = onNavigateRoutines,
                )
                TabletSidebarNavItem(
                    label = stringResource(R.string.nav_rooms),
                    icon = SmartHomeIcons.Rooms,
                    selected = selectedDestination == TabletSidebarDestination.Rooms,
                    compact = compact,
                    onClick = onNavigateRooms,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TabletSidebarAddButton(
                onClick = onAddClick,
                compact = compact,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SmartHomeSidebarLogo(compact: Boolean) {
    if (compact) {
        SmartHomeCompactBrand()
        return
    }

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
private fun SmartHomeCompactBrand() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "S",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "H",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun TabletSidebarNavItem(
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
            .padding(horizontal = if (compact) 0.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (compact) Arrangement.Center else Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (compact) label else null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        if (!compact) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TabletSidebarAddButton(
    onClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    if (compact) {
        Surface(
            modifier = modifier
                .height(52.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(999.dp),
            color = BrandIndigoDark,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = SmartHomeIcons.Add,
                    contentDescription = stringResource(R.string.dashboard_add),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        return
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(999.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandIndigoDark,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Icon(imageVector = SmartHomeIcons.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.dashboard_add),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
