package com.grupo4.smarthome.ui.screens.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

enum class DashboardEmptyVisual {
    Home,
    Routine,
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    buttonLabel: String? = null,
    onButtonClick: (() -> Unit)? = null,
    note: String? = null,
    example: String? = null,
    helpText: String? = null,
    visual: DashboardEmptyVisual = DashboardEmptyVisual.Home,
) {
    BoxWithConstraints(modifier = modifier) {
        val twoColumns = maxWidth >= 640.dp

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = if (twoColumns) 300.dp else 0.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            if (twoColumns) {
                Row(
                    modifier = Modifier.padding(28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    EmptyStateCopy(
                        title = title,
                        description = description,
                        buttonLabel = buttonLabel,
                        onButtonClick = onButtonClick,
                        note = note,
                        example = example,
                        helpText = helpText,
                        modifier = Modifier.weight(0.9f),
                    )
                    EmptyStateIllustration(
                        visual = visual,
                        modifier = Modifier.weight(1.1f),
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    EmptyStateIllustration(visual = visual)
                    EmptyStateCopy(
                        title = title,
                        description = description,
                        buttonLabel = buttonLabel,
                        onButtonClick = onButtonClick,
                        note = note,
                        example = example,
                        helpText = helpText,
                        centered = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCopy(
    title: String,
    description: String,
    buttonLabel: String?,
    onButtonClick: (() -> Unit)?,
    note: String?,
    example: String?,
    helpText: String?,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
        )
        if (example != null) {
            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = textAlign,
            )
        }
        if (helpText != null) {
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
            )
        }
        if (buttonLabel != null && onButtonClick != null) {
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = buttonLabel,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (note != null) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
            )
        }
    }
}

@Composable
private fun EmptyStateIllustration(
    visual: DashboardEmptyVisual,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 190.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(22.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (visual) {
            DashboardEmptyVisual.Home -> LivingRoomScene(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
            )

            DashboardEmptyVisual.Routine -> Box(
                modifier = Modifier
                    .size(118.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val icon: ImageVector = SmartHomeIcons.Routines
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(62.dp),
                )
            }
        }
    }
}

@Composable
private fun LivingRoomScene(
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val lightest = primary.copy(alpha = 0.07f)
    val light = primary.copy(alpha = 0.16f)
    val soft = primary.copy(alpha = 0.26f)
    val mid = primary.copy(alpha = 0.40f)
    val deep = primary.copy(alpha = 0.55f)
    val shadow = primary.copy(alpha = 0.10f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        fun roundRect(
            x: Float,
            y: Float,
            rw: Float,
            rh: Float,
            radius: Float,
            fill: Color,
        ) {
            drawRoundRect(
                color = fill,
                topLeft = Offset(x, y),
                size = Size(rw, rh),
                cornerRadius = CornerRadius(radius, radius),
            )
        }

        fun roundRect(
            x: Float,
            y: Float,
            rw: Float,
            rh: Float,
            radius: Float,
            brush: Brush,
        ) {
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(rw, rh),
                cornerRadius = CornerRadius(radius, radius),
            )
        }

        fun contactShadow(cx: Float, cy: Float, rw: Float, rh: Float) {
            drawOval(
                color = shadow,
                topLeft = Offset(cx - rw / 2f, cy - rh / 2f),
                size = Size(rw, rh),
            )
        }

        val chairBrush = Brush.verticalGradient(
            colors = listOf(soft, mid),
            startY = h * 0.34f,
            endY = h * 0.72f,
        )
        val cushionBrush = Brush.verticalGradient(
            colors = listOf(mid, deep),
            startY = h * 0.48f,
            endY = h * 0.66f,
        )

        // Rug
        drawOval(
            color = lightest,
            topLeft = Offset(w * 0.07f, h * 0.70f),
            size = Size(w * 0.86f, h * 0.24f),
        )
        drawOval(
            color = light,
            topLeft = Offset(w * 0.18f, h * 0.75f),
            size = Size(w * 0.64f, h * 0.13f),
        )

        // Wall picture
        roundRect(w * 0.15f, h * 0.11f, w * 0.16f, h * 0.24f, h * 0.035f, fill = onPrimary.copy(alpha = 0.85f))
        roundRect(w * 0.17f, h * 0.13f, w * 0.12f, h * 0.20f, h * 0.025f, fill = light)
        drawCircle(color = tertiary.copy(alpha = 0.45f), radius = h * 0.035f, center = Offset(w * 0.205f, h * 0.19f))
        roundRect(w * 0.17f, h * 0.24f, w * 0.12f, h * 0.09f, h * 0.02f, fill = soft)

        // Contact shadows
        contactShadow(w * 0.49f, h * 0.77f, w * 0.40f, h * 0.07f)
        contactShadow(w * 0.185f, h * 0.79f, w * 0.18f, h * 0.045f)
        contactShadow(w * 0.85f, h * 0.79f, w * 0.12f, h * 0.04f)
        contactShadow(w * 0.725f, h * 0.79f, w * 0.13f, h * 0.035f)

        // Floor lamp (right)
        val lampX = w * 0.85f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(lampX, h * 0.27f),
                radius = h * 0.18f,
            ),
            radius = h * 0.18f,
            center = Offset(lampX, h * 0.27f),
        )
        roundRect(lampX - w * 0.011f, h * 0.28f, w * 0.022f, h * 0.50f, w * 0.011f, fill = soft)
        roundRect(lampX - w * 0.05f, h * 0.745f, w * 0.10f, h * 0.045f, h * 0.02f, fill = mid)
        val shade = Path().apply {
            moveTo(lampX - w * 0.05f, h * 0.29f)
            lineTo(lampX + w * 0.05f, h * 0.29f)
            lineTo(lampX + w * 0.072f, h * 0.15f)
            lineTo(lampX - w * 0.072f, h * 0.15f)
            close()
        }
        drawPath(
            shade,
            brush = Brush.verticalGradient(
                colors = listOf(soft, deep),
                startY = h * 0.15f,
                endY = h * 0.29f,
            ),
        )

        // Side table + vase
        val tableX = w * 0.685f
        val tableTop = h * 0.585f
        roundRect(tableX + w * 0.012f, tableTop + h * 0.03f, w * 0.015f, h * 0.16f, w * 0.007f, fill = soft)
        roundRect(tableX + w * 0.058f, tableTop + h * 0.03f, w * 0.015f, h * 0.16f, w * 0.007f, fill = soft)
        roundRect(tableX, tableTop, w * 0.085f, h * 0.05f, h * 0.02f, fill = mid)
        roundRect(tableX + w * 0.026f, tableTop - h * 0.10f, w * 0.034f, h * 0.10f, w * 0.017f, fill = tertiary.copy(alpha = 0.45f))

        // Armchair (center)
        val chairX = w * 0.31f
        val chairW = w * 0.36f
        roundRect(chairX, h * 0.34f, chairW, h * 0.34f, h * 0.11f, brush = chairBrush)
        roundRect(chairX + chairW * 0.16f, h * 0.40f, chairW * 0.68f, h * 0.20f, h * 0.08f, fill = light)
        roundRect(chairX - w * 0.005f, h * 0.46f, w * 0.085f, h * 0.24f, h * 0.06f, fill = mid)
        roundRect(chairX + chairW - w * 0.08f, h * 0.46f, w * 0.085f, h * 0.24f, h * 0.06f, fill = mid)
        roundRect(chairX + chairW * 0.10f, h * 0.55f, chairW * 0.80f, h * 0.16f, h * 0.07f, brush = cushionBrush)
        roundRect(chairX + chairW * 0.16f, h * 0.66f, w * 0.03f, h * 0.07f, w * 0.014f, fill = deep)
        roundRect(chairX + chairW * 0.78f, h * 0.66f, w * 0.03f, h * 0.07f, w * 0.014f, fill = deep)

        // Plant (left foreground)
        val potCx = w * 0.185f
        val potTop = h * 0.58f
        roundRect(potCx - w * 0.075f, potTop - h * 0.025f, w * 0.15f, h * 0.05f, h * 0.02f, fill = mid)
        val pot = Path().apply {
            moveTo(potCx - w * 0.065f, potTop)
            lineTo(potCx + w * 0.065f, potTop)
            lineTo(potCx + w * 0.05f, potTop + h * 0.20f)
            lineTo(potCx - w * 0.05f, potTop + h * 0.20f)
            close()
        }
        drawPath(
            pot,
            brush = Brush.verticalGradient(
                colors = listOf(soft, mid),
                startY = potTop,
                endY = potTop + h * 0.20f,
            ),
        )
        drawCircle(color = mid, radius = h * 0.075f, center = Offset(potCx, potTop - h * 0.07f))
        drawCircle(color = soft, radius = h * 0.058f, center = Offset(potCx - w * 0.05f, potTop - h * 0.03f))
        drawCircle(color = soft, radius = h * 0.058f, center = Offset(potCx + w * 0.05f, potTop - h * 0.03f))
        drawCircle(color = deep, radius = h * 0.045f, center = Offset(potCx, potTop - h * 0.12f))
    }
}
