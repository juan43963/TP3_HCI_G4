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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass
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
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.components.ModalHeader
import com.grupo4.smarthome.ui.screens.rooms.PendingDevice
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

private const val PENDING_FIELD_SEP = ""

internal const val MIN_DEVICE_NAME_LENGTH = 3

internal fun isDeviceNameValid(name: String): Boolean = name.trim().length >= MIN_DEVICE_NAME_LENGTH

internal val PendingDevicesSaver = listSaver<SnapshotStateList<PendingDevice>, String>(
    save = { list ->
        list.map {
            listOf(
                it.id,
                it.type.id,
                it.type.name,
                it.name,
                it.qrId ?: "",
            ).joinToString(PENDING_FIELD_SEP)
        }
    },
    restore = { saved ->
        saved.map { row ->
            val parts = row.split(PENDING_FIELD_SEP)
            PendingDevice(
                id = parts[0],
                type = DeviceTypeInfo(
                    id = parts.getOrElse(1) { "" },
                    name = parts.getOrElse(2) { parts.getOrElse(1) { "" } },
                ),
                name = parts.getOrElse(3) { "" },
                qrId = parts.getOrNull(4)?.ifEmpty { null },
            )
        }.toMutableStateList()
    },
)

@Composable
fun AddDevicesToRoomDialog(
    rooms: List<Room>,
    initialRoomId: String,
    availableDeviceTypes: List<DeviceTypeInfo>,
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

    val onToggleType: (DeviceTypeInfo, String) -> Unit = { type, defaultName ->
        val index = devices.indexOfFirst { it.type.id == type.id }
        if (index >= 0) {
            devices.removeAt(index)
        } else {
            devices.add(PendingDevice(id = type.id, type = type, name = defaultName))
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
                        canProceed = step == 1 || devices.all { isDeviceNameValid(it.name) },
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

