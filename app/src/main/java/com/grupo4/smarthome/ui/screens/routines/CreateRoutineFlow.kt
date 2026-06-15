package com.grupo4.smarthome.ui.screens.routines

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionParamInfo
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

private val DaysListSaver = listSaver<List<Boolean>, Boolean>(
    save = { it.toList() },
    restore = { it.toList() },
)

internal enum class CreateRoutineStep {
    Details,
    ActionsSummary,
    SelectDevice,
    SelectAction,
    ConfigureParameter,
}

private enum class NewRoutineCategory(@StringRes val labelRes: Int, val icon: ImageVector) {
    Morning(R.string.create_routine_category_morning, SmartHomeIcons.MorningRoutine),
    Night(R.string.create_routine_category_night, SmartHomeIcons.NightRoutine),
    Comfort(R.string.create_routine_category_comfort, SmartHomeIcons.ComfortRoutine),
    Security(R.string.create_routine_category_security, SmartHomeIcons.SecurityRoutine),
    Custom(R.string.create_routine_category_custom, SmartHomeIcons.CustomRoutine),
}

@Composable
fun BoxScope.CreateRoutineOverlay(
    visible: Boolean,
    isTablet: Boolean,
    onDismiss: () -> Unit,
    devices: List<Device> = emptyList(),
    deviceRoomNames: Map<String, String> = emptyMap(),
    deviceActions: Map<String, List<DeviceActionInfo>> = emptyMap(),
    onCreate: (name: String, category: RoutineCategory, time24h: String, days: List<Boolean>, actions: List<RoutineAction>) -> Unit = { _, _, _, _, _ -> },
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(tween(220)),
        exit = slideOutVertically(animationSpec = tween(250)) { it } + fadeOut(tween(180)),
        modifier = Modifier.matchParentSize(),
    ) {
        CreateRoutineScreen(
            onDismiss = onDismiss,
            devices = devices,
            deviceRoomNames = deviceRoomNames,
            deviceActions = deviceActions,
            onCreate = onCreate,
        )
    }
}

@Composable
fun BoxScope.AddRoutineActionOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    devices: List<Device> = emptyList(),
    deviceRoomNames: Map<String, String> = emptyMap(),
    deviceActions: Map<String, List<DeviceActionInfo>> = emptyMap(),
    onAddAction: (RoutineAction) -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(tween(220)),
        exit = slideOutVertically(animationSpec = tween(250)) { it } + fadeOut(tween(180)),
        modifier = Modifier.matchParentSize(),
    ) {
        AddRoutineActionScreen(
            onDismiss = onDismiss,
            devices = devices,
            deviceRoomNames = deviceRoomNames,
            deviceActions = deviceActions,
            onAddAction = onAddAction,
        )
    }
}

