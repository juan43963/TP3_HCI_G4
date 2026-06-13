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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.ui.components.ModalCloseButton
import com.grupo4.smarthome.ui.components.ModalHeader
import com.grupo4.smarthome.ui.screens.rooms.PendingDevice
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeLabel
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.ErrorBg
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun AddDevicesToRoomDialog(
    roomName: String,
    availableDeviceTypes: List<DeviceTypeInfo>,
    onDismiss: () -> Unit,
    onConfirm: (List<PendingDevice>) -> Unit,
    initialStep: Int = 1,
    initialDevices: List<PendingDevice> = emptyList(),
) {
    var step by rememberSaveable { mutableIntStateOf(initialStep.coerceIn(1, 2)) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var counter by rememberSaveable { mutableIntStateOf(initialDevices.size) }
    val pendingDevices = remember {
        mutableStateListOf<PendingDevice>().also { it.addAll(initialDevices) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .widthIn(max = 860.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column {
                AddDevicesHeader(step = step, roomName = roomName, onDismiss = onDismiss)
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    AddDevicesStepper(
                        step = step,
                        onDevicesClick = { step = 1 },
                        onReviewClick = {
                            submitted = true
                            if (pendingDevices.isNotEmpty()) {
                                step = 2
                            }
                        },
                    )
                    if (step == 1) {
                        DeviceTypeGrid(
                            availableDeviceTypes = availableDeviceTypes,
                            pendingDevices = pendingDevices,
                            onDeviceTypeClick = { type, defaultLabel ->
                                counter += 1
                                val countForType = pendingDevices.count { it.type.id == type.id } + 1
                                val candidate = if (countForType == 1) defaultLabel else "$defaultLabel $countForType"
                                // La API exige nombres de >=3 caracteres; si el label es corto (p. ej. "AC")
                                // garantizamos el mínimo agregando el número.
                                val name = if (candidate.trim().length >= 3) candidate else "$defaultLabel $countForType"
                                pendingDevices += PendingDevice(
                                    id = "pending-$counter",
                                    type = type,
                                    name = name,
                                )
                                submitted = false
                            },
                        )
                        PendingDevicesList(
                            pendingDevices = pendingDevices,
                            onRemove = { pending -> pendingDevices.remove(pending) },
                        )
                        if (submitted && pendingDevices.isEmpty()) {
                            Text(
                                text = stringResource(R.string.rooms_no_devices_selected),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else {
                        AddDevicesReview(
                            roomName = roomName,
                            pendingDevices = pendingDevices,
                        )
                    }
                }
                AddDevicesFooter(
                    step = step,
                    hasDevices = pendingDevices.isNotEmpty(),
                    onBack = { step = 1 },
                    onDismiss = onDismiss,
                    onNext = {
                        submitted = true
                        if (pendingDevices.isNotEmpty()) {
                            step = 2
                        }
                    },
                    onConfirm = { onConfirm(pendingDevices.toList()) },
                )
            }
        }
    }
}

@Composable
fun EditDeviceDialog(
    device: Device,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable(device.id) { mutableStateOf(device.name) }
    var submitted by rememberSaveable(device.id) { mutableStateOf(false) }
    val showNameError = submitted && name.isBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Column {
                ModalHeader(
                    icon = deviceTypeIcon(device.type),
                    title = stringResource(R.string.rooms_dialog_edit_device_title),
                    subtitle = stringResource(R.string.rooms_dialog_edit_device_subtitle),
                    onDismiss = onDismiss,
                )
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.rooms_field_name)) },
                        placeholder = { Text(stringResource(R.string.rooms_device_name_placeholder)) },
                        singleLine = true,
                        isError = showNameError,
                        supportingText = {
                            if (showNameError) {
                                Text(text = stringResource(R.string.rooms_error_required_name))
                            }
                        },
                    )
                }
                DeviceDialogFooter(
                    onDismiss = onDismiss,
                    confirmLabel = stringResource(R.string.rooms_save_changes),
                    onConfirm = {
                        submitted = true
                        if (name.isNotBlank()) {
                            onConfirm(name)
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun DeleteDeviceDialog(
    deviceName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .widthIn(max = 540.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
        ) {
            Box {
              Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(ErrorBg, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = SmartHomeIcons.Delete,
                        contentDescription = null,
                        tint = ErrorRed,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.rooms_dialog_delete_device_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.rooms_dialog_delete_device_body, deviceName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(text = stringResource(R.string.rooms_cancel), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(text = stringResource(R.string.rooms_delete), fontWeight = FontWeight.Bold)
                    }
                }
              }
              ModalCloseButton(
                  onDismiss = onDismiss,
                  modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
              )
            }
        }
    }
}

@Composable
private fun AddDevicesHeader(
    step: Int,
    roomName: String,
    onDismiss: () -> Unit,
) {
    ModalHeader(
        icon = SmartHomeIcons.Devices,
        eyebrow = stringResource(
            if (step == 1) R.string.rooms_add_devices_step_one_label
            else R.string.rooms_add_devices_step_two_label,
        ),
        title = stringResource(
            if (step == 1) R.string.rooms_add_devices_step_one_title
            else R.string.rooms_add_devices_step_two_title,
        ),
        subtitle = if (step == 1) {
            stringResource(R.string.rooms_add_devices_step_one_subtitle, roomName)
        } else {
            stringResource(R.string.rooms_add_devices_step_two_subtitle)
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun AddDevicesStepper(
    step: Int,
    onDevicesClick: () -> Unit,
    onReviewClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StepPill(
            number = "1",
            label = stringResource(R.string.rooms_step_devices),
            selected = step == 1,
            onClick = onDevicesClick,
            modifier = Modifier.weight(1f),
        )
        StepPill(
            number = "2",
            label = stringResource(R.string.rooms_step_review),
            selected = step == 2,
            onClick = onReviewClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StepPill(
    number: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DeviceTypeGrid(
    availableDeviceTypes: List<DeviceTypeInfo>,
    pendingDevices: List<PendingDevice>,
    onDeviceTypeClick: (DeviceTypeInfo, String) -> Unit,
) {
    BoxWithConstraints {
        val columns = when {
            maxWidth < 380.dp -> 2
            maxWidth < 640.dp -> 3
            else -> 5
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            availableDeviceTypes.chunked(columns).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { type ->
                        val label = deviceTypeLabel(type)
                        DeviceTypeTile(
                            type = type,
                            label = label,
                            count = pendingDevices.count { it.type.id == type.id },
                            onClick = { onDeviceTypeClick(type, label) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - row.size) {
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
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(104.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (count > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (count > 0) 2.dp else 1.dp,
            color = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = deviceTypeIcon(type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingDevicesList(
    pendingDevices: List<PendingDevice>,
    onRemove: (PendingDevice) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.rooms_added_devices),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            if (pendingDevices.isEmpty()) {
                Text(
                    text = stringResource(R.string.rooms_no_devices_selected),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                ) {
                    items(items = pendingDevices, key = { it.id }) { pending ->
                        PendingDeviceRow(
                            pending = pending,
                            onRemove = { onRemove(pending) },
                            showRemove = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingDeviceRow(
    pending: PendingDevice,
    onRemove: () -> Unit = {},
    showRemove: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = deviceTypeIcon(pending.type),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pending.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = deviceTypeLabel(pending.type),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showRemove) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = SmartHomeIcons.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddDevicesReview(
    roomName: String,
    pendingDevices: List<PendingDevice>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReviewSection(
            title = stringResource(R.string.rooms_review_room),
            content = roomName,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.rooms_review_devices),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(10.dp))
                pendingDevices.forEachIndexed { index, pending ->
                    PendingDeviceRow(pending = pending)
                    if (index < pendingDevices.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    content: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddDevicesFooter(
    step: Int,
    hasDevices: Boolean,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onConfirm: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp),
    ) {
        if (maxWidth < 430.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (step == 2) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(text = stringResource(R.string.rooms_back), fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(text = stringResource(R.string.rooms_cancel), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = if (step == 1) onNext else onConfirm,
                    enabled = step == 2 || hasDevices,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandIndigoDark,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            if (step == 1) R.string.rooms_next else R.string.rooms_add_devices_confirm,
                        ),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
            return@BoxWithConstraints
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (step == 2) {
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(text = stringResource(R.string.rooms_back), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(text = stringResource(R.string.rooms_cancel), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = if (step == 1) onNext else onConfirm,
                enabled = step == 2 || hasDevices,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigoDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (step == 1) R.string.rooms_next else R.string.rooms_add_devices_confirm,
                    ),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun DeviceDialogFooter(
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(18.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(text = stringResource(R.string.rooms_cancel), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onConfirm,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandIndigoDark,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text = confirmLabel, fontWeight = FontWeight.Bold)
        }
    }
}
