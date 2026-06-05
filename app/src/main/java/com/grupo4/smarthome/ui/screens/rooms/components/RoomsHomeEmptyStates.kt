package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    val minHeight = if (compact) 300.dp else 360.dp
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .dashedBorder(borderColor, 22.dp)
            .padding(horizontal = if (compact) 20.dp else 28.dp, vertical = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp),
        ) {
            RoomsEmptyIllustration(size = if (compact) 118.dp else 132.dp)
            Text(
                text = stringResource(R.string.rooms_empty_home_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.rooms_empty_home_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
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
            TextButton(onClick = {}) {
                Icon(
                    imageVector = SmartHomeIcons.Play,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.rooms_empty_home_help),
                    fontWeight = FontWeight.Bold,
                )
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.rooms_empty_detail_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
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

@Preview(
    name = "Rooms Empty State - Phone",
    showBackground = true,
    widthDp = 393,
    heightDp = 520,
)
@Composable
private fun RoomsHomeEmptyStatePhonePreview() {
    SmartHomeTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            RoomsHomeEmptyState(
                buttonLabel = stringResource(R.string.rooms_empty_cta),
                onCreateRoomClick = {},
                compact = true,
            )
        }
    }
}

@Preview(
    name = "Rooms Empty Detail - Tablet",
    showBackground = true,
    widthDp = 480,
    heightDp = 520,
)
@Composable
private fun RoomsEmptyDetailPlaceholderPreview() {
    SmartHomeTheme(darkTheme = false) {
        RoomsEmptyDetailPlaceholder(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}
