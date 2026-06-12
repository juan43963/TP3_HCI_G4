package com.grupo4.smarthome.ui.screens.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.CompactHomeSwitcherMenu
import com.grupo4.smarthome.ui.components.LocalHomeSwitcher
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun HomeSelector(
    homeName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    useCompactMenu: Boolean = false,
) {
    val switcher = LocalHomeSwitcher.current
    val displayName = switcher
        ?.homes
        ?.firstOrNull { it.id == switcher.activeHomeId }
        ?.name
        ?: homeName

    if (!useCompactMenu) {
        HomeSelectorPill(homeName = displayName, onClick = onClick, modifier = modifier)
        return
    }

    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        HomeSelectorPill(homeName = displayName, onClick = { expanded = true })
        CompactHomeSwitcherMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
        )
    }
}

@Composable
private fun HomeSelectorPill(
    homeName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Home,
                contentDescription = stringResource(R.string.dashboard_home_selector_content_description),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = homeName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Icon(
                imageVector = SmartHomeIcons.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
