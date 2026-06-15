package com.grupo4.smarthome.ui.screens.routines

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionParamInfo
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme


private enum class AddRoutineTabletStep {
    Details,
    ActionsSummary,
    SelectDevice,
    SelectAction,
    ConfigureParameter,
}

private enum class AddRoutineTabletCategory(@StringRes val labelRes: Int, val icon: ImageVector) {
    Morning(R.string.create_routine_category_morning, SmartHomeIcons.MorningRoutine),
    Night(R.string.create_routine_category_night, SmartHomeIcons.NightRoutine),
    Comfort(R.string.create_routine_category_comfort, SmartHomeIcons.ComfortRoutine),
    Security(R.string.create_routine_category_security, SmartHomeIcons.SecurityRoutine),
    Custom(R.string.create_routine_category_custom, SmartHomeIcons.CustomRoutine);

    fun toDomain(): RoutineCategory = when (this) {
        Morning -> RoutineCategory.MORNING
        Night -> RoutineCategory.NIGHT
        Comfort -> RoutineCategory.COMFORT
        Security -> RoutineCategory.SECURITY
        Custom -> RoutineCategory.CUSTOM
    }
}


@Composable
fun BoxScope.AddRoutineTabletOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    devices: List<Device> = emptyList(),
    deviceRoomNames: Map<String, String> = emptyMap(),
    deviceActions: Map<String, List<DeviceActionInfo>> = emptyMap(),
    initialRoutine: Routine? = null,
    onCreate: (name: String, category: RoutineCategory, time24h: String, days: List<Boolean>, actions: List<RoutineAction>) -> Unit = { _, _, _, _, _ -> },
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            AddRoutineTabletDialogContent(
                onDismiss = onDismiss,
                devices = devices,
                deviceRoomNames = deviceRoomNames,
                deviceActions = deviceActions,
                initialRoutine = initialRoutine,
                onCreate = onCreate,
                maxSheetHeight = maxHeight,
            )
        }
    }
}

