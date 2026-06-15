package com.grupo4.smarthome.ui.screens.routines

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionParamInfo
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
internal fun SelectDeviceStep(
    devices: List<Device>,
    deviceRoomNames: Map<String, String>,
    selectedDeviceId: String?,
    onSelected: (Device) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val visibleDevices = remember(query, devices, deviceRoomNames) {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) {
            devices
        } else {
            devices.filter { device ->
                device.name.lowercase().contains(normalized) ||
                    deviceRoomNames[device.id].orEmpty().lowercase().contains(normalized)
            }
        }
    }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.create_routine_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            Icon(
                imageVector = SmartHomeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
    Spacer(modifier = Modifier.height(14.dp))
    Text(
        text = stringResource(R.string.create_routine_devices_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = stringResource(R.string.create_routine_devices_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            devices.isEmpty() -> EmptyDeviceMessage(R.string.create_routine_devices_empty)
            visibleDevices.isEmpty() -> EmptyDeviceMessage(R.string.create_routine_devices_no_results)
        }
        val otherRoomLabel = stringResource(R.string.create_routine_devices_other_room)
        val grouped = visibleDevices.groupBy { deviceRoomNames[it.id].orEmpty().ifBlank { otherRoomLabel } }
        grouped.entries.sortedBy { it.key }.forEach { (roomName, roomDevices) ->
            Text(
                text = roomName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            roomDevices.forEach { device ->
                DeviceOptionCard(
                    name = device.name,
                    roomName = "",
                    icon = deviceTypeIcon(device.type),
                    selected = selectedDeviceId == device.id,
                    onClick = { onSelected(device) },
                )
            }
        }
    }
}

@Composable
private fun EmptyDeviceMessage(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun DeviceOptionCard(
    name: String,
    roomName: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (roomName.isNotBlank()) {
                    Text(
                        text = roomName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (selected) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = SmartHomeIcons.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SelectActionStep(
    deviceName: String,
    actions: List<DeviceActionInfo>,
    onSelected: (DeviceActionInfo) -> Unit,
) {
    Text(
        text = stringResource(R.string.create_routine_action_question, deviceName),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(18.dp))
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        actions.forEach { action ->
            ActionOptionCard(
                label = routineActionLabel(action.name),
                onClick = { onSelected(action) },
            )
        }
    }
}

@Composable
private fun ActionOptionCard(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = SmartHomeIcons.Routines,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = SmartHomeIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun ConfigureParameterStep(
    actionName: String,
    param: DeviceActionParamInfo?,
    value: Float,
    onValueChange: (Float) -> Unit,
    textParamLabel: String?,
    textValue: String,
    onTextValueChange: (String) -> Unit,
) {
    val isTextParam = textParamLabel != null
    val min = (param?.min ?: 0).toFloat()
    val max = (param?.max ?: 100).toFloat()
    Text(
        text = stringResource(R.string.create_routine_parameter_prompt, actionName),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(28.dp))
    if (isTextParam) {
        OutlinedTextField(
            value = textValue,
            onValueChange = onTextValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(textParamLabel.orEmpty().replaceFirstChar { it.uppercase() }) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    } else {
        Text(
            text = param?.name.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = min..max,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
internal fun CreateRoutineBottomBar(
    currentStep: CreateRoutineStep,
    canCreate: Boolean,
    onNext: () -> Unit,
    onConfirmParameter: () -> Unit,
    canConfirmParameter: Boolean,
    onCreate: () -> Unit,
) {
    if (currentStep == CreateRoutineStep.SelectDevice || currentStep == CreateRoutineStep.SelectAction) {
        return
    }
    val label: String
    val onClick: () -> Unit
    val enabled: Boolean
    when (currentStep) {
        CreateRoutineStep.Details -> {
            label = stringResource(R.string.create_routine_next)
            onClick = onNext
            enabled = true
        }
        CreateRoutineStep.ConfigureParameter -> {
            label = stringResource(R.string.create_routine_add_action_cta)
            onClick = onConfirmParameter
            enabled = canConfirmParameter
        }
        else -> {
            label = stringResource(R.string.create_routine_create)
            onClick = onCreate
            enabled = canCreate
        }
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                )
            }
        }
    }
}

