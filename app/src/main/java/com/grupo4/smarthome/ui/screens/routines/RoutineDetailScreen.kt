package com.grupo4.smarthome.ui.screens.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.RoutineDay
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

data class RoutineActionUi(
    val icon: ImageVector,
    val action: RoutineActionItemUi,
)

data class RoutineDetailUi(
    val id: String = "",
    val name: String,
    val category: RoutineCategory,
    val enabled: Boolean,
    val time: String,
    val daysSummary: String,
    val days: List<Boolean>,
    val actions: List<RoutineActionUi>,
    val favorite: Boolean = false,
)

@Composable
fun RoutineDetailScreen(
    routine: RoutineDetailUi = sampleRoutineDetail,
    onBack: () -> Unit = {},
    onRename: (String) -> Unit = {},
    onDelete: () -> Unit = {},
    onAddAction: () -> Unit = {},
    onSave: (name: String, time24h: String, days: List<Boolean>, enabled: Boolean) -> Unit = { _, _, _, _ -> },
) {
    var enabled by remember { mutableStateOf(routine.enabled) }
    var name by remember { mutableStateOf(routine.name) }
    var time by remember { mutableStateOf(routine.time) }
    var days by remember { mutableStateOf(routine.days) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    val daysSummary = daysSummaryText(days)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        DetailTopBar(
            onBack = onBack,
            onRename = { showRenameDialog = true },
            onDelete = { showDeleteDialog = true },
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                HeroCard(
                    routine = routine,
                    name = name,
                    time = time,
                    daysSummary = daysSummary,
                    enabled = enabled,
                    onEnabledChange = { enabled = it },
                )
            }
            item {
                NavigationRowCard(
                    icon = SmartHomeIcons.Schedule,
                    title = stringResource(R.string.routine_detail_schedule_title),
                    subtitle = time,
                    onClick = { showScheduleDialog = true },
                )
            }
            item {
                DaysCard(
                    days = days,
                    onToggleDay = { index ->
                        days = days.toMutableList().also { it[index] = !it[index] }
                    },
                )
            }
            item {
                ActionsCard(
                    actions = routine.actions,
                    onAddAction = onAddAction,
                    showAddActionButton = true,
                )
            }
        }

        SaveBar(onSave = { onSave(name, parseDisplayTimeTo24h(time), days, enabled) })
    }

    if (showRenameDialog) {
        RenameRoutineDialog(
            currentName = name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                name = newName
                showRenameDialog = false
                onRename(newName)
            },
        )
    }

    if (showDeleteDialog) {
        DeleteRoutineDialog(
            routineName = name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
        )
    }

    if (showScheduleDialog) {
        EditScheduleDialog(
            currentTime = time,
            onDismiss = { showScheduleDialog = false },
            onConfirm = { newTime ->
                time = newTime
                showScheduleDialog = false
            },
        )
    }
}

@Composable
internal fun RenameRoutineDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var draft by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Rename,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.routine_rename_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.routine_rename_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.routine_rename_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(draft.trim()) },
                enabled = draft.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.routine_rename_confirm),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