@Composable
private fun AddRoutineTabletDialogContent(
    onDismiss: () -> Unit,
    devices: List<Device>,
    deviceRoomNames: Map<String, String>,
    deviceActions: Map<String, List<DeviceActionInfo>>,
    initialRoutine: Routine? = null,
    onCreate: (name: String, category: RoutineCategory, time24h: String, days: List<Boolean>, actions: List<RoutineAction>) -> Unit,
    maxSheetHeight: Dp,
) {
    var currentStep by rememberSaveable { mutableStateOf(AddRoutineTabletStep.Details) }
    var routineName by rememberSaveable { mutableStateOf(initialRoutine?.name.orEmpty()) }
    var selectedCategory by rememberSaveable {
        mutableStateOf(initialRoutine?.category?.toTabletCategory() ?: AddRoutineTabletCategory.Morning)
    }
    var time by rememberSaveable {
        mutableStateOf(initialRoutine?.time?.let(::formatRoutineTime) ?: "08:00 AM")
    }
    var days by rememberSaveable(stateSaver = TabletDaysSaver) {
        mutableStateOf(
            initialRoutine?.days ?: listOf(true, true, true, true, true, false, false),
        )
    }
    // Manual: la rutina se ejecuta a mano, sin horario ni días
    var manual by rememberSaveable {
        mutableStateOf(initialRoutine?.days?.none { it } ?: false)
    }
    var draftActions by remember { mutableStateOf(initialRoutine?.actions.orEmpty()) }
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    var selectedActionInfo by remember { mutableStateOf<DeviceActionInfo?>(null) }
    var paramValue by remember { mutableStateOf(0f) }
    var paramTextValue by rememberSaveable { mutableStateOf("") }

    val currentActions = remember(selectedDevice, deviceActions) {
        selectedDevice?.let { deviceActions[it.type.id] }.orEmpty()
    }

    val commitAction: (List<String>) -> Unit = { params ->
        val device = selectedDevice
        val info = selectedActionInfo
        if (device != null && info != null) {
            draftActions = draftActions + RoutineAction(
                deviceId = device.id,
                deviceName = device.name,
                actionName = info.name,
                params = params,
            )
        }
        currentStep = AddRoutineTabletStep.ActionsSummary
    }

    val goBack: () -> Unit = {
        currentStep = when (currentStep) {
            AddRoutineTabletStep.Details -> AddRoutineTabletStep.Details
            AddRoutineTabletStep.ActionsSummary -> AddRoutineTabletStep.Details
            AddRoutineTabletStep.SelectDevice -> AddRoutineTabletStep.ActionsSummary
            AddRoutineTabletStep.SelectAction -> AddRoutineTabletStep.SelectDevice
            AddRoutineTabletStep.ConfigureParameter -> AddRoutineTabletStep.SelectAction
        }
    }

    val canCreate = routineName.isNotBlank() && draftActions.isNotEmpty()
    val compactHeight = maxSheetHeight < 480.dp

    Surface(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .heightIn(max = maxSheetHeight),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 6.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AddRoutineTabletHeader(
                currentStep = currentStep,
                onClose = onDismiss,
                compact = compactHeight,
                isEditing = initialRoutine != null,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 20.dp,
                        vertical = if (compactHeight) 12.dp else 18.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (currentStep) {
                    AddRoutineTabletStep.Details -> AddRoutineTabletDetailsStep(
                        routineName = routineName,
                        onRoutineNameChange = { routineName = it },
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        time = time,
                        onTimeChange = { time = it },
                        days = days,
                        onDaysChange = { days = it },
                        manual = manual,
                        onManualChange = { manual = it },
                    )

                    AddRoutineTabletStep.ActionsSummary -> AddRoutineTabletActionsSummaryStep(
                        actions = draftActions,
                        onAddAction = { currentStep = AddRoutineTabletStep.SelectDevice },
                        onRemoveAction = { index ->
                            draftActions = draftActions.filterIndexed { actionIndex, _ ->
                                actionIndex != index
                            }
                        },
                    )

                    AddRoutineTabletStep.SelectDevice -> AddRoutineTabletSelectDeviceStep(
                        devices = devices,
                        deviceRoomNames = deviceRoomNames,
                        selectedDeviceId = selectedDevice?.id,
                        onSelected = {
                            selectedDevice = it
                            currentStep = AddRoutineTabletStep.SelectAction
                        },
                    )

                    AddRoutineTabletStep.SelectAction -> AddRoutineTabletSelectActionStep(
                        deviceName = selectedDevice?.name.orEmpty(),
                        actions = currentActions,
                        onSelected = { info ->
                            selectedActionInfo = info
                            paramTextValue = ""
                            val numericParam = routineNumericParam(info)
                            when {
                                numericParam != null -> {
                                    paramValue = (numericParam.min ?: 0).toFloat()
                                    currentStep = AddRoutineTabletStep.ConfigureParameter
                                }
                                routineTextParamLabel(selectedDevice, info) != null -> {
                                    currentStep = AddRoutineTabletStep.ConfigureParameter
                                }
                                else -> {
                                    commitAction(emptyList())
                                }
                            }
                        },
                    )

                    AddRoutineTabletStep.ConfigureParameter -> AddRoutineTabletConfigureParameterStep(
                        actionName = routineActionLabel(selectedActionInfo?.name.orEmpty()),
                        param = selectedActionInfo?.let(::routineNumericParam),
                        value = paramValue,
                        onValueChange = { paramValue = it },
                        textParamLabel = selectedActionInfo?.let { routineTextParamLabel(selectedDevice, it) },
                        textValue = paramTextValue,
                        onTextValueChange = { paramTextValue = it },
                    )
                }
            }

            AddRoutineTabletFooter(
                currentStep = currentStep,
                canCreate = canCreate,
                onBack = goBack,
                onCancel = onDismiss,
                onNext = {
                    currentStep = when (currentStep) {
                        AddRoutineTabletStep.Details -> AddRoutineTabletStep.ActionsSummary
                        AddRoutineTabletStep.ActionsSummary -> AddRoutineTabletStep.ActionsSummary
                        AddRoutineTabletStep.SelectDevice -> AddRoutineTabletStep.SelectAction
                        AddRoutineTabletStep.SelectAction -> AddRoutineTabletStep.ConfigureParameter
                        AddRoutineTabletStep.ConfigureParameter -> AddRoutineTabletStep.ActionsSummary
                    }
                },
                onConfirmParameter = {
                    if (selectedActionInfo?.let { routineTextParamLabel(selectedDevice, it) } != null) {
                        commitAction(listOf(paramTextValue.trim()))
                    } else {
                        commitAction(listOf(paramValue.toInt().toString()))
                    }
                },
                canConfirmParameter = selectedActionInfo?.let { info ->
                    val textLabel = routineTextParamLabel(selectedDevice, info)
                    textLabel == null || paramTextValue.isNotBlank()
                } ?: true,
                isEditing = initialRoutine != null,
                onCreate = {
                    if (canCreate) {
                        onCreate(
                            routineName.trim(),
                            selectedCategory.toDomain(),
                            parseDisplayTimeTo24h(time),
                            if (manual) List(7) { false } else days,
                            draftActions,
                        )
                        onDismiss()
                    }
                },
            )
        }
    }
}

