package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun RoomsHomeEmptyState(
    buttonLabel: String,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val minHeight = if (compact) 340.dp else 420.dp
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .dashedBorder(borderColor, 24.dp)
            .padding(horizontal = if (compact) 24.dp else 36.dp, vertical = if (compact) 32.dp else 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 18.dp else 22.dp),
        ) {
            RoomsListEmptyIllustration(size = if (compact) 156.dp else 196.dp)
            Text(
                text = stringResource(R.string.rooms_empty_home_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.rooms_empty_home_description),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
            Button(
                onClick = onCreateRoomClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigoDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = buttonLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RoomsNoDevicesEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    card: Boolean = true,
    onAddDevice: (() -> Unit)? = null,
    addButtonLabel: String? = null,
) {
    if (card) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(if (compact) 20.dp else 22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, RoomsSoftBorder),
            shadowElevation = 1.dp,
        ) {
            RoomsNoDevicesContent(
                compact = compact,
                onAddDevice = onAddDevice,
                addButtonLabel = addButtonLabel,
                modifier = Modifier.padding(if (compact) 24.dp else 32.dp),
            )
        }
    } else {
        Box(
            modifier = modifier.padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            RoomsNoDevicesContent(
                compact = compact,
                onAddDevice = onAddDevice,
                addButtonLabel = addButtonLabel,
                modifier = Modifier.widthIn(max = 400.dp),
            )
        }
    }
}

@Composable
private fun RoomsNoDevicesContent(
    compact: Boolean,
    onAddDevice: (() -> Unit)?,
    addButtonLabel: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 16.dp else 20.dp),
    ) {
        RoomsDevicesEmptyIllustration(size = if (compact) 140.dp else 184.dp)
        Text(
            text = stringResource(R.string.rooms_empty_devices_title),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.rooms_empty_devices_description),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
        )
        if (onAddDevice != null && addButtonLabel != null) {
            Button(
                onClick = onAddDevice,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigoDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = addButtonLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RoomsEmptyDetailPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.width(360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RoomsEmptyIllustration(size = 154.dp, detail = true)
            Text(
                text = stringResource(R.string.rooms_empty_detail_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.rooms_empty_detail_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
            OutlinedButton(
                onClick = {},
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.rooms_empty_detail_example),
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                Text(
                    text = stringResource(R.string.rooms_empty_detail_or),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
            Button(
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                ),
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.rooms_add_device),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun RoomsEmptyIllustration(
    size: Dp,
    detail: Boolean = false,
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f),
                    shape = CircleShape,
                ),
        )
        Icon(
            imageVector = if (detail) SmartHomeIcons.Living else SmartHomeIcons.Rooms,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
            modifier = Modifier.size(size * 0.42f),
        )
        Icon(
            imageVector = if (detail) SmartHomeIcons.Living else SmartHomeIcons.Rooms,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = size * 0.18f, bottom = size * 0.1f)
                .size(size * 0.18f),
        )
        Icon(
            imageVector = SmartHomeIcons.Routines,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = size * 0.16f, end = size * 0.16f)
                .size(size * 0.1f),
        )
    }
}

@Composable
private fun RoomsListEmptyIllustration(size: Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SmartHomeIcons.Living,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(size * 0.6f),
        )
    }
}

@Composable
private fun RoomsDevicesEmptyIllustration(size: Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.82f)
                .dashedCircle(MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)),
        )
        Box(
            modifier = Modifier
                .size(size * 0.5f)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    CircleShape,
                ),
        )
        Icon(
            imageVector = SmartHomeIcons.Devices,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(size * 0.3f),
        )
        RoomsIllustrationBubble(
            icon = SmartHomeIcons.Lightbulb,
            size = size * 0.22f,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = size * 0.02f, top = size * 0.1f),
        )
        RoomsIllustrationBubble(
            icon = SmartHomeIcons.Speaker,
            size = size * 0.22f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = size * 0.02f, top = size * 0.06f),
        )
        RoomsIllustrationBubble(
            icon = SmartHomeIcons.Thermostat,
            size = size * 0.22f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = size * 0.06f, bottom = size * 0.08f),
        )
        RoomsIllustrationBubble(
            icon = SmartHomeIcons.AirConditioner,
            size = size * 0.22f,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = size * 0.06f, bottom = size * 0.04f),
        )
    }
}

@Composable
private fun RoomsIllustrationBubble(
    icon: ImageVector,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(size * 0.2f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun Modifier.dashedCircle(
    color: androidx.compose.ui.graphics.Color,
): Modifier = drawBehind {
    drawCircle(
        color = color,
        style = Stroke(
            width = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f),
        ),
    )
}

private fun Modifier.dashedBorder(
    color: androidx.compose.ui.graphics.Color,
    cornerRadius: Dp,
): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
        ),
    )
}

