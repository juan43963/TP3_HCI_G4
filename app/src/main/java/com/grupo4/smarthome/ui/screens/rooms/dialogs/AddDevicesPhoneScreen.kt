package com.grupo4.smarthome.ui.screens.rooms.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.screens.rooms.PendingDevice
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun AddDevicesPhoneScreen(
    rooms: List<Room>,
    initialRoomId: String,
    availableDeviceTypes: List<DeviceTypeInfo>,
    onDismiss: () -> Unit,
    onConfirm: (roomId: String, devices: List<PendingDevice>) -> Unit,
    initialStep: Int = 1,
    initialDevices: List<PendingDevice> = emptyList(),
) {
    var step by rememberSaveable { mutableIntStateOf(initialStep.coerceIn(1, 3)) }
    var selectedRoomId by rememberSaveable { mutableStateOf(initialRoomId) }
    var showRoomPicker by rememberSaveable { mutableStateOf(false) }
    val devices = rememberSaveable(saver = PendingDevicesSaver) {
        mutableStateListOf<PendingDevice>().also { it.addAll(initialDevices) }
    }
    val hasDevices = devices.isNotEmpty()
    val selectedRoom = rooms.firstOrNull { it.id == selectedRoomId } ?: rooms.firstOrNull()

    if (showRoomPicker) {
        BackHandler { showRoomPicker = false }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                PhoneWizardTopBar(
                    title = stringResource(R.string.rooms_select_room_title),
                    onBack = { showRoomPicker = false },
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp, bottom = 16.dp),
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
        }
        return
    }

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

    val goBack = { if (step > 1) step -= 1 else onDismiss() }
    BackHandler(onBack = goBack)

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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            PhoneWizardTopBar(title = stringResource(titleRes), onBack = goBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(subtitleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AddDevicesStepper(
                    step = step,
                    hasDevices = hasDevices,
                    onSelectClick = { step = 1 },
                    onCustomizeClick = { if (hasDevices) step = 2 },
                    onReviewClick = { if (hasDevices) step = 3 },
                )
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
            // En selección (paso 1) los nombres son los por defecto; al personalizar/confirmar exigimos
            // que todos sean válidos (la API rechaza nombres muy cortos).
            val allNamesValid = devices.all { isDeviceNameValid(it.name) }
            Button(
                onClick = {
                    if (step < 3) {
                        if (hasDevices) step += 1
                    } else {
                        onConfirm(selectedRoomId, devices.toList())
                    }
                },
                enabled = hasDevices && (step == 1 || allNamesValid),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (step < 3) R.string.rooms_continue else R.string.rooms_add_devices_confirm,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun PhoneWizardTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
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
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