// Persiste la lista de días a través de rotación (dialog tablet)
private val TabletDaysSaver = listSaver<List<Boolean>, Boolean>(
    save = { it.toList() },
    restore = { it.toList() },
)

private fun RoutineCategory.toTabletCategory(): AddRoutineTabletCategory = when (this) {
    RoutineCategory.MORNING -> AddRoutineTabletCategory.Morning
    RoutineCategory.NIGHT -> AddRoutineTabletCategory.Night
    RoutineCategory.COMFORT -> AddRoutineTabletCategory.Comfort
    RoutineCategory.SECURITY -> AddRoutineTabletCategory.Security
    RoutineCategory.CUSTOM -> AddRoutineTabletCategory.Custom
}

@Composable
private fun AddRoutineTabletHeader(
    currentStep: AddRoutineTabletStep,
    onClose: () -> Unit,
    compact: Boolean = false,
    isEditing: Boolean = false,
) {
    val inDetails = currentStep == AddRoutineTabletStep.Details

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                    ),
                ),
            )
            .padding(
                start = 20.dp,
                end = 12.dp,
                top = if (compact) 12.dp else 16.dp,
                bottom = if (compact) 12.dp else 16.dp,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AddRoutineTabletHeroIcon(compact = compact)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (inDetails) R.string.create_routine_step_one_label
                        else R.string.create_routine_step_two_label,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            Text(
                text = stringResource(
                    when {
                        isEditing && inDetails -> R.string.routine_detail_edit
                        inDetails -> R.string.create_routine_details_title
                        else -> R.string.create_routine_actions_title
                    },
                ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!compact) {
                    Text(
                        text = stringResource(
                            if (inDetails) R.string.create_routine_details_subtitle
                            else R.string.create_routine_actions_subtitle,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = SmartHomeIcons.Close,
                    contentDescription = stringResource(R.string.create_routine_close_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))
        AddRoutineTabletStepper(currentStep = currentStep)
    }
}

@Composable
private fun AddRoutineTabletHeroIcon(compact: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (compact) 40.dp else 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SmartHomeIcons.MorningRoutine,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun AddRoutineTabletStepper(currentStep: AddRoutineTabletStep) {
    val actionsStarted = currentStep != AddRoutineTabletStep.Details
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        AddRoutineTabletStepMarker(
            number = "1",
            label = stringResource(R.string.create_routine_step_details),
            selected = !actionsStarted,
            completed = actionsStarted,
        )
        Box(
            modifier = Modifier
                .width(104.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        )
        AddRoutineTabletStepMarker(
            number = "2",
            label = stringResource(R.string.create_routine_step_actions),
            selected = actionsStarted,
            completed = false,
        )
    }
}

@Composable
private fun AddRoutineTabletStepMarker(
    number: String,
    label: String,
    selected: Boolean,
    completed: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        selected -> MaterialTheme.colorScheme.primary
                        completed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        else -> MaterialTheme.colorScheme.surface
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (selected || completed) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@Composable
private fun AddRoutineTabletDetailsStep(
    routineName: String,
    onRoutineNameChange: (String) -> Unit,
    selectedCategory: AddRoutineTabletCategory,
    onCategorySelected: (AddRoutineTabletCategory) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    days: List<Boolean>,
    onDaysChange: (List<Boolean>) -> Unit,
    manual: Boolean,
    onManualChange: (Boolean) -> Unit,
) {
    var showScheduleDialog by rememberSaveable { mutableStateOf(false) }
    var showDaysDialog by rememberSaveable { mutableStateOf(false) }

    AddRoutineTabletField(
        label = stringResource(R.string.create_routine_name_label),
        value = routineName,
        onValueChange = onRoutineNameChange,
        placeholder = stringResource(R.string.create_routine_name_placeholder),
    )
    Text(
        text = stringResource(R.string.create_routine_category_label),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    AddRoutineTabletCategoryRow(
        selectedCategory = selectedCategory,
        onCategorySelected = onCategorySelected,
    )
    Text(
        text = stringResource(R.string.create_routine_activation_label),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AddRoutineTabletPill(
            label = stringResource(R.string.create_routine_activation_schedule),
            icon = SmartHomeIcons.Schedule,
            selected = !manual,
            modifier = Modifier.weight(1f),
            onClick = { onManualChange(false) },
        )
        AddRoutineTabletPill(
            label = stringResource(R.string.create_routine_activation_manual),
            icon = SmartHomeIcons.ManualTrigger,
            selected = manual,
            modifier = Modifier.weight(1f),
            onClick = { onManualChange(true) },
        )
    }
    // Manual no usa horario ni repetición
    if (!manual) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AddRoutineTabletScheduleCard(
                time = time,
                onClick = { showScheduleDialog = true },
                modifier = Modifier.weight(1f),
            )
            AddRoutineTabletRepeatCard(
                days = days,
                onClick = { showDaysDialog = true },
                modifier = Modifier.weight(1.4f),
            )
        }
    }

    if (showScheduleDialog) {
        EditScheduleDialog(
            currentTime = time,
            onDismiss = { showScheduleDialog = false },
            onConfirm = {
                onTimeChange(it)
                showScheduleDialog = false
            },
        )
    }
    if (showDaysDialog) {
        EditDaysDialog(
            currentDays = days,
            onDismiss = { showDaysDialog = false },
            onConfirm = {
                onDaysChange(it)
                showDaysDialog = false
            },
        )
    }
}

@Composable
private fun AddRoutineTabletField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            placeholder = {
                Text(
                    text = placeholder,
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
    }
}

@Composable
private fun AddRoutineTabletCategoryRow(
    selectedCategory: AddRoutineTabletCategory,
    onCategorySelected: (AddRoutineTabletCategory) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AddRoutineTabletCategory.entries.forEach { category ->
            AddRoutineTabletPill(
                label = stringResource(category.labelRes),
                icon = category.icon,
                selected = selectedCategory == category,
                modifier = Modifier.weight(1f),
                onClick = { onCategorySelected(category) },
            )
        }
    }
}

@Composable
private fun AddRoutineTabletPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AddRoutineTabletScheduleCard(time: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AddRoutineTabletInfoCard(modifier = modifier) {
        AddRoutineTabletCardTitle(
            icon = SmartHomeIcons.Schedule,
            text = stringResource(R.string.create_routine_time_label),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun AddRoutineTabletRepeatCard(days: List<Boolean>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dayInitials = stringArrayResource(R.array.routines_day_initials)
    AddRoutineTabletInfoCard(modifier = modifier) {
        AddRoutineTabletCardTitle(
            icon = SmartHomeIcons.Calendar,
            text = stringResource(R.string.create_routine_repeat_label),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            dayInitials.forEachIndexed { index, day ->
                val selected = days.getOrElse(index) { false }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(9.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    border = if (selected) null else BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                    ),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRoutineTabletInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun AddRoutineTabletCardTitle(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@Composable
private fun AddRoutineTabletActionsSummaryStep(
    actions: List<RoutineAction>,
    onAddAction: () -> Unit,
    onRemoveAction: (Int) -> Unit,
) {
    actions.forEachIndexed { index, action ->
        AddRoutineTabletActionSummaryRow(
            number = (index + 1).toString(),
            title = action.deviceName.ifBlank { stringResource(R.string.create_routine_device_generic) },
            subtitle = routineActionDescription(action.actionName, action.params),
            onRemove = { onRemoveAction(index) },
        )
    }
    AddRoutineTabletAddActionCard(onClick = onAddAction)
}

@Composable
private fun AddRoutineTabletActionSummaryRow(
    number: String,
    title: String,
    subtitle: String,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
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
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = SmartHomeIcons.Delete,
                    contentDescription = stringResource(R.string.create_routine_remove_action),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun AddRoutineTabletAddActionCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = SmartHomeIcons.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.create_routine_add_action),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.create_routine_add_action_subtitle),
                    style = MaterialTheme.typography.bodySmall,
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
private fun AddRoutineTabletSelectDeviceStep(
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
private fun AddRoutineTabletSelectActionStep(
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
private fun AddRoutineTabletConfigureParameterStep(
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

private val routineNumericTypes = setOf("integer", "int", "number", "float", "double")

private fun routineNumericParam(action: DeviceActionInfo): DeviceActionParamInfo? =
    action.params.firstOrNull { param ->
        param.type.lowercase() in routineNumericTypes && param.min != null && param.max != null
    }

private fun routineTextParamLabel(device: Device?, action: DeviceActionInfo): String? {
    if (routineNumericParam(action) != null) return null
    val declaredParam = action.params.firstOrNull { it.type.lowercase() !in routineNumericTypes }
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
private fun AddRoutineTabletFooter(
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

                // SelectDevice/SelectAction avanzan al tocar una tarjeta
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


@Preview(name = "Add routine tablet", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun AddRoutineTabletPreview() {
    SmartHomeTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AddRoutineTabletDialogContent(
                onDismiss = {},
                devices = emptyList(),
                deviceRoomNames = emptyMap(),
                deviceActions = emptyMap(),
                onCreate = { _, _, _, _, _ -> },
                maxSheetHeight = 600.dp,
            )
        }
    }
}
