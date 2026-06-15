package com.grupo4.smarthome.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

val HomeActionMenuWidth = 168.dp
private val HomeActionMenuItemHeight = 42.dp

// Menú de acciones por hogar compartido entre el switcher y el drawer.
@Composable
fun HomeActionMenuContent(
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.width(HomeActionMenuWidth),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            HomeActionMenuItem(
                label = stringResource(R.string.select_home_menu_rename),
                icon = Icons.Outlined.Edit,
                onClick = onEdit,
            )
            HomeActionMenuItem(
                label = stringResource(R.string.select_home_menu_share),
                icon = Icons.Outlined.Share,
                onClick = onShare,
            )
            HomeActionMenuItem(
                label = stringResource(R.string.select_home_menu_delete),
                icon = SmartHomeIcons.Delete,
                onClick = onDelete,
                destructive = true,
            )
        }
    }
}

// Versión anclada a un botón para usar como popup desplegable.
@Composable
fun HomeActionMenuPopup(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    if (!expanded) return
    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        HomeActionMenuContent(
            onEdit = onEdit,
            onShare = onShare,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun HomeActionMenuItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val contentColor = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HomeActionMenuItemHeight)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (destructive) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
