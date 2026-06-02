package com.grupo4.smarthome.ui.screens.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (example != null) {
            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (helpText != null) {
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            )
        }
    }
}

@Composable
private fun EmptyStateIllustration(
    visual: DashboardEmptyVisual,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector = when (visual) {
        DashboardEmptyVisual.Home -> SmartHomeIcons.Rooms
        DashboardEmptyVisual.Routine -> SmartHomeIcons.Routines
    }

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
        Box(
            modifier = Modifier
                .size(118.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(62.dp),
            )
        }
    }
}
