package com.grupo4.smarthome.ui.screens.routines

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
import androidx.compose.ui.graphics.Color
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

internal enum class RoutinesEmptyVisual {
    Home,
    Search,
}

@Composable
internal fun RoutinesEmptyStateCard(
    onCreateRoutineClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    dashed: Boolean = false,
    visual: RoutinesEmptyVisual = RoutinesEmptyVisual.Home,
) {
    val shape = RoundedCornerShape(22.dp)
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val cardModifier = Modifier
        .fillMaxWidth()
        .heightIn(
            min = when {
                compact -> 280.dp
                dashed -> 360.dp
                else -> 460.dp
            },
        )
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)
        .then(
            if (dashed) Modifier.dashedBorder(borderColor, 22.dp)
            else Modifier
        )
        .padding(horizontal = if (compact) 22.dp else 28.dp, vertical = if (compact) 28.dp else 36.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = if (dashed) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = if (dashed) 0.dp else 1.dp,
    ) {
        Box(
            modifier = cardModifier,
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp),
            ) {
                if (visual == RoutinesEmptyVisual.Search) {
                    RoutineSearchIllustration(size = if (compact) 132.dp else 120.dp)
                } else {
                    RoutineHomeIllustration(size = if (compact) 168.dp else 132.dp)
                }
                Text(
                    text = stringResource(R.string.routines_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.routines_empty_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onCreateRoutineClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandIndigoDark,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        imageVector = SmartHomeIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.routines_empty_create_first),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun RoutinesEmptyDetailPane(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RoutineDetailIllustration(size = 178.dp)
                Text(
                    text = stringResource(R.string.routines_empty_detail_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.routines_empty_detail_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Icon(
                        imageVector = SmartHomeIcons.Play,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.routines_empty_detail_action),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineHomeIllustration(size: Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.78f)
                .dashedCircle(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
        )
        Box(
            modifier = Modifier
                .size(size * 0.5f)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.44f),
                    shape = CircleShape,
                ),
        )
        Icon(
            imageVector = SmartHomeIcons.Home,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f),
            modifier = Modifier.size(size * 0.34f),
        )
        Icon(
            imageVector = SmartHomeIcons.Routines,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
            modifier = Modifier.size(size * 0.13f),
        )
        RoutineIllustrationBubble(
            icon = SmartHomeIcons.Schedule,
            size = size * 0.22f,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = size * 0.04f, top = size * 0.08f),
        )
        RoutineIllustrationBubble(
            icon = SmartHomeIcons.MorningRoutine,
            size = size * 0.22f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = size * 0.04f, top = size * 0.16f),
        )
        RoutineIllustrationBubble(
            icon = SmartHomeIcons.NightRoutine,
            size = size * 0.22f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = size * 0.04f),
        )
    }
}

@Composable
private fun RoutineSearchIllustration(size: Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SmartHomeIcons.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f),
            modifier = Modifier.size(size * 0.34f),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = size * 0.18f, end = size * 0.2f)
                .size(size * 0.14f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = SmartHomeIcons.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(size * 0.08f),
            )
        }
    }
}

@Composable
private fun RoutineDetailIllustration(size: Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.76f)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(width = size * 0.42f, height = size * 0.5f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                .padding(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                    )
                }
            }
        }
        RoutineIllustrationBubble(
            icon = SmartHomeIcons.Calendar,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = size * 0.08f, bottom = size * 0.12f),
        )
        RoutineIllustrationBubble(
            icon = SmartHomeIcons.Schedule,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = size * 0.08f, bottom = size * 0.12f),
        )
    }
}

@Composable
private fun RoutineIllustrationBubble(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(size * 0.18f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
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

private fun Modifier.dashedCircle(color: Color): Modifier = drawBehind {
    drawCircle(
        color = color,
        style = Stroke(
            width = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f),
        ),
    )
}

@Preview(name = "Routines Empty - Phone", showBackground = true, widthDp = 393, heightDp = 520)
@Composable
private fun RoutinesEmptyPhonePreview() {
    SmartHomeTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            RoutinesEmptyStateCard(
                onCreateRoutineClick = {},
                compact = true,
            )
        }
    }
}

@Preview(name = "Routines Empty Detail - Tablet", showBackground = true, widthDp = 520, heightDp = 520)
@Composable
private fun RoutinesEmptyDetailPreview() {
    SmartHomeTheme(darkTheme = false) {
        RoutinesEmptyDetailPane()
    }
}
