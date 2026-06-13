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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionParamInfo
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

// Persiste la lista de días (7 booleans) a través de rotación
private val DaysListSaver = listSaver<List<Boolean>, Boolean>(
    save = { it.toList() },
    restore = { it.toList() },
)

private enum class CreateRoutineStep {
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
    // Horario en formato display ("08:00 AM"); días lunes-primero
    var time by rememberSaveable { mutableStateOf("08:00 AM") }
    var days by rememberSaveable(stateSaver = DaysListSaver) {
        mutableStateOf(listOf(true, true, true, true, true, false, false))
    }
    var draftActions by remember { mutableStateOf(listOf<RoutineAction>()) }
    // Selección transitoria mientras se arma una acción
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    var selectedActionInfo by remember { mutableStateOf<DeviceActionInfo?>(null) }
    var paramValue by remember { mutableStateOf(0f) }

    val currentActions = remember(selectedDevice, deviceActions) {
        selectedDevice?.let { deviceActions[it.type.id] }.orEmpty()
    }

    // Agrega la acción en construcción a la lista y vuelve al resumen
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
                    onClose = onDismiss,
                )
            },
            bottomBar = {
                CreateRoutineBottomBar(
                    currentStep = currentStep,
                    canCreate = canCreate,
                    onBack = goBack,
                    onCancel = onDismiss,
                    onNext = {
                        currentStep = when (currentStep) {
                            CreateRoutineStep.Details -> CreateRoutineStep.ActionsSummary
                            CreateRoutineStep.ActionsSummary -> CreateRoutineStep.ActionsSummary
                            CreateRoutineStep.SelectDevice -> CreateRoutineStep.SelectAction
                            CreateRoutineStep.SelectAction -> CreateRoutineStep.ConfigureParameter
                            CreateRoutineStep.ConfigureParameter -> CreateRoutineStep.ActionsSummary
                        }
                    },
                    onConfirmParameter = { commitAction(listOf(paramValue.toInt().toString())) },
                    onCreate = {
                        if (canCreate) {
                            onCreate(
                                routineName.trim(),
                                selectedCategory.toDomain(),
                                parseDisplayTimeTo24h(time),
                                days,
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
                    )

                    CreateRoutineStep.ActionsSummary -> ActionsSummaryStep(
                        actions = draftActions,
                        onAddAction = { currentStep = CreateRoutineStep.SelectDevice },
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
                            val numericParam = info.params.firstOrNull { it.min != null && it.max != null }
                            if (numericParam == null) {
                                // Acción sin parámetros: se agrega directo
                                commitAction(emptyList())
                            } else {
                                paramValue = (numericParam.min ?: 0).toFloat()
                                currentStep = CreateRoutineStep.ConfigureParameter
                            }
                        },
                    )

                    CreateRoutineStep.ConfigureParameter -> ConfigureParameterStep(
                        actionName = selectedActionInfo?.name.orEmpty().toReadableAction(),
                        param = selectedActionInfo?.params?.firstOrNull { it.min != null && it.max != null },
                        value = paramValue,
                        onValueChange = { paramValue = it },
                    )
                }
            }
        }
    }
}

// Mapea la categoría de la UI de creación al enum de dominio
private fun NewRoutineCategory.toDomain(): RoutineCategory = when (this) {
    NewRoutineCategory.Morning -> RoutineCategory.MORNING
    NewRoutineCategory.Night -> RoutineCategory.NIGHT
    NewRoutineCategory.Comfort -> RoutineCategory.COMFORT
    NewRoutineCategory.Security -> RoutineCategory.SECURITY
    NewRoutineCategory.Custom -> RoutineCategory.CUSTOM
}

@Composable
private fun CreateRoutineHeader(
    currentStep: CreateRoutineStep,
    onBack: () -> Unit,
    onClose: () -> Unit,
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
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = SmartHomeIcons.Back,
                    contentDescription = stringResource(R.string.create_routine_back_description),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = SmartHomeIcons.Close,
                    contentDescription = stringResource(R.string.create_routine_close_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RoutineHeroIcon()
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
                        if (inDetails) R.string.create_routine_details_title
                        else R.string.create_routine_actions_title,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
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
        Spacer(modifier = Modifier.height(14.dp))
        RoutineStepper(currentStep = currentStep)
    }
}

