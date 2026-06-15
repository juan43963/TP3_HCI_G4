package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.StarActive
import kotlin.math.min

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
    val corner = if (phone) 18.dp else if (compact) 12.dp else 14.dp
    val cardBorder = when {
        selected -> BorderStroke(1.dp, RoomsSelectedChipBorder)
        compact || phone -> BorderStroke(1.dp, RoomsSoftBorder)
        else -> BorderStroke(1.dp, RoomsSoftBorder)
    }
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(
                minHeight = when {
                    phone -> 96.dp
                    compact -> 118.dp
                    else -> 198.dp
                },
            )
            .then(if (editMode) Modifier.dashedBorder(primary, corner) else Modifier.clickable(onClick = onClick)),
        shape = RoundedCornerShape(corner),
        color = if (selected) RoomsSelectedChipContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface,
        border = if (editMode) null else cardBorder,
        shadowElevation = when {
            editMode -> 0.dp
            phone -> 2.dp
            compact -> 0.dp
            else -> 3.dp
        },
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(
                        when {
                            phone -> 16.dp
                            compact -> 12.dp
                            else -> 18.dp
                        },
                    )
                    .then(if (editMode) Modifier.alpha(0.4f) else Modifier),
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
            }

            if (editMode) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                )
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = primary,
                        ),
                    ) {
                        Icon(
                            imageVector = SmartHomeIcons.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.rooms_edit_room), fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(
                            imageVector = SmartHomeIcons.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.rooms_delete_room), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
        ),
    )
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
