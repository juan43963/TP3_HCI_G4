package com.grupo4.smarthome.ui.screens.rooms.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.ui.components.ModalCloseButton
import com.grupo4.smarthome.ui.components.ModalHeader
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.ErrorBg
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun EditDeviceDialog(
    device: Device,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable(device.id) { mutableStateOf(device.name) }
    var submitted by rememberSaveable(device.id) { mutableStateOf(false) }
    val nameErrorRes = when {
        name.isBlank() -> R.string.rooms_error_required_name
        !isDeviceNameValid(name) -> R.string.rooms_error_name_too_short
        else -> null
    }
    val showNameError = submitted && nameErrorRes != null

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
                                if (showNameError && nameErrorRes != null) {
                                    Text(text = stringResource(nameErrorRes))
                                }
                            },
                        )
                    }
                    DeviceDialogFooter(
                        onDismiss = onDismiss,
                        confirmLabel = stringResource(R.string.rooms_save_changes),
                        onConfirm = {
                            submitted = true
                            if (isDeviceNameValid(name)) {
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
