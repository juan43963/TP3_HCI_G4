package com.grupo4.smarthome.ui.screens.rooms.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.data.model.QrDevicePayload
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.components.ModalHeader
import com.grupo4.smarthome.ui.screens.rooms.ScannedDeviceRoomTarget
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeLabel
import com.grupo4.smarthome.ui.screens.rooms.components.roomTypeIcon
import com.grupo4.smarthome.ui.screens.rooms.components.roomTypeLabel
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

// Alta de un dispositivo escaneado en dos pasos: 1) elegir habitación existente o crear una nueva,
// 2) confirmar tipo (fijo del QR; si el QR no lo trae se elige) y nombre del dispositivo.
@Composable
fun AddScannedDeviceDialog(
    payload: QrDevicePayload,
    rooms: List<Room>,
    availableDeviceTypes: List<DeviceTypeInfo>,
    onDismiss: () -> Unit,
    onConfirm: (ScannedDeviceRoomTarget, DeviceTypeInfo, String) -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(1) }
    var selectedRoomId by rememberSaveable { mutableStateOf<String?>(null) }
    var newRoomName by rememberSaveable { mutableStateOf<String?>(null) }
    var newRoomTypeName by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreateRoom by rememberSaveable { mutableStateOf(false) }
    var deviceName by rememberSaveable { mutableStateOf(payload.suggestedName.orEmpty()) }
    // Guardamos el id del tipo elegido (saveable); el tipo en sí se resuelve del catálogo.
    var selectedTypeId by rememberSaveable { mutableStateOf<String?>(null) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val roomTarget: ScannedDeviceRoomTarget? = when {
        newRoomName != null && newRoomTypeName != null ->
            ScannedDeviceRoomTarget.New(newRoomName!!, RoomType.valueOf(newRoomTypeName!!))
        selectedRoomId != null -> ScannedDeviceRoomTarget.Existing(selectedRoomId!!)
        else -> null
    }
    // El QR trae un hint de tipo (un nombre): lo resolvemos contra el catálogo real por nombre canónico.
    val qrType = payload.typeName?.let { name ->
        val canonical = DeviceTypeNames.canonical(name)
        availableDeviceTypes.firstOrNull { it.canonicalName == canonical }
    }
    // Si el QR resolvió un tipo, queda fijo; si no, vale el que elija el usuario.
    val deviceType = qrType ?: selectedTypeId?.let { id -> availableDeviceTypes.firstOrNull { it.id == id } }
    val headerIcon = qrType?.let { deviceTypeIcon(it) } ?: SmartHomeIcons.Devices

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 640.dp)
                    .heightIn(max = maxHeight),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column {
                    ModalHeader(
                        icon = headerIcon,
                        eyebrow = stringResource(
                            if (step == 1) R.string.scanned_device_step_room_label
                            else R.string.scanned_device_step_device_label,
                        ),
                        title = stringResource(R.string.scanned_device_title),
                        subtitle = stringResource(
                            if (step == 1) R.string.scanned_device_room_subtitle
                            else R.string.scanned_device_device_subtitle,
                        ),
                        onDismiss = onDismiss,
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (step == 1) {
                        RoomStep(
                            rooms = rooms,
                            selectedRoomId = selectedRoomId,
                            newRoomName = newRoomName,
                            onSelectExisting = {
                                selectedRoomId = it
                                newRoomName = null
                                newRoomTypeName = null
                            },
                            onCreateNew = { showCreateRoom = true },
                        )
                        if (submitted && roomTarget == null) {
                            Text(
                                text = stringResource(R.string.scanned_device_room_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else {
                        DeviceStep(
                            fixedType = qrType,
                            availableDeviceTypes = availableDeviceTypes,
                            selectedType = deviceType,
                            onSelectType = { selectedTypeId = it.id },
                            deviceName = deviceName,
                            onNameChange = { deviceName = it },
                            nameErrorRes = if (!submitted) null else when {
                                deviceName.isBlank() -> R.string.rooms_error_required_name
                                !isDeviceNameValid(deviceName) -> R.string.rooms_error_name_too_short
                                else -> null
                            },
                            showTypeError = submitted && deviceType == null,
                        )
                    }
                }
                Footer(
                    step = step,
                    onCancel = onDismiss,
                    onBack = { step = 1 },
                    onNext = {
                        submitted = true
                        if (roomTarget != null) {
                            submitted = false
                            step = 2
                        }
                    },
                    onConfirm = {
                        submitted = true
                        val target = roomTarget
                        val type = deviceType
                        if (target != null && type != null && isDeviceNameValid(deviceName)) {
                            onConfirm(target, type, deviceName.trim())
                        }
                    },
                )
                }
            }
        }
    }

    if (showCreateRoom) {
        AddEditRoomDialog(
            room = null,
            onDismiss = { showCreateRoom = false },
            onConfirm = { name, type ->
                newRoomName = name.trim()
                newRoomTypeName = type.name
                selectedRoomId = null
                showCreateRoom = false
            },
        )
    }
}

