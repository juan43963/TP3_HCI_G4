package com.grupo4.smarthome.ui.screens.device

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCard
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCardVariant
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsIconTileGradient
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsSoftBorder
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon

// Detalle de un dispositivo. Stateless: recibe el device ya cargado (del hogar activo) y el nombre de
// su habitación, y delega el control en los callbacks. Reutiliza DeviceCard tal cual (núcleo del
// control) y agrega solo la fila de "Habitación", que la card no muestra. Pensada para reutilizarse
// luego en búsqueda global, notificaciones y favoritos.
@Composable
fun DeviceDetailScreen(
    device: Device,
    roomName: String?,
    isTablet: Boolean,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onValueChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        DeviceDetailTopBar(onBack = onBack)

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxContentWidth = if (isTablet) 640.dp else maxWidth
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = maxContentWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RoomInfoRow(device = device, roomName = roomName)
                DeviceCard(
                    device = device,
                    editMode = false,
                    onToggle = onToggle,
                    onValueChange = onValueChange,
                    onEdit = {},
                    onDelete = {},
                    variant = if (isTablet) DeviceCardVariant.Standard else DeviceCardVariant.Phone,
                )
            }
        }
    }
}

@Composable
private fun DeviceDetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 4.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = com.grupo4.smarthome.ui.theme.SmartHomeIcons.Back,
                contentDescription = stringResource(R.string.topbar_back_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = stringResource(R.string.device_detail_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RoomInfoRow(
    device: Device,
    roomName: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RoomsSoftBorder),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(RoomsIconTileGradient, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = deviceTypeIcon(device.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.device_detail_room_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = roomName?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.device_detail_no_room),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
