package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.ui.theme.BorderDefault
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

enum class DeviceCardVariant {
    Standard,
    Phone,
}

@Composable
fun DeviceCard(
    device: Device,
    editMode: Boolean,
    onToggle: () -> Unit,
    onValueChange: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DeviceCardVariant = DeviceCardVariant.Standard,
) {
    if (variant == DeviceCardVariant.Phone) {
        PhoneDeviceCard(
            device = device,
            editMode = editMode,
            onToggle = onToggle,
            onValueChange = onValueChange,
            onEdit = onEdit,
            onDelete = onDelete,
            modifier = modifier,
        )
        return
    }

    val cardShape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 224.dp),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (editMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.52f) else RoomsSoftBorder,
        ),
        shadowElevation = 1.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
            ) {
                DeviceCardHeader(
                    device = device,
                    onToggle = onToggle,
                )
                Spacer(modifier = Modifier.height(18.dp))
                DeviceControlContent(
                    device = device,
                    onToggle = onToggle,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
            if (editMode) {
                DeviceCardEditOverlay(
                    onEdit = onEdit,
                    onDelete = onDelete,
                    shape = cardShape,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}

@Composable
private fun DeviceCardEditOverlay(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DeviceEditActionButton(
                label = stringResource(R.string.rooms_edit_short),
                icon = SmartHomeIcons.Edit,
                onClick = onEdit,
                filled = true,
            )
            DeviceEditActionButton(
                label = stringResource(R.string.rooms_delete),
                icon = SmartHomeIcons.Delete,
                onClick = onDelete,
                destructive = true,
                filled = true,
            )
        }
    }
}

@Composable
private fun PhoneDeviceCard(
    device: Device,
    editMode: Boolean,
    onToggle: () -> Unit,
    onValueChange: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 130.dp)
            .then(if (editMode) Modifier.dashedBorder(primary, 20.dp) else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = if (editMode) null else BorderStroke(1.dp, RoomsSoftBorder),
        shadowElevation = if (editMode) 0.dp else 2.dp,
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .then(if (editMode) Modifier.alpha(0.4f) else Modifier),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DeviceCardHeader(
                    device = device,
                    onToggle = onToggle,
                )
                DeviceControlContent(
                    device = device,
                    onToggle = onToggle,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (editMode) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {},
                        ),
                )
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DeviceEditActionButton(
                        label = stringResource(R.string.rooms_edit_short),
                        icon = SmartHomeIcons.Edit,
                        onClick = onEdit,
                        filled = true,
                    )
                    DeviceEditActionButton(
                        label = stringResource(R.string.rooms_delete),
                        icon = SmartHomeIcons.Delete,
                        onClick = onDelete,
                        destructive = true,
                        filled = true,
                    )
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
private fun DeviceEditActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    filled: Boolean = false,
) {
    val contentColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        border = BorderStroke(1.dp, if (destructive) contentColor else contentColor.copy(alpha = 0.42f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            containerColor = if (filled) MaterialTheme.colorScheme.surface else Color.Transparent,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DeviceCardHeader(
    device: Device,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(RoomsIconTileGradient, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = deviceTypeIcon(device.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(29.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            DeviceMetadataText(device = device)
        }
        HeaderControl(
            device = device,
            onToggle = onToggle,
        )
    }
}

@Composable
private fun DeviceMetadataText(device: Device) {
    Text(
        text = deviceTypeLabel(device.type),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun HeaderControl(
    device: Device,
    onToggle: () -> Unit,
) {
    if (device.type == DeviceType.DOOR) {
        Icon(
            imageVector = if (device.locked == true) SmartHomeIcons.Locked else SmartHomeIcons.Unlocked,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 5.dp)
                .size(22.dp),
        )
    } else {
        RoomsGradientSwitch(
            checked = device.isOn,
            onCheckedChange = onToggle,
        )
    }
}

@Composable
private fun RoomsGradientSwitch(
    checked: Boolean,
    onCheckedChange: () -> Unit,
) {
    val thumbOffset = animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        label = "rooms_switch_thumb",
    )
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .clip(shape)
            .then(
                if (checked) {
                    Modifier.background(RoomsControlGradient)
                } else {
                    Modifier.background(RoomsSwitchInactiveTrack)
                },
            )
            .border(
                width = 1.dp,
                color = if (checked) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    RoomsSoftBorder
                },
                shape = shape,
            )
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = { onCheckedChange() },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset.value)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        )
    }
}

@Composable
private fun DeviceControlContent(
    device: Device,
    onToggle: () -> Unit,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        when (device.type) {
            DeviceType.LIGHT -> LightControl(device = device, onValueChange = onValueChange)
            DeviceType.AC -> AirConditionerControl(device = device, onValueChange = onValueChange)
            DeviceType.DOOR -> DoorControl(device = device, onToggle = onToggle)
            DeviceType.SPEAKER -> SpeakerControl(device = device, onValueChange = onValueChange)
            else -> NoAdditionalControl()
        }
    }
}

@Composable
private fun ControlValueRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun LightControl(
    device: Device,
    onValueChange: (Int) -> Unit,
) {
    if (!device.isOn) {
        return
    }
    val brightness = device.brightness ?: 0
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ControlValueRow(
            label = stringResource(R.string.rooms_device_intensity),
            value = "$brightness%",
        )
        RoomsGradientSlider(
            value = brightness.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
        )
    }
}

@Composable
private fun AirConditionerControl(
    device: Device,
    onValueChange: (Int) -> Unit,
) {
    if (!device.isOn) {
        return
    }
    val temperature = device.temperature ?: 22
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ControlValueRow(
            label = stringResource(R.string.rooms_device_temperature),
            value = "${temperature}°C",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TemperatureStepButton(
                label = "−",
                onClick = { onValueChange(temperature - 1) },
            )
            RoomsGradientSlider(
                value = temperature.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 16f..30f,
                modifier = Modifier.weight(1f),
            )
            TemperatureStepButton(
                label = "+",
                onClick = { onValueChange(temperature + 1) },
            )
        }
    }
}

@Composable
private fun TemperatureStepButton(
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.requiredHeight(38.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 13.dp),
        border = BorderStroke(1.dp, RoomsSoftBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SpeakerControl(
    device: Device,
    onValueChange: (Int) -> Unit,
) {
    if (!device.isOn) {
        return
    }
    val volume = device.volume ?: 5
    val volumeSteps = volume.coerceIn(0, 10)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ControlValueRow(
            label = stringResource(R.string.rooms_device_volume),
            value = stringResource(R.string.rooms_device_volume_fraction, volumeSteps),
        )
        RoomsGradientSlider(
            value = volume.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..10f,
        )
    }
}

@Composable
private fun RoomsGradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
) {
    val progress = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier.height(44.dp),
        contentAlignment = Alignment.Center,
    ) {
        val activeWidth = maxWidth * progress
        val trackShape = RoundedCornerShape(999.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(trackShape)
                .background(RoomsControlTrack),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(activeWidth)
                .height(6.dp)
                .clip(trackShape)
                .background(RoomsControlGradient),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun DoorControl(
    device: Device,
    onToggle: () -> Unit,
) {
    val locked = device.locked == true
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        OutlinedButton(
            onClick = onToggle,
            modifier = Modifier
                .height(40.dp)
                .widthIn(min = 128.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            border = BorderStroke(1.dp, RoomsSelectedChipBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(
                imageVector = if (locked) SmartHomeIcons.Unlocked else SmartHomeIcons.Locked,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(
                    if (locked) R.string.rooms_device_unlock else R.string.rooms_device_lock,
                ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun NoAdditionalControl() = Unit
