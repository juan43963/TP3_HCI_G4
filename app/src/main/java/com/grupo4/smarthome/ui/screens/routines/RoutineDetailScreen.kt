package com.grupo4.smarthome.ui.screens.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardSearchBar
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardTopBar
import com.grupo4.smarthome.ui.screens.dashboard.layout.DashboardScaffold
import com.grupo4.smarthome.ui.screens.dashboard.layout.SidebarDestination
import com.grupo4.smarthome.ui.screens.dashboard.layout.TabletSidebar
import com.grupo4.smarthome.ui.theme.RoutineDay
import com.grupo4.smarthome.ui.theme.RoutinePlay
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme
import com.grupo4.smarthome.ui.theme.StarActive

/** Fila de acción dentro de una rutina. Modelo temporal de interfaz. */
data class RoutineActionUi(
    val icon: ImageVector,
    val action: RoutineActionItemUi,
)

/** Modelo temporal para la pantalla de detalle. */
data class RoutineDetailUi(
    val id: String = "",
    val name: String,
    val category: RoutineCategory,
    val enabled: Boolean,
    val time: String,
    val daysSummary: String,
    /** 7 valores, empezando por lunes (L M M J V S D). */
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
    var showDaysDialog by remember { mutableStateOf(false) }

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
            onAddAction = onAddAction,
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
                    onClick = { showDaysDialog = true },
                )
            }
            item {
                ActionsCard(
                    actions = routine.actions,
                    onAddAction = onAddAction,
                    showAddActionButton = false,
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

    if (showDaysDialog) {
        EditDaysDialog(
            currentDays = days,
            onDismiss = { showDaysDialog = false },
            onConfirm = { newDays ->
                days = newDays
                showDaysDialog = false
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
    onAddAction: (() -> Unit)? = null,
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
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                if (onAddAction != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.routine_detail_add_action)) },
                        onClick = {
                            menuExpanded = false
                            onAddAction()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = SmartHomeIcons.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                    )
                }
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
                        text = stringResource(
                            R.string.routine_detail_summary_actions,
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
private fun LeadingIconBadge(icon: ImageVector) {
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
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
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
            Icon(
                imageVector = SmartHomeIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun DaysCard(days: List<Boolean>, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
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
                DayChipsRow(days = days)
            }
            Icon(
                imageVector = SmartHomeIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DayChipsRow(days: List<Boolean>) {
    val initials = stringArrayResource(R.array.routines_day_initials)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        days.forEachIndexed { index, active ->
            DayChip(label = initials.getOrElse(index) { "" }, active = active)
        }
    }
}

@Composable
private fun DayChip(label: String, active: Boolean) {
    val base = Modifier
        .size(34.dp)
        .clip(CircleShape)
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
                Column {
                    Text(
                        text = stringResource(R.string.routine_detail_actions_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            R.string.routine_detail_summary_actions,
                            actions.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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

            if (showAddActionButton) {
                Spacer(modifier = Modifier.height(12.dp))
                AddActionButton(onClick = onAddAction)
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
private fun AddActionButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = SmartHomeIcons.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.routine_detail_add_action),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
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

private val sampleRoutineDetail = RoutineDetailUi(
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

@Composable
fun TabletRoutinesDetailLayout(
    onBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAddRoutine: () -> Unit = {},
    onDelete: () -> Unit = {},
    initialRoutineId: String? = null,
    masterRoutines: List<RoutineUi> = sampleRoutines,
    routine: RoutineDetailUi = sampleRoutineDetail,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDaysDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<RoutineCategory?>(null) }
    var selectedRoutineId by remember(initialRoutineId, masterRoutines) {
        mutableStateOf(initialRoutineId ?: masterRoutines.firstOrNull()?.id)
    }
    val visibleMasterRoutines = remember(selectedCategory, masterRoutines) {
        if (selectedCategory == null) masterRoutines
        else masterRoutines.filter { it.category == selectedCategory }
    }

    LaunchedEffect(visibleMasterRoutines, selectedRoutineId) {
        if (selectedRoutineId == null || visibleMasterRoutines.none { it.id == selectedRoutineId }) {
            selectedRoutineId = visibleMasterRoutines.firstOrNull()?.id
        }
    }

    val selectedRoutine = visibleMasterRoutines.firstOrNull { it.id == selectedRoutineId }
        ?: masterRoutines.firstOrNull { it.id == selectedRoutineId }
        ?: masterRoutines.firstOrNull()
    val selectedDetail = selectedRoutine?.toDetailUi() ?: routine

    var enabled by remember(selectedRoutine?.id) { mutableStateOf(selectedDetail.enabled) }
    var name by remember(selectedRoutine?.id) { mutableStateOf(selectedDetail.name) }
    var time by remember(selectedRoutine?.id) { mutableStateOf(selectedDetail.time) }
    var days by remember(selectedRoutine?.id) { mutableStateOf(selectedDetail.days) }

    val daysSummary = daysSummaryText(days)

    val callbacks = DashboardCallbacks(
        onProfileClick = onProfileClick,
        onNavigateHome = onNavigateHome,
        onNavigateRooms = onNavigateRooms,
        onNavigateRoutines = {},
        onQuickAddClick = onAddRoutine,
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxWidth < 720.dp
        val contentPadding = if (compact) 16.dp else 28.dp

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            RoutinesTabletNavigationRail(callbacks = callbacks)
            Column(modifier = Modifier.fillMaxSize()) {
                RoutinesTabletTopBar(
                    onSearchClick = onSearchClick,
                    onHelpClick = {},
                    onProfileClick = onProfileClick,
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    MasterListPane(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight(),
                        routines = masterRoutines,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        selectedRoutineId = selectedRoutineId,
                        onRoutineSelected = { selectedRoutineId = it },
                        contentPadding = contentPadding,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                    Column(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight(),
                    ) {
                        DetailTopBar(
                            onBack = onBack,
                            onRename = { showRenameDialog = true },
                            onDelete = { showDeleteDialog = true },
                            showTitle = false,
                        )
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = contentPadding,
                                end = contentPadding,
                                top = 8.dp,
                                bottom = 20.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            item {
                                HeroCard(
                                    routine = selectedDetail,
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
                                    onClick = { showDaysDialog = true },
                                )
                            }
                            item {
                                ActionsCard(
                                    actions = selectedDetail.actions,
                                    onAddAction = onAddRoutine,
                                )
                            }
                        }
                        DetailActionBar(
                            onDelete = { showDeleteDialog = true },
                            onSave = {},
                            contentPadding = contentPadding,
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameRoutineDialog(
            currentName = name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                name = newName
                showRenameDialog = false
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
    if (showDaysDialog) {
        EditDaysDialog(
            currentDays = days,
            onDismiss = { showDaysDialog = false },
            onConfirm = { newDays ->
                days = newDays
                showDaysDialog = false
            },
        )
    }
}

@Composable
private fun RoutinesTabletNavigationRail(callbacks: DashboardCallbacks) {
    Surface(
        modifier = Modifier
            .width(78.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "SH",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                RoutinesTabletRailItem(
                    icon = SmartHomeIcons.Home,
                    selected = false,
                    onClick = callbacks.onNavigateHome,
                )
                RoutinesTabletRailItem(
                    icon = SmartHomeIcons.Rooms,
                    selected = false,
                    onClick = callbacks.onNavigateRooms,
                )
                RoutinesTabletRailItem(
                    icon = SmartHomeIcons.Routines,
                    selected = true,
                    onClick = callbacks.onNavigateRoutines,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            RoutinesTabletRailItem(
                icon = SmartHomeIcons.Settings,
                selected = false,
                onClick = callbacks.onProfileClick,
            )
        }
    }
}

@Composable
private fun RoutinesTabletRailItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun RoutinesTabletTopBar(
    onSearchClick: () -> Unit,
    onHelpClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = SmartHomeIcons.Search,
                    contentDescription = stringResource(R.string.dashboard_search_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = SmartHomeIcons.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "J",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutinesSearchBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = stringResource(R.string.routines_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MasterListPane(
    modifier: Modifier,
    routines: List<RoutineUi>,
    selectedCategory: RoutineCategory?,
    onCategorySelected: (RoutineCategory?) -> Unit,
    selectedRoutineId: String?,
    onRoutineSelected: (String) -> Unit,
    contentPadding: Dp,
) {
    val visible = if (selectedCategory == null) routines
    else routines.filter { it.category == selectedCategory }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = contentPadding,
            end = contentPadding,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.routines_header_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.routines_header_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            RoutinesSearchBar(modifier = Modifier.fillMaxWidth())
        }
        item {
            CategoryFilter(
                routines = routines,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
            )
        }
        items(items = visible, key = { it.id }) { item ->
            MasterRoutineCard(
                routine = item,
                selected = item.id == selectedRoutineId,
                onClick = { onRoutineSelected(item.id) },
            )
        }
    }
}

@Composable
internal fun MasterRoutineCard(
    routine: RoutineUi,
    selected: Boolean,
    onClick: () -> Unit,
    onExecute: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
) {
    val border = if (selected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = border,
        shadowElevation = if (selected) 0.dp else 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LeadingIconBadge(icon = routine.category.icon)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MasterCategoryPill(category = routine.category)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (routine.favorite) SmartHomeIcons.Favorite
                        else SmartHomeIcons.FavoriteBorder,
                        contentDescription = null,
                        tint = if (routine.favorite) StarActive
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = SmartHomeIcons.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = routine.time,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = routine.actions.firstOrNull()?.let { action ->
                        routineActionCardLabel(action)
                    } ?: stringResource(R.string.routine_detail_no_actions_configured),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MasterDayLabels(days = routine.days, modifier = Modifier.weight(1f))
                MasterPlayButton(onClick = onExecute)
            }
        }
    }
}

@Composable
private fun MasterCategoryPill(category: RoutineCategory) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = stringResource(category.labelRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun MasterDayLabels(days: List<Boolean>, modifier: Modifier = Modifier) {
    val names = stringArrayResource(R.array.routines_day_names)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEachIndexed { index, active ->
            Text(
                text = names.getOrElse(index) { "" }.take(3),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) RoutineDay else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MasterPlayButton(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(RoutinePlay),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SmartHomeIcons.Play,
            contentDescription = stringResource(R.string.routines_play_content_description),
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
internal fun DetailActionBar(
    onDelete: () -> Unit,
    onSave: () -> Unit,
    contentPadding: Dp,
    showDelete: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = contentPadding, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showDelete) {
                Surface(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onDelete),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = SmartHomeIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.routine_detail_delete),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
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

@Preview(name = "Detalle tablet horizontal", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun TabletRoutinesDetailPreview() {
    SmartHomeTheme(darkTheme = false) {
        TabletRoutinesDetailLayout()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RoutineDetailScreenLightPreview() {
    SmartHomeTheme(darkTheme = false) {
        RoutineDetailScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RoutineDetailScreenDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        RoutineDetailScreen()
    }
}
