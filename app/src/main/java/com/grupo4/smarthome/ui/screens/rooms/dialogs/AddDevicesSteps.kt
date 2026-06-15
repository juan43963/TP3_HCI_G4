package com.grupo4.smarthome.ui.screens.rooms.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.screens.rooms.PendingDevice
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeLabel
import com.grupo4.smarthome.ui.screens.rooms.components.roomTypeIcon
import com.grupo4.smarthome.ui.screens.rooms.components.roomTypeLabel
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
internal fun SelectDevicesStep(
    availableDeviceTypes: List<DeviceTypeInfo>,
    devices: List<PendingDevice>,
    onToggleType: (DeviceTypeInfo, String) -> Unit,
) {
    BoxWithConstraints {
        val columns = when {
            maxWidth < 380.dp -> 2
            maxWidth < 620.dp -> 3
            else -> 5
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            availableDeviceTypes.chunked(columns).forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowTypes.forEach { type ->
                        val label = deviceTypeLabel(type)
                        SelectableDeviceTile(
                            type = type,
                            label = label,
                            selected = devices.any { it.type.id == type.id },
                            onClick = { onToggleType(type, label) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - rowTypes.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableDeviceTile(
    type: DeviceTypeInfo,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            SelectionCheck(
                selected = selected,
                modifier = Modifier.align(Alignment.TopEnd),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = deviceTypeIcon(type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SelectionCheck(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Box(
            modifier = modifier
                .size(22.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(22.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
    }
}

@Composable
internal fun CustomizeDevicesStep(
    devices: List<PendingDevice>,
    onRename: (String, String) -> Unit,
    onRemove: (String) -> Unit,
) {
    if (devices.isEmpty()) {
        EmptySelectionHint()
        return
    }
    devices.forEach { device ->
        CustomizeDeviceRow(
            device = device,
            onRename = { onRename(device.id, it) },
            onClear = { onRename(device.id, "") },
            onRemove = { onRemove(device.id) },
        )
    }
}

@Composable
private fun CustomizeDeviceRow(
    device: PendingDevice,
    onRename: (String) -> Unit,
    onClear: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DeviceIconTile(type = device.type)
                Text(
                    text = deviceTypeLabel(device.type),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                DeleteDeviceButton(onClick = onRemove)
            }
            Spacer(modifier = Modifier.height(12.dp))
            val nameError = when {
                device.name.isBlank() -> R.string.rooms_error_required_name
                !isDeviceNameValid(device.name) -> R.string.rooms_error_name_too_short
                else -> null
            }
            OutlinedTextField(
                value = device.name,
                onValueChange = onRename,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.rooms_device_name_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = nameError != null,
                supportingText = nameError?.let { { Text(stringResource(it)) } },
                trailingIcon = {
                    if (device.name.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = SmartHomeIcons.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

@Composable
private fun DeleteDeviceButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = SmartHomeIcons.Delete,
            contentDescription = stringResource(R.string.rooms_delete),
            tint = ErrorRed,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
internal fun ReviewDevicesStep(
    roomName: String,
    roomType: RoomType,
    devices: List<PendingDevice>,
    onRemove: (String) -> Unit,
    onEditAll: () -> Unit,
    onRoomClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRoomClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = roomTypeIcon(roomType),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = roomTypeLabel(roomType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.rooms_change_room),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.rooms_devices_to_add, devices.size),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.rooms_edit_all),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onEditAll),
        )
    }

    devices.forEach { device ->
        ReviewDeviceRow(device = device, onRemove = { onRemove(device.id) })
    }
}

@Composable
private fun ReviewDeviceRow(
    device: PendingDevice,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DeviceIconTile(type = device.type)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name.ifBlank { deviceTypeLabel(device.type) },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = deviceTypeLabel(device.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DeleteDeviceButton(onClick = onRemove)
        }
    }
}

@Composable
private fun DeviceIconTile(type: DeviceTypeInfo) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = deviceTypeIcon(type),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun EmptySelectionHint() {
    Text(
        text = stringResource(R.string.rooms_no_devices_selected),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun RoomPickerList(
    rooms: List<Room>,
    selectedRoomId: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rooms.forEach { room ->
            val selected = room.id == selectedRoomId
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(room.id) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    DeviceIconTileRoom(roomType = room.type)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = roomTypeLabel(room.type),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceIconTileRoom(roomType: RoomType) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = roomTypeIcon(roomType),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
internal fun AddDevicesFooter(
    step: Int,
    hasDevices: Boolean,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (step > 1) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(text = stringResource(R.string.rooms_back), fontWeight = FontWeight.Bold)
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }
        if (hasDevices) {
            Button(
                onClick = if (step < 3) onNext else onConfirm,
                enabled = canProceed,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigoDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (step < 3) R.string.rooms_continue else R.string.rooms_add_devices_confirm,
                    ),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

