package com.grupo4.smarthome.ui.screens.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.BrandIndigoLight
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun WelcomeSummaryCard(
    userName: String,
    activeDevicesCount: Int,
    roomsCount: Int,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val stacked = maxWidth < 720.dp

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = BrandIndigoLight.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        ) {
            if (stacked) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    WelcomeCopy(userName = userName)
                    MetricsRow(
                        activeDevicesCount = activeDevicesCount,
                        roomsCount = roomsCount,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    WelcomeCopy(
                        userName = userName,
                        modifier = Modifier.weight(1f),
                    )
                    MetricsRow(
                        activeDevicesCount = activeDevicesCount,
                        roomsCount = roomsCount,
                        modifier = Modifier.weight(1.05f),
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeCopy(
    userName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_welcome_title, userName),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.dashboard_welcome_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MetricsRow(
    activeDevicesCount: Int,
    roomsCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MetricCard(
            value = activeDevicesCount.toString(),
            label = stringResource(R.string.dashboard_metric_devices_on),
            icon = SmartHomeIcons.Devices,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        MetricCard(
            value = roomsCount.toString(),
            label = stringResource(R.string.dashboard_metric_rooms),
            icon = SmartHomeIcons.Rooms,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}
