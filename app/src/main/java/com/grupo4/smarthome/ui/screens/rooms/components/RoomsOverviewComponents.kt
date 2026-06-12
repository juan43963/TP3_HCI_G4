package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.components.SectionEditIconButton
import com.grupo4.smarthome.ui.theme.BorderDefault
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.StarActive
import kotlin.math.min

@Composable
fun RoomsHeader(
    totalActiveDevices: Int,
    activeRoomsCount: Int,
    editMode: Boolean,
    onCreateRoomClick: () -> Unit,
    onEditRoomsToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showEditButton: Boolean = true,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compactActions = maxWidth < 760.dp

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RoomsHeaderTitle(
                    compact = compactActions,
                    modifier = Modifier.weight(1f),
                )
                RoomsHeaderActions(
                    editMode = editMode,
                    compact = compactActions,
                    onCreateRoomClick = onCreateRoomClick,
                    onEditRoomsToggle = onEditRoomsToggle,
                    showEditButton = showEditButton,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.rooms_header_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            RoomsSummaryMetrics(
                totalActiveDevices = totalActiveDevices,
                activeRoomsCount = activeRoomsCount,
            )
        }
    }
}

@Composable
private fun RoomsHeaderTitle(
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.rooms_header_title),
        modifier = modifier,
        style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun RoomsSummaryMetrics(
    totalActiveDevices: Int,
    activeRoomsCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomsSummaryMetric(
            icon = com.grupo4.smarthome.ui.theme.SmartHomeIcons.Devices,
            value = totalActiveDevices,
            label = stringResource(R.string.rooms_summary_metric_active),
        )
        Box(
            modifier = Modifier
                .size(width = 1.dp, height = 14.dp)
                .background(MaterialTheme.colorScheme.outline),
        )
        RoomsSummaryMetric(
            icon = com.grupo4.smarthome.ui.theme.SmartHomeIcons.Rooms,
            value = activeRoomsCount,
            label = stringResource(R.string.rooms_summary_metric_rooms),
        )
    }
}