@Composable
private fun AddRoutineActionScreen(
    onDismiss: () -> Unit,
    devices: List<Device>,
    deviceRoomNames: Map<String, String>,
    deviceActions: Map<String, List<DeviceActionInfo>>,
    onAddAction: (RoutineAction) -> Unit,
) {
    var currentStep by rememberSaveable { mutableStateOf(CreateRoutineStep.SelectDevice) }
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
            onAddAction(
                RoutineAction(
                    deviceId = device.id,
                    deviceName = device.name,
                    actionName = info.name,
                    params = params,
                ),
            )
            onDismiss()
        }
    }

    val goBack: () -> Unit = {
        when (currentStep) {
            CreateRoutineStep.SelectDevice -> onDismiss()
            CreateRoutineStep.SelectAction -> currentStep = CreateRoutineStep.SelectDevice
            CreateRoutineStep.ConfigureParameter -> currentStep = CreateRoutineStep.SelectAction
            else -> onDismiss()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CreateRoutineHeader(
                    currentStep = currentStep,
                    onBack = goBack,
                )
            },
            bottomBar = {
                CreateRoutineBottomBar(
                    currentStep = currentStep,
                    canCreate = false,
                    onNext = {},
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
                    onCreate = {},
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                when (currentStep) {
                    CreateRoutineStep.SelectDevice -> SelectDeviceStep(
                        devices = devices,
                        deviceRoomNames = deviceRoomNames,
                        selectedDeviceId = selectedDevice?.id,
                        onSelected = {
                            selectedDevice = it
                            currentStep = CreateRoutineStep.SelectAction
                        },
                    )

                    CreateRoutineStep.SelectAction -> SelectActionStep(
                        deviceName = selectedDevice?.name.orEmpty(),
                        actions = currentActions,
                        onSelected = { info ->
                            selectedActionInfo = info
                            paramTextValue = ""
                            val numericParam = routineNumericParam(info)
                            when {
                                numericParam != null -> {
                                    paramValue = (numericParam.min ?: 0).toFloat()
                                    currentStep = CreateRoutineStep.ConfigureParameter
                                }
                                routineTextParamLabel(selectedDevice, info) != null -> {
                                    currentStep = CreateRoutineStep.ConfigureParameter
                                }
                                else -> commitAction(emptyList())
                            }
                        },
                    )

                    CreateRoutineStep.ConfigureParameter -> ConfigureParameterStep(
                        actionName = routineActionLabel(selectedActionInfo?.name.orEmpty()),
                        param = selectedActionInfo?.let(::routineNumericParam),
                        value = paramValue,
                        onValueChange = { paramValue = it },
                        textParamLabel = selectedActionInfo?.let {
                            routineTextParamLabel(selectedDevice, it)
                        },
                        textValue = paramTextValue,
                        onTextValueChange = { paramTextValue = it },
                    )

                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun CreateRoutineScreen(
    onDismiss: () -> Unit,
    devices: List<Device>,
    deviceRoomNames: Map<String, String>,
    deviceActions: Map<String, List<DeviceActionInfo>>,
    onCreate: (name: String, category: RoutineCategory, time24h: String, days: List<Boolean>, actions: List<RoutineAction>) -> Unit,
) {
    var currentStep by rememberSaveable { mutableStateOf(CreateRoutineStep.Details) }
    var routineName by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(NewRoutineCategory.Morning) }
    var time by rememberSaveable { mutableStateOf("08:00 AM") }
    var days by rememberSaveable(stateSaver = DaysListSaver) {
        mutableStateOf(listOf(true, true, true, true, true, false, false))
    }
    var manual by rememberSaveable { mutableStateOf(false) }
    var draftActions by remember { mutableStateOf(listOf<RoutineAction>()) }
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
        currentStep = CreateRoutineStep.ActionsSummary
    }

    val goBack: () -> Unit = {
        currentStep = when (currentStep) {
            CreateRoutineStep.Details -> CreateRoutineStep.Details
            CreateRoutineStep.ActionsSummary -> CreateRoutineStep.Details
            CreateRoutineStep.SelectDevice -> CreateRoutineStep.ActionsSummary
            CreateRoutineStep.SelectAction -> CreateRoutineStep.SelectDevice
            CreateRoutineStep.ConfigureParameter -> CreateRoutineStep.SelectAction
        }
    }

    val canCreate = routineName.isNotBlank() && draftActions.isNotEmpty()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CreateRoutineHeader(
                    currentStep = currentStep,
                    onBack = if (currentStep == CreateRoutineStep.Details) onDismiss else goBack,
                )
            },
            bottomBar = {
                CreateRoutineBottomBar(
                    currentStep = currentStep,
                    canCreate = canCreate,
                    onNext = {
                        currentStep = when (currentStep) {
                            CreateRoutineStep.Details -> CreateRoutineStep.ActionsSummary
                            CreateRoutineStep.ActionsSummary -> CreateRoutineStep.ActionsSummary
                            CreateRoutineStep.SelectDevice -> CreateRoutineStep.SelectAction
                            CreateRoutineStep.SelectAction -> CreateRoutineStep.ConfigureParameter
                            CreateRoutineStep.ConfigureParameter -> CreateRoutineStep.ActionsSummary
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
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                when (currentStep) {
                    CreateRoutineStep.Details -> DetailsStep(
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

                    CreateRoutineStep.ActionsSummary -> ActionsSummaryStep(
                        actions = draftActions,
                        onAddAction = { currentStep = CreateRoutineStep.SelectDevice },
                        onRemoveAction = { index ->
                            draftActions = draftActions.filterIndexed { actionIndex, _ ->
                                actionIndex != index
                            }
                        },
                    )

                    CreateRoutineStep.SelectDevice -> SelectDeviceStep(
                        devices = devices,
                        deviceRoomNames = deviceRoomNames,
                        selectedDeviceId = selectedDevice?.id,
                        onSelected = {
                            selectedDevice = it
                            currentStep = CreateRoutineStep.SelectAction
                        },
                    )

                    CreateRoutineStep.SelectAction -> SelectActionStep(
                        deviceName = selectedDevice?.name.orEmpty(),
                        actions = currentActions,
                        onSelected = { info ->
                            selectedActionInfo = info
                            paramTextValue = ""
                            val numericParam = routineNumericParam(info)
                            when {
                                numericParam != null -> {
                                    paramValue = (numericParam.min ?: 0).toFloat()
                                    currentStep = CreateRoutineStep.ConfigureParameter
                                }
                                routineTextParamLabel(selectedDevice, info) != null -> {
                                    currentStep = CreateRoutineStep.ConfigureParameter
                                }
                                else -> {
                                    commitAction(emptyList())
                                }
                            }
                        },
                    )

                    CreateRoutineStep.ConfigureParameter -> ConfigureParameterStep(
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
        }
    }
}

private fun NewRoutineCategory.toDomain(): RoutineCategory = when (this) {
    NewRoutineCategory.Morning -> RoutineCategory.MORNING
    NewRoutineCategory.Night -> RoutineCategory.NIGHT
    NewRoutineCategory.Comfort -> RoutineCategory.COMFORT
    NewRoutineCategory.Security -> RoutineCategory.SECURITY
    NewRoutineCategory.Custom -> RoutineCategory.CUSTOM
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
private fun CreateRoutineHeader(
    currentStep: CreateRoutineStep,
    onBack: () -> Unit,
) {
    val inDetails = currentStep == CreateRoutineStep.Details

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                    ),
                ),
            )
            .statusBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = SmartHomeIcons.Back,
                    contentDescription = stringResource(R.string.create_routine_back_description),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(
                    if (inDetails) R.string.create_routine_details_title
                    else R.string.create_routine_actions_title,
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(
                if (inDetails) R.string.create_routine_details_subtitle
                else R.string.create_routine_actions_subtitle,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        val showStepper = currentStep == CreateRoutineStep.Details ||
            currentStep == CreateRoutineStep.ActionsSummary
        if (showStepper) {
            Spacer(modifier = Modifier.height(14.dp))
            RoutineStepper(currentStep = currentStep)
        }
    }
}

@Composable
private fun RoutineStepper(currentStep: CreateRoutineStep) {
    val actionsStarted = currentStep != CreateRoutineStep.Details
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RoutineStepPill(
            number = 1,
            label = stringResource(R.string.create_routine_step_details),
            state = if (actionsStarted) RoutineStepState.Completed else RoutineStepState.Current,
            modifier = Modifier.weight(1f),
        )
        RoutineStepPill(
            number = 2,
            label = stringResource(R.string.create_routine_step_actions),
            state = if (actionsStarted) RoutineStepState.Current else RoutineStepState.Upcoming,
            modifier = Modifier.weight(1f),
        )
    }
}

private enum class RoutineStepState { Completed, Current, Upcoming }

@Composable
private fun RoutineStepPill(
    number: Int,
    label: String,
    state: RoutineStepState,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val active = state != RoutineStepState.Upcoming
    val containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (active) primary else MaterialTheme.colorScheme.outline
    val textColor = if (active) primary else MaterialTheme.colorScheme.onSurfaceVariant
    val badgeColor = if (state == RoutineStepState.Upcoming) MaterialTheme.colorScheme.outline else primary

    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = BorderStroke(if (state == RoutineStepState.Current) 2.dp else 1.dp, borderColor),
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
                if (state == RoutineStepState.Completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
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

@Composable
private fun DetailsStep(
    routineName: String,
    onRoutineNameChange: (String) -> Unit,
    selectedCategory: NewRoutineCategory,
    onCategorySelected: (NewRoutineCategory) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    days: List<Boolean>,
    onDaysChange: (List<Boolean>) -> Unit,
    manual: Boolean,
    onManualChange: (Boolean) -> Unit,
) {
    var showScheduleDialog by rememberSaveable { mutableStateOf(false) }
    var showDaysDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompactTextField(
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
        CategoryGrid(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
        )
        Text(
            text = stringResource(R.string.create_routine_activation_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ActivationSelector(manual = manual, onManualChange = onManualChange)
        if (!manual) {
            ScheduleCard(time = time, onClick = { showScheduleDialog = true })
            RepeatCard(days = days, onClick = { showDaysDialog = true })
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
private fun CompactTextField(
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
            modifier = Modifier.fillMaxWidth(),
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
private fun CategoryGrid(
    selectedCategory: NewRoutineCategory,
    onCategorySelected: (NewRoutineCategory) -> Unit,
) {
    val rows = NewRoutineCategory.entries.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { category ->
                    ChoicePill(
                        label = stringResource(category.labelRes),
                        icon = category.icon,
                        selected = selectedCategory == category,
                        modifier = Modifier.weight(1f),
                        onClick = { onCategorySelected(category) },
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ChoicePill(
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
            modifier = Modifier.padding(horizontal = 10.dp),
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
private fun ActivationSelector(
    manual: Boolean,
    onManualChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChoicePill(
            label = stringResource(R.string.create_routine_activation_schedule),
            icon = SmartHomeIcons.Schedule,
            selected = !manual,
            modifier = Modifier.weight(1f),
            onClick = { onManualChange(false) },
        )
        ChoicePill(
            label = stringResource(R.string.create_routine_activation_manual),
            icon = SmartHomeIcons.ManualTrigger,
            selected = manual,
            modifier = Modifier.weight(1f),
            onClick = { onManualChange(true) },
        )
    }
}

@Composable
private fun ScheduleCard(time: String, onClick: () -> Unit) {
    InfoCard {
        CardSectionTitle(
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
private fun RepeatCard(days: List<Boolean>, onClick: () -> Unit) {
    val dayInitials = stringArrayResource(R.array.routines_day_initials)
    InfoCard {
        CardSectionTitle(
            icon = SmartHomeIcons.Calendar,
            text = stringResource(R.string.create_routine_repeat_label),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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
private fun CardSectionTitle(icon: ImageVector, text: String) {
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
private fun ActionsSummaryStep(
    actions: List<RoutineAction>,
    onAddAction: () -> Unit,
    onRemoveAction: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        actions.forEachIndexed { index, action ->
            RoutineActionSummaryCard(
                position = index + 1,
                action = action,
                onRemove = { onRemoveAction(index) },
            )
        }
        AddActionCard(onClick = onAddAction)
    }
}

@Composable
private fun RoutineActionSummaryCard(
    position: Int,
    action: RoutineAction,
    onRemove: () -> Unit,
) {
    val description = routineActionDescription(action.actionName, action.params)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
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
                        text = position.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.deviceName.ifBlank { stringResource(R.string.create_routine_device_generic) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
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
private fun AddActionCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
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

