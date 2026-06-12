package com.grupo4.smarthome.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.grupo4.smarthome.R

enum class SmartHomeWordmarkSize { Compact, Regular, Large }

@Composable
fun SmartHomeWordmark(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    size: SmartHomeWordmarkSize = if (compact) SmartHomeWordmarkSize.Compact else SmartHomeWordmarkSize.Regular,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
) {
    val textStyle = when (size) {
        SmartHomeWordmarkSize.Compact -> MaterialTheme.typography.titleMedium
        SmartHomeWordmarkSize.Regular -> MaterialTheme.typography.titleLarge
        SmartHomeWordmarkSize.Large -> MaterialTheme.typography.headlineSmall
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.welcome_brand_smart),
            style = textStyle,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.welcome_brand_home),
            style = textStyle,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
