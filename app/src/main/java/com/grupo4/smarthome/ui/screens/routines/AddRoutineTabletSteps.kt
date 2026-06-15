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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
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
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
internal fun AddRoutineTabletSelectDeviceStep(
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

    AddRoutineTabletSubstepLabel(stringResource(R.string.create_routine_device_step_label))
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
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
    Text(
        text = stringResource(R.string.create_routine_devices_title),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    when {
        devices.isEmpty() -> AddRoutineTabletEmptyDeviceMessage(R.string.create_routine_devices_empty)
        visibleDevices.isEmpty() -> AddRoutineTabletEmptyDeviceMessage(R.string.create_routine_devices_no_results)
    }
    val rows = visibleDevices.chunked(3)
    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { device ->
                AddRoutineTabletDeviceTile(
                    name = device.name,
                    roomName = deviceRoomNames[device.id].orEmpty(),
                    icon = deviceTypeIcon(device.type),
                    selected = selectedDeviceId == device.id,
                    onClick = { onSelected(device) },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(3 - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AddRoutineTabletEmptyDeviceMessage(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AddRoutineTabletDeviceTile(
    name: String,
    roomName: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (roomName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = roomName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (selected) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = SmartHomeIcons.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddRoutineTabletSelectActionStep(
    deviceName: String,
    actions: List<DeviceActionInfo>,
    onSelected: (DeviceActionInfo) -> Unit,
) {
    AddRoutineTabletSubstepLabel(stringResource(R.string.create_routine_action_step_label))
    Text(
        text = stringResource(R.string.create_routine_action_question, deviceName),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    actions.forEach { action ->
        AddRoutineTabletActionRow(
            label = routineActionLabel(action.name),
            selected = false,
            onClick = { onSelected(action) },
        )
    }
}

@Composable
private fun AddRoutineTabletActionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (selected) 1.5.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
internal fun AddRoutineTabletConfigureParameterStep(
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
    AddRoutineTabletSubstepLabel(stringResource(R.string.create_routine_parameters_step_label))
    Text(
        text = stringResource(R.string.create_routine_parameter_prompt, actionName),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
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

internal val tabletRoutineNumericTypes = setOf("integer", "int", "number", "float", "double")

internal fun tabletRoutineNumericParam(action: DeviceActionInfo): DeviceActionParamInfo? =
    action.params.firstOrNull { param ->
        param.type.lowercase() in tabletRoutineNumericTypes && param.min != null && param.max != null
    }

internal fun tabletRoutineTextParamLabel(device: Device?, action: DeviceActionInfo): String? {
    if (tabletRoutineNumericParam(action) != null) return null
    val declaredParam = action.params.firstOrNull { it.type.lowercase() !in tabletRoutineNumericTypes }
    val isAlarmAction = device?.type?.canonicalName == DeviceTypeNames.ALARM
    return when {
        declaredParam != null -> declaredParam.name.ifBlank { "value" }
        isAlarmAction -> "code"
        else -> null
    }
}

@Composable
private fun AddRoutineTabletSubstepLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun AddRoutineTabletFooter(
    currentStep: AddRoutineTabletStep,
    canCreate: Boolean,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onNext: () -> Unit,
    onConfirmParameter: () -> Unit,
    canConfirmParameter: Boolean,
    isEditing: Boolean,
    onCreate: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (currentStep != AddRoutineTabletStep.Details) {
                AddRoutineTabletTextButton(
                    text = stringResource(R.string.create_routine_back),
                    onClick = onBack,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            AddRoutineTabletSecondaryButton(
                text = stringResource(R.string.action_cancel),
                onClick = onCancel,
            )
            Spacer(modifier = Modifier.width(12.dp))
            when (currentStep) {
                AddRoutineTabletStep.Details -> AddRoutineTabletPrimaryButton(
                    text = stringResource(R.string.create_routine_next),
                    onClick = onNext,
                )

                AddRoutineTabletStep.ActionsSummary -> AddRoutineTabletPrimaryButton(
                    text = stringResource(
                        if (isEditing) R.string.routine_detail_save
                        else R.string.create_routine_create,
                    ),
                    icon = if (isEditing) SmartHomeIcons.Check else SmartHomeIcons.Add,
                    onClick = onCreate,
                    enabled = canCreate,
                )

                AddRoutineTabletStep.ConfigureParameter -> AddRoutineTabletPrimaryButton(
                    text = stringResource(R.string.create_routine_add_action_cta),
                    icon = SmartHomeIcons.Add,
                    onClick = onConfirmParameter,
                    enabled = canConfirmParameter,
                )

                else -> Unit
            }
        }
    }
}

@Composable
private fun AddRoutineTabletPrimaryButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun AddRoutineTabletSecondaryButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun AddRoutineTabletTextButton(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = SmartHomeIcons.Back,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