@Composable
private fun RoomsSummaryMetric(
    icon: ImageVector,
    value: Int,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RoomsHeaderActions(
    editMode: Boolean,
    compact: Boolean,
    onCreateRoomClick: () -> Unit,
    onEditRoomsToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showEditButton: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val editLabel = stringResource(
            when {
                editMode -> R.string.rooms_finish_editing
                compact -> R.string.rooms_edit_short
                else -> R.string.rooms_edit_rooms
            },
        )
        if (showEditButton) {
            if (compact) {
                RoomsEditIconButton(
                    onClick = onEditRoomsToggle,
                    contentDescription = editLabel,
                )
            } else {
                OutlinedButton(
                    onClick = onEditRoomsToggle,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = ButtonDefaults.compactContentPadding(false),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = com.grupo4.smarthome.ui.theme.SmartHomeIcons.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = editLabel,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Button(
            onClick = onCreateRoomClick,
            modifier = Modifier.height(if (compact) 40.dp else 48.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = ButtonDefaults.compactContentPadding(compact),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandIndigoDark,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                imageVector = com.grupo4.smarthome.ui.theme.SmartHomeIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 18.dp else 20.dp),
            )
            Spacer(modifier = Modifier.width(if (compact) 6.dp else 8.dp))
            Text(
                text = stringResource(
                    if (compact) R.string.rooms_create_room_short else R.string.rooms_create_room,
                ),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun ButtonDefaults.compactContentPadding(compact: Boolean) =
    androidx.compose.foundation.layout.PaddingValues(
        horizontal = if (compact) 12.dp else 18.dp,
        vertical = 0.dp,
    )

@Composable
fun RoomsEditIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    SectionEditIconButton(
        onClick = onClick,
        contentDescription = contentDescription,
        modifier = modifier,
        size = size,
    )
}

@Composable
fun RoomsFilterChips(
    selectedRoomType: RoomType?,
    filterCounts: Map<RoomType, Int>,
    totalRooms: Int,
    onFilterSelected: (RoomType?) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showCounts: Boolean = true,
    showCategoryLabel: Boolean = false,
) {
    Column(modifier = modifier) {
        if (showCategoryLabel) {
            Text(
                text = stringResource(R.string.routines_filter_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            RoomsFilterChip(
                label = stringResource(R.string.rooms_filter_all),
                count = totalRooms,
                selected = selectedRoomType == null,
                onClick = { onFilterSelected(null) },
                compact = compact,
                showCount = showCounts,
            )
            RoomType.entries
                .filter { (filterCounts[it] ?: 0) > 0 || it == selectedRoomType }
                .forEach { type ->
                    RoomsFilterChip(
                        label = roomTypeLabel(type),
                        count = filterCounts[type] ?: 0,
                        selected = selectedRoomType == type,
                        onClick = { onFilterSelected(type) },
                        compact = compact,
                        showCount = showCounts,
                    )
                }
        }
    }
}

@Composable
private fun RoomsFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    compact: Boolean,
    showCount: Boolean,
) {
    val containerColor = if (selected) RoomsSelectedChipContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .height(if (compact) 36.dp else 44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = when {
            selected -> BorderStroke(1.dp, RoomsSelectedChipBorder)
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        shadowElevation = if (selected || compact) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 12.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        ) {
            Text(
                text = label,
                style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            if (showCount) {
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                selected -> MaterialTheme.colorScheme.surface
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape,
                        )
                        .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                }
            }
        }
    }
}

enum class RoomCardVariant {
    Standard,
    Compact,
    Phone,
}

@Composable
fun RoomCard(
    room: Room,
    editMode: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    variant: RoomCardVariant = RoomCardVariant.Standard,
    selected: Boolean = false,
) {
    val compact = variant == RoomCardVariant.Compact
    val phone = variant == RoomCardVariant.Phone
    val cardBorder = when {
        selected -> BorderStroke(1.dp, RoomsSelectedChipBorder)
        editMode -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        compact || phone -> BorderStroke(1.dp, RoomsSoftBorder)
        else -> BorderStroke(1.dp, RoomsSoftBorder)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(
                minHeight = when {
                    phone && editMode -> 146.dp
                    phone -> 96.dp
                    compact -> 118.dp
                    else -> 198.dp
                },
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(if (phone) 18.dp else if (compact) 12.dp else 14.dp),
        color = if (selected) RoomsSelectedChipContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface,
        border = cardBorder,
        shadowElevation = when {
            phone -> 2.dp
            compact -> 0.dp
            else -> 3.dp
        },
    ) {
        Column(
            modifier = Modifier.padding(
                when {
                    phone -> 16.dp
                    compact -> 12.dp
                    else -> 18.dp
                },
            ),
            verticalArrangement = Arrangement.spacedBy(if (compact || phone) 10.dp else 12.dp),
        ) {
            if (phone) {
                RoomCardPhoneContent(
                    room = room,
                    onToggleFavorite = onToggleFavorite,
                )
            } else if (compact) {
                RoomCardCompactContent(
                    room = room,
                    onToggleFavorite = onToggleFavorite,
                )
                RoomCompactMetrics(
                    totalDevices = room.devicesTotal,
                    activeDevices = room.devicesActive,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    RoomTypeIconTile(room = room, compact = false)
                    RoomFavoriteButton(
                        isFavorite = room.isFavorite,
                        compact = false,
                        onToggleFavorite = onToggleFavorite,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    RoomCardCopy(
                        room = room,
                        compact = false,
                        modifier = Modifier.weight(1f),
                    )
                    RoomActiveDonut(
                        active = room.devicesActive,
                        total = room.devicesTotal,
                        modifier = Modifier.size(72.dp),
                    )
                }
            }

            if (editMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ButtonDefaults.compactContentPadding(compact || phone),
                    ) {
                        Text(text = stringResource(R.string.rooms_edit_room), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = ButtonDefaults.compactContentPadding(compact || phone),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(text = stringResource(R.string.rooms_delete_room), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomCardPhoneContent(
    room: Room,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoomTypeIconTile(room = room, compact = true)
        Column(modifier = Modifier.weight(1f)) {
            RoomCardCopy(
                room = room,
                compact = false,
                showStatusLine = false,
            )
            Spacer(modifier = Modifier.height(5.dp))
            RoomDevicesSummaryText(
                activeDevices = room.devicesActive,
                totalDevices = room.devicesTotal,
            )
        }
        RoomFavoriteButton(
            isFavorite = room.isFavorite,
            compact = true,
            onToggleFavorite = onToggleFavorite,
        )
        Icon(
            imageVector = com.grupo4.smarthome.ui.theme.SmartHomeIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun RoomCardCompactContent(
    room: Room,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RoomTypeIconTile(room = room, compact = true)
        RoomCardCopy(
            room = room,
            compact = true,
            showStatusLine = false,
            modifier = Modifier.weight(1f),
        )
        RoomFavoriteButton(
            isFavorite = room.isFavorite,
            compact = true,
            onToggleFavorite = onToggleFavorite,
            buttonSize = 44.dp,
            iconSize = 26.dp,
        )
    }
}

@Composable
private fun RoomCompactMetrics(
    totalDevices: Int,
    activeDevices: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoomCompactMetric(
            icon = com.grupo4.smarthome.ui.theme.SmartHomeIcons.Devices,
            text = pluralStringResource(R.plurals.rooms_devices_count, totalDevices, totalDevices),
        )
        RoomCompactMetric(
            icon = com.grupo4.smarthome.ui.theme.SmartHomeIcons.Active,
            text = pluralStringResource(R.plurals.rooms_active_count, activeDevices, activeDevices),
        )
    }
}

@Composable
private fun RoomCompactMetric(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RoomTypeIconTile(
    room: Room,
    compact: Boolean,
) {
    Box(
        modifier = Modifier
            .size(if (compact) 42.dp else 58.dp)
            .background(
                RoomsIconTileGradient,
                RoundedCornerShape(if (compact) 10.dp else 12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = roomTypeIcon(room.type),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (compact) 23.dp else 30.dp),
        )
    }
}

@Composable
private fun RoomFavoriteButton(
    isFavorite: Boolean,
    compact: Boolean,
    onToggleFavorite: () -> Unit,
    buttonSize: Dp = if (compact) 32.dp else 34.dp,
    iconSize: Dp = if (compact) 20.dp else 24.dp,
) {
    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(buttonSize)) {
        Icon(
            imageVector = if (isFavorite) {
                com.grupo4.smarthome.ui.theme.SmartHomeIcons.Favorite
            } else {
                com.grupo4.smarthome.ui.theme.SmartHomeIcons.FavoriteBorder
            },
            contentDescription = stringResource(R.string.rooms_favorite_content_description),
            tint = if (isFavorite) StarActive else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun RoomCardCopy(
    room: Room,
    compact: Boolean,
    modifier: Modifier = Modifier,
    showStatusLine: Boolean = true,
) {
    Column(modifier = modifier) {
        Text(
            text = room.name,
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showStatusLine) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    if (room.devicesTotal == 0) {
                        R.string.rooms_no_linked_devices
                    } else {
                        R.string.rooms_active_devices_label
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RoomDevicesSummaryText(
    activeDevices: Int,
    totalDevices: Int,
) {
    Text(
        text = if (totalDevices == 0) {
            stringResource(R.string.rooms_no_linked_devices)
        } else {
            pluralStringResource(
                R.plurals.rooms_active_devices_progress,
                totalDevices,
                activeDevices,
                totalDevices,
            )
        },
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun RoomActiveDonut(
    active: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (total == 0) 0f else min(1f, active.toFloat() / total.toFloat())
    val trackColor = RoomsControlTrack

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2f
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            drawArc(
                brush = RoomsControlGradient,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
        }
        Text(
            text = stringResource(R.string.rooms_active_fraction, active, total),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun RoomsEmptyState(
    title: String,
    description: String,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = com.grupo4.smarthome.ui.theme.SmartHomeIcons.Rooms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigoDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(text = buttonLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}