@Composable
private fun RoutineHeroIcon() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SmartHomeIcons.MorningRoutine,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun RoutineStepper(currentStep: CreateRoutineStep) {
    val actionsStarted = currentStep != CreateRoutineStep.Details
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        StepMarker(
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
        StepMarker(
            number = "2",
            label = stringResource(R.string.create_routine_step_actions),
            selected = actionsStarted,
            completed = false,
        )
    }
}

@Composable
private fun StepMarker(
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
private fun DetailsStep(
    routineName: String,
    onRoutineNameChange: (String) -> Unit,
    selectedCategory: NewRoutineCategory,
    onCategorySelected: (NewRoutineCategory) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    days: List<Boolean>,
    onDaysChange: (List<Boolean>) -> Unit,
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
        ScheduleCard(time = time, onClick = { showScheduleDialog = true })
        RepeatCard(days = days, onClick = { showDaysDialog = true })
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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        actions.forEachIndexed { index, action ->
            RoutineActionSummaryCard(position = index + 1, action = action)
        }
        AddActionCard(onClick = onAddAction)
    }
}

@Composable
private fun RoutineActionSummaryCard(position: Int, action: RoutineAction) {
    val params = action.params.joinToString(", ")
    val description = action.actionName.toReadableAction() + if (params.isNotBlank()) " • $params" else ""
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

@Composable
private fun SelectDeviceStep(
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

    StepContentLabel(R.string.create_routine_device_step_label)
    Spacer(modifier = Modifier.height(14.dp))
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
        visibleDevices.forEach { device ->
            DeviceOptionCard(
                name = device.name,
                roomName = deviceRoomNames[device.id].orEmpty(),
                icon = deviceTypeIcon(device.type),
                selected = selectedDeviceId == device.id,
                onClick = { onSelected(device) },
            )
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
private fun SelectActionStep(
    deviceName: String,
    actions: List<DeviceActionInfo>,
    onSelected: (DeviceActionInfo) -> Unit,
) {
    StepContentLabel(R.string.create_routine_action_step_label)
    Spacer(modifier = Modifier.height(18.dp))
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
                label = action.name.toReadableAction(),
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
private fun ConfigureParameterStep(
    actionName: String,
    param: DeviceActionParamInfo?,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val min = (param?.min ?: 0).toFloat()
    val max = (param?.max ?: 100).toFloat()
    StepContentLabel(R.string.create_routine_parameters_step_label)
    Spacer(modifier = Modifier.height(22.dp))
    Text(
        text = stringResource(R.string.create_routine_parameter_prompt, actionName),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(28.dp))
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

@Composable
private fun StepContentLabel(@StringRes labelRes: Int) {
    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CreateRoutineBottomBar(
    currentStep: CreateRoutineStep,
    canCreate: Boolean,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onNext: () -> Unit,
    onConfirmParameter: () -> Unit,
    onCreate: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (currentStep == CreateRoutineStep.Details) {
                SecondaryActionButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                )
                PrimaryActionButton(
                    text = stringResource(R.string.create_routine_next),
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                )
            } else {
                TextButtonLike(
                    text = stringResource(R.string.create_routine_back),
                    onClick = onBack,
                    modifier = Modifier.weight(0.85f),
                )
                SecondaryActionButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                )
                when (currentStep) {
                    CreateRoutineStep.ActionsSummary -> PrimaryActionButton(
                        text = stringResource(R.string.create_routine_create),
                        icon = SmartHomeIcons.Add,
                        onClick = onCreate,
                        enabled = canCreate,
                        modifier = Modifier.weight(1.35f),
                    )

                    CreateRoutineStep.ConfigureParameter -> PrimaryActionButton(
                        text = stringResource(R.string.create_routine_add_action_cta),
                        icon = SmartHomeIcons.Add,
                        onClick = onConfirmParameter,
                        modifier = Modifier.weight(1.5f),
                    )

                    // SelectDevice/SelectAction avanzan al tocar una tarjeta: no necesitan botón primario
                    else -> Spacer(modifier = Modifier.weight(1.35f))
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
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
private fun TextButtonLike(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = SmartHomeIcons.Back,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Preview(name = "Create Routine - Details", showBackground = true, showSystemUi = true)
@Composable
private fun CreateRoutinePreview() {
    SmartHomeTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            CreateRoutineOverlay(
                visible = true,
                isTablet = false,
                onDismiss = {},
            )
        }
    }
}
