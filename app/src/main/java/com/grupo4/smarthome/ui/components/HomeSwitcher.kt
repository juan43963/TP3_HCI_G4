package com.grupo4.smarthome.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.ui.screens.selecthome.SelectableHome
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import kotlin.math.roundToInt

data class HomeSwitcherState(
    val homes: List<SelectableHome>,
    val activeHomeId: String?,
    val onHomeSelected: (String) -> Unit,
    val onEditHome: (SelectableHome) -> Unit,
    val onShareHome: (SelectableHome) -> Unit,
    val onAddHome: () -> Unit,
)

val LocalHomeSwitcher = compositionLocalOf<HomeSwitcherState?> { null }

private val SwitcherWidth = 240.dp
private val SwitcherCorner = 20.dp
private val SwitcherListMaxHeight = 240.dp
private val SwitcherRowHeight = 44.dp
private val SwitcherRowCorner = 12.dp
private val ActionMenuItemHeight = 42.dp

private class ActiveHomeMenu(val home: SelectableHome, val anchor: Rect)

@Composable
fun CompactHomeSwitcherMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val switcher = LocalHomeSwitcher.current ?: return
    if (!expanded) return

    val yOffset = with(LocalDensity.current) { 54.dp.roundToPx() }

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, yOffset),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            modifier = modifier.width(SwitcherWidth),
            shape = RoundedCornerShape(SwitcherCorner),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 8.dp,
        ) {
            var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
            var activeMenu by remember { mutableStateOf<ActiveHomeMenu?>(null) }

            Box(modifier = Modifier.onGloballyPositioned { rootCoords = it }) {
                Column(modifier = Modifier.padding(8.dp)) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = SwitcherListMaxHeight),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(items = switcher.homes, key = { it.id }) { home ->
                            CompactHomeRow(
                                home = home,
                                selected = home.id == switcher.activeHomeId,
                                onSelect = {
                                    onDismiss()
                                    switcher.onHomeSelected(home.id)
                                },
                                onOpenMenu = { buttonCoords ->
                                    val rect = rootCoords?.localBoundingBoxOf(
                                        buttonCoords,
                                        clipBounds = false,
                                    )
                                    if (rect != null) {
                                        activeMenu = ActiveHomeMenu(home, rect)
                                    }
                                },
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )

                    AddHomeRow(
                        onClick = {
                            onDismiss()
                            switcher.onAddHome()
                        },
                    )
                }

                val menu = activeMenu
                if (menu != null) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { activeMenu = null },
                    )
                    HomeRowActionMenu(
                        anchor = menu.anchor,
                        rootHeightPx = rootCoords?.size?.height ?: 0,
                        onEdit = {
                            activeMenu = null
                            onDismiss()
                            switcher.onEditHome(menu.home)
                        },
                        onShare = {
                            activeMenu = null
                            onDismiss()
                            switcher.onShareHome(menu.home)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactHomeRow(
    home: SelectableHome,
    selected: Boolean,
    onSelect: () -> Unit,
    onOpenMenu: (LayoutCoordinates) -> Unit,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val rowBackground = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SwitcherRowHeight)
            .clip(RoundedCornerShape(SwitcherRowCorner))
            .background(rowBackground)
            .padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onSelect)
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = home.type.toSwitcherIcon(),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = home.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
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

        var buttonCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
        Box(modifier = Modifier.onGloballyPositioned { buttonCoords = it }) {
            IconButton(
                onClick = { buttonCoords?.let(onOpenMenu) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(
                        R.string.select_home_card_menu_content_description,
                    ),
                    tint = if (selected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.HomeRowActionMenu(
    anchor: Rect,
    rootHeightPx: Int,
    onEdit: () -> Unit,
    onShare: () -> Unit,
) {
    val density = LocalDensity.current
    val menuWidthPx = with(density) { HomeActionMenuWidth.toPx() }
    val gapPx = with(density) { 4.dp.toPx() }
    val estMenuHeightPx = with(density) { (ActionMenuItemHeight * 2 + 12.dp).toPx() }

    val x = (anchor.right - menuWidthPx).coerceAtLeast(0f)
    val openDown = rootHeightPx <= 0 || anchor.bottom + gapPx + estMenuHeightPx <= rootHeightPx
    val y = if (openDown) {
        anchor.bottom + gapPx
    } else {
        (anchor.top - gapPx - estMenuHeightPx).coerceAtLeast(0f)
    }

    HomeActionMenuContent(
        onEdit = onEdit,
        onShare = onShare,
        modifier = Modifier
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .zIndex(1f),
    )
}

@Composable
private fun AddHomeRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SwitcherRowHeight)
            .clip(RoundedCornerShape(SwitcherRowCorner))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = SmartHomeIcons.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.dashboard_add_home),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun HomeType.toSwitcherIcon(): ImageVector =
    when (this) {
        HomeType.CASA -> SmartHomeIcons.Home
        HomeType.DEPARTAMENTO -> SmartHomeIcons.Apartment
        HomeType.OFICINA -> SmartHomeIcons.Office
        HomeType.OTRO -> SmartHomeIcons.Home
    }
