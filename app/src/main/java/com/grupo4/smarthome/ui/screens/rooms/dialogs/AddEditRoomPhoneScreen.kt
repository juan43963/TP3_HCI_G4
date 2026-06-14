package com.grupo4.smarthome.ui.screens.rooms.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun AddEditRoomPhoneScreen(
    room: Room?,
    onDismiss: () -> Unit,
    onConfirm: (String, RoomType) -> Unit,
) {
    var name by rememberSaveable(room?.id) { mutableStateOf(room?.name.orEmpty()) }
    var selectedTypeName by rememberSaveable(room?.id) { mutableStateOf(room?.type?.name) }
    var submitted by rememberSaveable(room?.id) { mutableStateOf(false) }
    val selectedType = selectedTypeName?.let(RoomType::valueOf)
    val isEditing = room != null
    val showNameError = submitted && name.isBlank()
    val showTypeError = submitted && selectedType == null

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            RoomPhoneTopBar(
                title = stringResource(
                    if (isEditing) R.string.rooms_dialog_edit_room_title
                    else R.string.rooms_dialog_add_room_title,
                ),
                onBack = onDismiss,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(
                        if (isEditing) R.string.rooms_dialog_edit_room_subtitle
                        else R.string.rooms_dialog_add_room_subtitle,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RoomPhoneFormCard {
                    Text(
                        text = stringResource(R.string.rooms_field_name),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.rooms_room_name_placeholder)) },
                        singleLine = true,
                        isError = showNameError,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            if (showNameError) {
                                Text(text = stringResource(R.string.rooms_error_required_name))
                            }
                        },
                    )
                }
                RoomPhoneFormCard {
                    Text(
                        text = stringResource(R.string.rooms_field_type),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RoomTypeSelector(
                        selectedType = selectedType,
                        onSelected = { selectedTypeName = it.name },
                    )
                    if (showTypeError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.rooms_error_required_type),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Button(
                onClick = {
                    submitted = true
                    val type = selectedType
                    if (name.isNotBlank() && type != null) {
                        onConfirm(name, type)
                    }
                },
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
                        if (isEditing) R.string.rooms_save_changes else R.string.rooms_create_room,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun RoomPhoneTopBar(title: String, onBack: () -> Unit) {
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

@Composable
private fun RoomPhoneFormCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