internal fun DeleteRoutineDialog(
    routineName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.routine_delete_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.routine_delete_message, routineName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(
                    text = stringResource(R.string.routine_delete_confirm),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
internal fun DetailTopBar(
    onBack: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onEditRoutine: (() -> Unit)? = null,
    showTitle: Boolean = true,
    showBackButton: Boolean = true,
    showOverflowMenu: Boolean = true,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 4.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBackButton) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = SmartHomeIcons.Back,
                    contentDescription = stringResource(R.string.routine_detail_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (showTitle) {
            Text(
                text = stringResource(R.string.routine_detail_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        if (onEditRoutine != null) {
            Button(
                onClick = onEditRoutine,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(horizontal = 14.dp),
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.routine_detail_edit),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (showOverflowMenu) Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = SmartHomeIcons.MoreOptions,
                    contentDescription = stringResource(R.string.routine_detail_more_options),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.routine_detail_rename)) },
                    onClick = {
                        menuExpanded = false
                        onRename()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = SmartHomeIcons.Rename,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.routine_detail_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = SmartHomeIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun HeroCard(
    routine: RoutineDetailUi,
    name: String,
    time: String,
    daysSummary: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    editable: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = routine.category.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.routine_detail_automated),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    enabled = editable,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryItem(
                        icon = SmartHomeIcons.Routines,
                        text = pluralStringResource(
                            R.plurals.routine_detail_summary_actions,
                            routine.actions.size,
                            routine.actions.size,
                        ),
                    )
                    SummaryDot()
                    SummaryItem(icon = SmartHomeIcons.Schedule, text = time)
                    SummaryDot()
                    SummaryItem(icon = SmartHomeIcons.Calendar, text = daysSummary)
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SummaryDot() {
    Text(
        text = "•",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
internal fun LeadingIconBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
internal fun NavigationRowCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    editable: Boolean = true,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = editable, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LeadingIconBadge(icon = icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (editable) {
                Icon(
                    imageVector = SmartHomeIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun DaysCard(
    days: List<Boolean>,
    onToggleDay: (Int) -> Unit = {},
    editable: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LeadingIconBadge(icon = SmartHomeIcons.Calendar)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.routine_detail_days_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                DayChipsRow(
                    days = days,
                    editable = editable,
                    onToggleDay = onToggleDay,
                )
            }
        }
    }
}

@Composable
private fun DayChipsRow(
    days: List<Boolean>,
    editable: Boolean,
    onToggleDay: (Int) -> Unit,
) {
    val initials = stringArrayResource(R.array.routines_day_initials)
    val names = stringArrayResource(R.array.routines_day_names)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        days.forEachIndexed { index, active ->
            DayChip(
                label = initials.getOrElse(index) { "" },
                contentDescription = names.getOrElse(index) { "" },
                active = active,
                editable = editable,
                onClick = { onToggleDay(index) },
            )
        }
    }
}

@Composable
private fun DayChip(
    label: String,
    contentDescription: String,
    active: Boolean,
    editable: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val base = Modifier
        .size(36.dp)
        .clip(shape)
        .clickable(enabled = editable, onClick = onClick)
    val styled = if (active) {
        base.background(RoutineDay)
    } else {
        base
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
    }
    Box(modifier = styled, contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun ActionsCard(
    actions: List<RoutineActionUi>,
    onAddAction: () -> Unit,
    showAddActionButton: Boolean = true,
) {
    DetailCard {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LeadingIconBadge(icon = SmartHomeIcons.Routines)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.routine_detail_actions_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.routine_detail_summary_actions,
                            actions.size,
                            actions.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (showAddActionButton) {
                    AddActionIconButton(onClick = onAddAction)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (actions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.routine_detail_no_actions_configured),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    actions.forEachIndexed { index, action ->
                        ActionRow(index = index + 1, action = action)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(index: Int, action: RoutineActionUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = action.action.deviceName.ifBlank {
                            stringResource(R.string.create_routine_device_generic)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = routineActionDescription(
                            action.action.actionName,
                            action.action.params,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = SmartHomeIcons.DragHandle,
                    contentDescription = stringResource(R.string.routine_detail_reorder),
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun AddActionIconButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = SmartHomeIcons.Add,
                contentDescription = stringResource(R.string.routine_detail_add_action),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SaveBar(onSave: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onSave),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = SmartHomeIcons.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.routine_detail_save),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditScheduleDialog(
    currentTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val (initialHour, initialMinute) = remember(currentTime) { parseTime(currentTime) }
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.routine_schedule_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.routine_schedule_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(formatTime(state.hour, state.minute)) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.routine_schedule_confirm),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
internal fun EditDaysDialog(
    currentDays: List<Boolean>,
    onDismiss: () -> Unit,
    onConfirm: (List<Boolean>) -> Unit,
) {
    var selected by remember { mutableStateOf(currentDays) }
    val initials = stringArrayResource(R.array.routines_day_initials)
    val names = stringArrayResource(R.array.routines_day_names)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Calendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.routine_days_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.routine_days_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    selected.forEachIndexed { index, active ->
                        SelectableDayChip(
                            label = initials.getOrElse(index) { "" },
                            contentDescription = names.getOrElse(index) { "" },
                            active = active,
                            onClick = {
                                selected = selected.toMutableList().also { it[index] = !active }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selected) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.routine_days_confirm),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun SelectableDayChip(
    label: String,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val base = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .clickable(onClick = onClick)
    val styled = if (active) {
        base.background(RoutineDay)
    } else {
        base
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    }
    Box(modifier = styled, contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun daysSummaryText(days: List<Boolean>): String {
    val weekdays = listOf(true, true, true, true, true, false, false)
    val weekend = listOf(false, false, false, false, false, true, true)
    val everyday = List(7) { true }
    val initials = stringArrayResource(R.array.routines_day_initials)
    return when {
        days == everyday -> stringResource(R.string.routine_days_everyday)
        days == weekdays -> stringResource(R.string.routine_days_weekdays)
        days == weekend -> stringResource(R.string.routine_days_weekend)
        days.none { it } -> stringResource(R.string.routine_days_empty)
        else -> days.mapIndexedNotNull { index, on ->
            if (on) initials.getOrNull(index) else null
        }.joinToString(", ")
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val isAm = hour < 12
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val suffix = if (isAm) "AM" else "PM"
    return "%02d:%02d %s".format(displayHour, minute, suffix)
}

private fun parseTime(time: String): Pair<Int, Int> = runCatching {
    val parts = time.trim().split(" ")
    val hm = parts[0].split(":")
    var hour = hm[0].toInt()
    val minute = hm[1].toInt()
    when (parts.getOrNull(1)?.uppercase()) {
        "PM" -> if (hour != 12) hour += 12
        "AM" -> if (hour == 12) hour = 0
    }
    hour to minute
}.getOrDefault(7 to 0)

internal val sampleRoutineDetail = RoutineDetailUi(
    id = "sample",
    name = "Buenos días",
    category = RoutineCategory.MORNING,
    enabled = true,
    time = "07:00 AM",
    daysSummary = "Lun a Vie",
    days = listOf(true, true, true, true, true, false, false),
    actions = listOf(
        RoutineActionUi(
            icon = SmartHomeIcons.Speaker,
            action = RoutineActionItemUi(
                actionName = "play",
                deviceName = "Parlante",
                params = listOf("Jazz"),
            ),
        ),
        RoutineActionUi(
            icon = SmartHomeIcons.Lightbulb,
            action = RoutineActionItemUi(
                actionName = "turnOn",
                deviceName = "Luces cocina",
                params = listOf("80%"),
            ),
        ),
        RoutineActionUi(
            icon = SmartHomeIcons.Thermostat,
            action = RoutineActionItemUi(
                actionName = "setTemperature",
                deviceName = "Termostato",
                params = listOf("22°"),
            ),
        ),
    ),
)

internal fun RoutineUi.toDetailUi(): RoutineDetailUi {
    val detailActions = actions.map { action ->
        RoutineActionUi(
            icon = SmartHomeIcons.Routines,
            action = action,
        )
    }

    return RoutineDetailUi(
        id = id,
        name = name,
        category = category,
        enabled = true,
        time = time,
        daysSummary = "",
        days = days,
        actions = detailActions,
        favorite = favorite,
    )
}

