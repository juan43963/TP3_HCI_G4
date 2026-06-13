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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.ActionArg
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCard
import com.grupo4.smarthome.ui.screens.rooms.components.DeviceCardVariant
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsIconTileGradient
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsSoftBorder
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

/**
 * Pantalla de detalle de un dispositivo: top bar + [DeviceDetailContent]. Stateless: recibe el device ya
 * cargado (del hogar activo), el nombre de su habitación y delega el control en los callbacks.
 *
 * Es el destino del flujo QR de dispositivo conocido: al escanear un QR que matchea un device del hogar,
 * `ScanViewModel` emite `ScanNavEvent.OpenDevice` y la navegación (ruta `device_detail/{deviceId}` en
 * `MainActivity`) abre esta pantalla. El cuerpo reusable vive en [DeviceDetailContent], pensado además
 * para reutilizarse luego desde búsqueda global (desde su propio scaffold o un bottom sheet) sin
 * duplicar lógica.
 */
@Composable
fun DeviceDetailScreen(
    device: Device,
    roomName: String?,
    isTablet: Boolean,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onValueChange: (Int) -> Unit,
    onAction: (actionName: String, args: List<ActionArg>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        DeviceDetailTopBar(title = device.name, onBack = onBack)
        DeviceDetailContent(
            device = device,
            roomName = roomName,
            isTablet = isTablet,
            onToggle = onToggle,
            onValueChange = onValueChange,
            onAction = onAction,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Cuerpo reutilizable del detalle de dispositivo (sin scaffold/top bar): la card de control + la fila de
 * habitación. Pensado para usarse tanto en el flujo de QR de dispositivo conocido como, más adelante, en
 * búsqueda global (desde su propio scaffold o un bottom sheet), sin duplicar lógica.
 *
 * Reutiliza [DeviceCard] (núcleo del control: ícono/nombre/tipo/estado + acción primaria + "Más
 * acciones") y los mappings del catálogo (`DeviceTypeInfo`); no agrega lógica de tipos propia. Usa la
 * variante `Phone` en ambas plataformas (controles inline) porque el detalle es una vista enfocada de un
 * solo dispositivo; en tablet se centra con un ancho máximo.
 */
@Composable
fun DeviceDetailContent(
    device: Device,
    roomName: String?,
    isTablet: Boolean,
    onToggle: () -> Unit,
    onValueChange: (Int) -> Unit,
    onAction: (actionName: String, args: List<ActionArg>) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val maxContentWidth = if (isTablet) 560.dp else maxWidth
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = maxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DeviceCard(
                device = device,
                editMode = false,
                onToggle = onToggle,
                onValueChange = onValueChange,
                onEdit = {},
                onDelete = {},
                onAction = onAction,
                // Detalle: siempre la variante con controles inline (también en tablet, centrada).
                variant = DeviceCardVariant.Phone,
            )
            if (!roomName.isNullOrBlank()) {
                DeviceRoomRow(roomName = roomName)
            }
        }
    }
}

@Composable
private fun DeviceDetailTopBar(title: String, onBack: () -> Unit) {
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
                imageVector = SmartHomeIcons.Back,
                contentDescription = stringResource(R.string.topbar_back_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

// Fila de habitación: ícono de habitación (no el del dispositivo, que ya está en la card) + nombre.
@Composable
private fun DeviceRoomRow(roomName: String) {
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
                    imageVector = SmartHomeIcons.Rooms,
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
                    text = roomName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
