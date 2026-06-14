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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.components.ModalCloseButton
import com.grupo4.smarthome.ui.components.ModalHeader
import com.grupo4.smarthome.ui.screens.rooms.components.roomTypeIcon
import com.grupo4.smarthome.ui.screens.rooms.components.roomTypeLabel
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.ErrorBg
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import androidx.compose.ui.unit.dp

@Composable
fun AddEditRoomDialog(
    room: Room?,
    onDismiss: () -> Unit,
    onConfirm: (String, RoomType) -> Unit,
) {
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    if (!isTablet) {
        AddEditRoomPhoneScreen(room = room, onDismiss = onDismiss, onConfirm = onConfirm)
        return
    }

    var name by rememberSaveable(room?.id) { mutableStateOf(room?.name.orEmpty()) }
    var selectedTypeName by rememberSaveable(room?.id) { mutableStateOf(room?.type?.name) }
    var submitted by rememberSaveable(room?.id) { mutableStateOf(false) }
    val selectedType = selectedTypeName?.let(RoomType::valueOf)
    val isEditing = room != null
    val showNameError = submitted && name.isBlank()
    val showTypeError = submitted && selectedType == null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val maxSheetHeight = maxHeight
            val compactHeight = maxHeight < 480.dp
            Surface(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column {
                    ModalHeader(
                        icon = SmartHomeIcons.Rooms,
                        title = stringResource(
                            if (isEditing) R.string.rooms_dialog_edit_room_title
                            else R.string.rooms_dialog_add_room_title,
                        ),
                        subtitle = stringResource(
                            if (isEditing) R.string.rooms_dialog_edit_room_subtitle
                            else R.string.rooms_dialog_add_room_subtitle,
                        ),
                        onDismiss = onDismiss,
                        compact = compactHeight,
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(
                                horizontal = 24.dp,
                                vertical = if (compactHeight) 14.dp else 20.dp,
                            ),
                        verticalArrangement = Arrangement.spacedBy(if (compactHeight) 12.dp else 18.dp),
                    ) {
                        RoomDialogForm(
                            name = name,
                            onNameChange = { name = it },
                            showNameError = showNameError,
                            selectedType = selectedType,
                            onTypeSelected = { selectedTypeName = it.name },
                            showTypeError = showTypeError,
                        )
                    }
                    DialogFooter(
                        onDismiss = onDismiss,
                        confirmLabel = stringResource(
                            if (isEditing) R.string.rooms_save_changes else R.string.rooms_create_room,
                        ),
                        onConfirm = {
                            submitted = true
                            val type = selectedType
                            if (name.isNotBlank() && type != null) {
                                onConfirm(name, type)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun RoomDialogForm(
    name: String,
    onNameChange: (String) -> Unit,
    showNameError: Boolean,
    selectedType: RoomType?,
    onTypeSelected: (RoomType) -> Unit,
    showTypeError: Boolean,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.rooms_field_name)) },
        placeholder = { Text(stringResource(R.string.rooms_room_name_placeholder)) },
        singleLine = true,
        isError = showNameError,
        supportingText = {
            if (showNameError) {
                Text(text = stringResource(R.string.rooms_error_required_name))
            }
        },
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.rooms_field_type),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        RoomTypeSelector(
            selectedType = selectedType,
            onSelected = onTypeSelected,
        )
        if (showTypeError) {
            Text(
                text = stringResource(R.string.rooms_error_required_type),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun DeleteRoomDialog(
    roomName: String,
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
                    text = stringResource(R.string.rooms_dialog_delete_room_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.rooms_dialog_delete_room_body, roomName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.rooms_dialog_delete_room_note),
                    style = MaterialTheme.typography.bodySmall,
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
internal fun RoomTypeSelector(
    selectedType: RoomType?,
    onSelected: (RoomType) -> Unit,
) {
    BoxWithConstraints {
        val columns = if (maxWidth < 380.dp) 2 else 3
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            RoomType.entries.chunked(columns).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { type ->
                        RoomTypeTile(
                            type = type,
                            selected = selectedType == type,
                            onClick = { onSelected(type) },
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
private fun RoomTypeTile(
    type: RoomType,
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
        Box(modifier = Modifier.padding(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = roomTypeIcon(type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = roomTypeLabel(type),
                    style = MaterialTheme.typography.labelLarge,
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
private fun DialogFooter(
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