@Composable
private fun RoomStep(
    rooms: List<Room>,
    selectedRoomId: String?,
    newRoomName: String?,
    onSelectExisting: (String) -> Unit,
    onCreateNew: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.scanned_device_choose_room),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (rooms.isEmpty() && newRoomName == null) {
            Text(
                text = stringResource(R.string.scanned_device_no_rooms),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (rooms.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rooms.forEach { room ->
                    RoomRow(
                        icon = roomTypeIcon(room.type),
                        title = room.name,
                        subtitle = roomTypeLabel(room.type),
                        selected = newRoomName == null && selectedRoomId == room.id,
                        onClick = { onSelectExisting(room.id) },
                    )
                }
            }
        }
        if (newRoomName != null) {
            RoomRow(
                icon = SmartHomeIcons.Add,
                title = newRoomName,
                subtitle = stringResource(R.string.scanned_device_new_room_tag),
                selected = true,
                onClick = onCreateNew,
            )
        }
        OutlinedButton(
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.scanned_device_create_room),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RoomRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(
                    imageVector = SmartHomeIcons.Selected,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DeviceStep(
    fixedType: DeviceTypeInfo?,
    availableDeviceTypes: List<DeviceTypeInfo>,
    selectedType: DeviceTypeInfo?,
    onSelectType: (DeviceTypeInfo) -> Unit,
    deviceName: String,
    onNameChange: (String) -> Unit,
    nameErrorRes: Int?,
    showTypeError: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.scanned_device_type_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (fixedType != null) {
            DeviceTypeTile(type = fixedType, selected = true, onClick = {})
        } else {
            Text(
                text = stringResource(R.string.scanned_device_pick_type),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DeviceTypeGrid(
                types = availableDeviceTypes,
                selectedType = selectedType,
                onSelected = onSelectType,
            )
            if (showTypeError) {
                Text(
                    text = stringResource(R.string.scanned_device_type_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        OutlinedTextField(
            value = deviceName,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.rooms_field_name)) },
            placeholder = { Text(stringResource(R.string.rooms_device_name_placeholder)) },
            singleLine = true,
            isError = nameErrorRes != null,
            supportingText = {
                if (nameErrorRes != null) {
                    Text(text = stringResource(nameErrorRes))
                }
            },
        )
    }
}

@Composable
private fun DeviceTypeGrid(
    types: List<DeviceTypeInfo>,
    selectedType: DeviceTypeInfo?,
    onSelected: (DeviceTypeInfo) -> Unit,
) {
    BoxWithConstraints {
        val columns = when {
            maxWidth < 380.dp -> 2
            maxWidth < 560.dp -> 3
            else -> 4
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            types.chunked(columns).forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowTypes.forEach { type ->
                        DeviceTypeTile(
                            type = type,
                            selected = selectedType?.id == type.id,
                            onClick = { onSelected(type) },
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
private fun DeviceTypeTile(
    type: DeviceTypeInfo,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = deviceTypeIcon(type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = deviceTypeLabel(type),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Footer(
    step: Int,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        if (step == 2) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(10.dp)) {
                Text(text = stringResource(R.string.rooms_back), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(10.dp)) {
            Text(text = stringResource(R.string.rooms_cancel), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = if (step == 1) onNext else onConfirm,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandIndigoDark,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(
                    if (step == 1) R.string.rooms_next else R.string.scanned_device_confirm,
                ),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}
