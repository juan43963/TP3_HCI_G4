package com.grupo4.smarthome.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.StarActive

@Composable
fun InfoEmptyStateCard(
    messagePrefix: String,
    highlightedTarget: String,
    messageSuffix: String,
    actionLabel: String,
    tabletActionLabel: String,
    onActionClick: () -> Unit,
    onTabletActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FavoriteEmptyTabletCard(
        messagePrefix = messagePrefix,
        highlightedTarget = highlightedTarget,
        messageSuffix = messageSuffix,
        actionLabel = tabletActionLabel,
        onActionClick = onTabletActionClick,
        modifier = modifier,
    )
}

@Composable
private fun FavoriteEmptyTabletCard(
    messagePrefix: String,
    highlightedTarget: String,
    messageSuffix: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .dashedBorder(borderColor, 14.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = StarActive.copy(alpha = 0.14f),
            ) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = SmartHomeIcons.FavoriteBorder,
                        contentDescription = null,
                        tint = StarActive,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            FavoriteMessage(
                messagePrefix = messagePrefix,
                highlightedTarget = highlightedTarget,
                messageSuffix = messageSuffix,
                centerText = false,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun FavoriteMessage(
    messagePrefix: String,
    highlightedTarget: String,
    messageSuffix: String,
    centerText: Boolean,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val annotatedText = buildAnnotatedString {
        append(messagePrefix)
        append(" ")
        pushStyle(SpanStyle(color = primary))
        append(highlightedTarget)
        pop()
        append(" ")
        append(messageSuffix)
    }

    Text(
        text = annotatedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = if (centerText) androidx.compose.ui.text.style.TextAlign.Center
        else androidx.compose.ui.text.style.TextAlign.Start,
    )
}

private fun Modifier.dashedBorder(
    color: androidx.compose.ui.graphics.Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
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
