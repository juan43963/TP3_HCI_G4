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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
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
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.components.ModalCloseButton
import com.grupo4.smarthome.ui.components.ModalHeader
import com.grupo4.smarthome.ui.screens.rooms.PendingDevice
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeLabel
import com.grupo4.smarthome.ui.screens.rooms.components.roomTypeIcon
import com.grupo4.smarthome.ui.screens.rooms.components.roomTypeLabel
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.ErrorBg
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

private const val PENDING_FIELD_SEP = ""

internal val PendingDevicesSaver = listSaver<SnapshotStateList<PendingDevice>, String>(
    save = { list ->
        list.map { listOf(it.id, it.type.name, it.name, it.qrId ?: "").joinToString(PENDING_FIELD_SEP) }
    },
    restore = { saved ->
        saved.map { row ->
            val parts = row.split(PENDING_FIELD_SEP)
            PendingDevice(
                id = parts[0],
                type = DeviceType.valueOf(parts[1]),
                name = parts.getOrElse(2) { "" },
                qrId = parts.getOrNull(3)?.ifEmpty { null },
            )
        }.toMutableStateList()
    },
)

@Composable
fun AddDevicesToRoomDialog(
    rooms: List<Room>,
    initialRoomId: String,
    availableDeviceTypes: List<DeviceType>,
    onDismiss: () -> Unit,
    onConfirm: (roomId: String, devices: List<PendingDevice>) -> Unit,
    initialStep: Int = 1,
    initialDevices: List<PendingDevice> = emptyList(),
) {
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    if (!isTablet) {
        AddDevicesPhoneScreen(
            rooms = rooms,
            initialRoomId = initialRoomId,
            availableDeviceTypes = availableDeviceTypes,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            initialStep = initialStep,
            initialDevices = initialDevices,
        )
        return
    }

    var step by rememberSaveable { mutableIntStateOf(initialStep.coerceIn(1, 3)) }
    var selectedRoomId by rememberSaveable { mutableStateOf(initialRoomId) }
    var showRoomPicker by rememberSaveable { mutableStateOf(false) }
    val devices = rememberSaveable(saver = PendingDevicesSaver) {
        mutableStateListOf<PendingDevice>().also { it.addAll(initialDevices) }
    }
    val hasDevices = devices.isNotEmpty()
    val selectedRoom = rooms.firstOrNull { it.id == selectedRoomId } ?: rooms.firstOrNull()

    val onToggleType: (DeviceType, String) -> Unit = { type, defaultName ->
        val index = devices.indexOfFirst { it.type == type }
        if (index >= 0) {
            devices.removeAt(index)
        } else {
            devices.add(PendingDevice(id = type.name, type = type, name = defaultName))
        }
    }
    val onRename: (String, String) -> Unit = { id, newName ->
        val index = devices.indexOfFirst { it.id == id }
        if (index >= 0) devices[index] = devices[index].copy(name = newName)
    }
    val onRemove: (String) -> Unit = { id -> devices.removeAll { it.id == id } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val sheetMaxHeight = maxHeight
            val compactHeight = maxHeight < 480.dp
            Surface(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .heightIn(max = sheetMaxHeight),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                if (showRoomPicker) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        RoomPickerTopBar(onBack = { showRoomPicker = false })
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 18.dp),
                        ) {
                            RoomPickerList(
                                rooms = rooms,
                                selectedRoomId = selectedRoomId,
                                onSelect = {
                                    selectedRoomId = it
                                    showRoomPicker = false
                                },
                            )
                        }
                    }
                    return@Surface
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    AddDevicesHeader(
                        step = step,
                        roomName = selectedRoom?.name.orEmpty(),
                        onDismiss = onDismiss,
                        compact = compactHeight,
                    )
                    AddDevicesStepper(
                        step = step,
                        hasDevices = hasDevices,
                        onSelectClick = { step = 1 },
                        onCustomizeClick = { if (hasDevices) step = 2 },
                        onReviewClick = { if (hasDevices) step = 3 },
                        modifier = Modifier.padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = if (compactHeight) 12.dp else 18.dp,
                        ),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(
                                horizontal = 24.dp,
                                vertical = if (compactHeight) 12.dp else 18.dp,
                            ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        when (step) {
                            1 -> SelectDevicesStep(
                                availableDeviceTypes = availableDeviceTypes,
                                devices = devices,
                                onToggleType = onToggleType,
                            )
                            2 -> CustomizeDevicesStep(
                                devices = devices,
                                onRename = onRename,
                                onRemove = onRemove,
                            )
                            else -> ReviewDevicesStep(
                                roomName = selectedRoom?.name.orEmpty(),
                                roomType = selectedRoom?.type ?: RoomType.OTHER,
                                devices = devices,
                                onRemove = onRemove,
                                onEditAll = { step = 2 },
                                onRoomClick = { showRoomPicker = true },
                            )
                        }
                    }
                    AddDevicesFooter(
                        step = step,
                        hasDevices = hasDevices,
                        onBack = { if (step > 1) step -= 1 },
                        onNext = { if (hasDevices && step < 3) step += 1 },
                        onConfirm = { onConfirm(selectedRoomId, devices.toList()) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomPickerTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = SmartHomeIcons.Back,
                contentDescription = stringResource(R.string.rooms_back),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.rooms_select_room_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AddDevicesHeader(
    step: Int,
    roomName: String,
    onDismiss: () -> Unit,
    compact: Boolean = false,
) {
    val titleRes = when (step) {
        1 -> R.string.rooms_add_devices_select_title
        2 -> R.string.rooms_add_devices_customize_title
        else -> R.string.rooms_add_devices_review_title
    }
    val subtitleRes = when (step) {
        1 -> R.string.rooms_add_devices_select_subtitle
        2 -> R.string.rooms_add_devices_customize_subtitle
        else -> R.string.rooms_add_devices_review_subtitle
    }
    ModalHeader(
        icon = SmartHomeIcons.Devices,
        eyebrow = stringResource(R.string.rooms_add_devices_eyebrow, roomName, step),
        title = stringResource(titleRes),
        subtitle = stringResource(subtitleRes),
        onDismiss = onDismiss,
        compact = compact,
    )
}

@Composable
internal fun AddDevicesStepper(
    step: Int,
    hasDevices: Boolean,
    onSelectClick: () -> Unit,
    onCustomizeClick: () -> Unit,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StepPill(
            number = 1,
            label = stringResource(R.string.rooms_step_select),
            state = stepStateOf(1, step),
            enabled = true,
            onClick = onSelectClick,
            modifier = Modifier.weight(1f),
        )
        StepPill(
            number = 2,
            label = stringResource(R.string.rooms_step_customize),
            state = stepStateOf(2, step),
            enabled = hasDevices,
            onClick = onCustomizeClick,
            modifier = Modifier.weight(1f),
        )
        StepPill(
            number = 3,
            label = stringResource(R.string.rooms_step_review),
            state = stepStateOf(3, step),
            enabled = hasDevices,
            onClick = onReviewClick,
            modifier = Modifier.weight(1f),
        )
    }
}

private enum class StepPillState { Completed, Current, Upcoming }

private fun stepStateOf(index: Int, step: Int): StepPillState = when {
    index < step -> StepPillState.Completed
    index == step -> StepPillState.Current
    else -> StepPillState.Upcoming
}

@Composable
private fun StepPill(
    number: Int,
    label: String,
    state: StepPillState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val active = state != StepPillState.Upcoming
    val containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (active) primary else MaterialTheme.colorScheme.outline
    val textColor = if (active) primary else MaterialTheme.colorScheme.onSurfaceVariant
    val badgeColor = if (state == StepPillState.Upcoming) MaterialTheme.colorScheme.outline else primary

    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = BorderStroke(if (state == StepPillState.Current) 2.dp else 1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(badgeColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (state == StepPillState.Completed) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(15.dp),
                    )
                } else {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun SelectDevicesStep(
    availableDeviceTypes: List<DeviceType>,
    devices: List<PendingDevice>,
    onToggleType: (DeviceType, String) -> Unit,
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
                            selected = devices.any { it.type == type },
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
    type: DeviceType,
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
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DeviceIconTile(type = device.type)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceTypeLabel(device.type).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = device.name,
                    onValueChange = onRename,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.rooms_device_name_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
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
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = SmartHomeIcons.Delete,
                    contentDescription = stringResource(R.string.rooms_delete),
                    tint = ErrorRed,
                )
            }
        }
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
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = SmartHomeIcons.Delete,
                    contentDescription = stringResource(R.string.rooms_delete),
                    tint = ErrorRed,
                )
            }
        }
    }
}

@Composable
private fun DeviceIconTile(type: DeviceType) {
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
private fun AddDevicesFooter(
    step: Int,
    hasDevices: Boolean,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .fillMaxWidth(),
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
