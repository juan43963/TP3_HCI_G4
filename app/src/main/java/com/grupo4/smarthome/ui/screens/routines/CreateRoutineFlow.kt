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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Thermostat
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
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

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

private enum class ActivationMode(@StringRes val labelRes: Int, val icon: ImageVector) {
    Scheduled(R.string.create_routine_activation_schedule, SmartHomeIcons.Schedule),
    Manual(R.string.create_routine_activation_manual, SmartHomeIcons.ManualTrigger),
}

private data class RoutineDeviceOption(
    val nameRes: Int,
    val roomRes: Int,
    val icon: ImageVector,
)

private data class RoutineActionOption(
    val labelRes: Int,
    val icon: ImageVector,
)

@Composable
fun BoxScope.CreateRoutineOverlay(
    visible: Boolean,
    isTablet: Boolean,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible && !isTablet,
        enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(tween(220)),
        exit = slideOutVertically(animationSpec = tween(250)) { it } + fadeOut(tween(180)),
        modifier = Modifier.matchParentSize(),
    ) {
        CreateRoutineScreen(onDismiss = onDismiss)
    }
}

@Composable
private fun CreateRoutineScreen(onDismiss: () -> Unit) {
    var currentStep by rememberSaveable { mutableStateOf(CreateRoutineStep.Details) }
    var routineName by rememberSaveable { mutableStateOf("") }
    var routineDescription by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(NewRoutineCategory.Morning) }
    var activationMode by rememberSaveable { mutableStateOf(ActivationMode.Scheduled) }
    var selectedDevice by rememberSaveable { mutableStateOf(0) }
    var selectedAction by rememberSaveable { mutableStateOf(1) }
    var temperature by rememberSaveable { mutableStateOf(4f) }
    var actionAdded by rememberSaveable { mutableStateOf(true) }

    val goBack: () -> Unit = {
        currentStep = when (currentStep) {
            CreateRoutineStep.Details -> CreateRoutineStep.Details
            CreateRoutineStep.ActionsSummary -> CreateRoutineStep.Details
            CreateRoutineStep.SelectDevice -> CreateRoutineStep.ActionsSummary
            CreateRoutineStep.SelectAction -> CreateRoutineStep.SelectDevice
            CreateRoutineStep.ConfigureParameter -> CreateRoutineStep.SelectAction
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
                    onBack = if (currentStep == CreateRoutineStep.Details) onDismiss else goBack,
                    onClose = onDismiss,
                )
            },
            bottomBar = {
                CreateRoutineBottomBar(
                    currentStep = currentStep,
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
                        if (currentStep == CreateRoutineStep.ActionsSummary) {
                            actionAdded = true
                        }
                    },
                    onCreate = onDismiss,
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
                        routineDescription = routineDescription,
                        onRoutineDescriptionChange = { routineDescription = it },
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        activationMode = activationMode,
                        onActivationModeSelected = { activationMode = it },
                    )

                    CreateRoutineStep.ActionsSummary -> ActionsSummaryStep(
                        actionAdded = actionAdded,
                        onAddAction = { currentStep = CreateRoutineStep.SelectDevice },
                    )

                    CreateRoutineStep.SelectDevice -> SelectDeviceStep(
                        selectedIndex = selectedDevice,
                        onSelected = { selectedDevice = it },
                    )

                    CreateRoutineStep.SelectAction -> SelectActionStep(
                        selectedDevice = sampleDevices[selectedDevice],
                        onSelected = {
                            selectedAction = it
                            currentStep = CreateRoutineStep.ConfigureParameter
                        },
                    )

                    CreateRoutineStep.ConfigureParameter -> ConfigureParameterStep(
                        action = sampleActions[selectedAction],
                        temperature = temperature,
                        onTemperatureChange = { temperature = it },
                    )
                }
            }
        }
    }
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
    routineDescription: String,
    onRoutineDescriptionChange: (String) -> Unit,
    selectedCategory: NewRoutineCategory,
    onCategorySelected: (NewRoutineCategory) -> Unit,
    activationMode: ActivationMode,
    onActivationModeSelected: (ActivationMode) -> Unit,
) {
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
        CompactTextField(
            label = stringResource(R.string.create_routine_description_label),
            value = routineDescription,
            onValueChange = onRoutineDescriptionChange,
            placeholder = stringResource(R.string.create_routine_description_placeholder),
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
        SegmentedChoices(
            options = ActivationMode.entries,
            selected = activationMode,
            onSelected = onActivationModeSelected,
        )
        ScheduleCard()
        RepeatCard()
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
private fun SegmentedChoices(
    options: List<ActivationMode>,
    selected: ActivationMode,
    onSelected: (ActivationMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            ChoicePill(
                label = stringResource(option.labelRes),
                icon = option.icon,
                selected = selected == option,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(option) },
            )
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
private fun ScheduleCard() {
    InfoCard {
        CardSectionTitle(
            icon = SmartHomeIcons.Schedule,
            text = stringResource(R.string.create_routine_time_label),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.create_routine_time_value),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun RepeatCard() {
    val dayInitials = stringArrayResource(R.array.routines_day_initials)
    InfoCard {
        CardSectionTitle(
            icon = SmartHomeIcons.Calendar,
            text = stringResource(R.string.create_routine_repeat_label),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dayInitials.forEachIndexed { index, day ->
                val selected = index < 5
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
    actionAdded: Boolean,
    onAddAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (actionAdded) {
            RoutineActionSummaryCard()
        }
        AddActionCard(onClick = onAddAction)
    }
}

@Composable
private fun RoutineActionSummaryCard() {
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
                        text = "1",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Kitchen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.create_routine_device_fridge),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.create_routine_summary_temperature),
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
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    StepContentLabel(R.string.create_routine_device_step_label)
    Spacer(modifier = Modifier.height(14.dp))
    SearchField()
    Spacer(modifier = Modifier.height(20.dp))
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sampleDevices.forEachIndexed { index, device ->
            DeviceOptionCard(
                device = device,
                selected = selectedIndex == index,
                onClick = { onSelected(index) },
            )
        }
    }
}

@Composable
private fun SearchField() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.create_routine_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DeviceOptionCard(
    device: RoutineDeviceOption,
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
                        imageVector = device.icon,
                        contentDescription = null,
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(device.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(device.roomRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
    selectedDevice: RoutineDeviceOption,
    onSelected: (Int) -> Unit,
) {
    StepContentLabel(R.string.create_routine_action_step_label)
    Spacer(modifier = Modifier.height(18.dp))
    Text(
        text = stringResource(
            R.string.create_routine_action_question,
            stringResource(selectedDevice.nameRes),
        ),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(18.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sampleActions.forEachIndexed { index, action ->
            ActionOptionCard(
                action = action,
                onClick = { onSelected(index) },
            )
        }
    }
}

@Composable
private fun ActionOptionCard(
    action: RoutineActionOption,
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
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = stringResource(action.labelRes),
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
    action: RoutineActionOption,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
) {
    StepContentLabel(R.string.create_routine_parameters_step_label)
    Spacer(modifier = Modifier.height(22.dp))
    Text(
        text = stringResource(
            R.string.create_routine_parameter_prompt,
            stringResource(action.labelRes),
        ),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(28.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.create_routine_temperature_label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.create_routine_temperature_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(modifier = Modifier.height(22.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Slider(
            value = temperature,
            onValueChange = onTemperatureChange,
            valueRange = 0f..8f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        )
        Text(
            text = temperature.toInt().toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(22.dp),
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
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onNext: () -> Unit,
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
                        modifier = Modifier.weight(1.35f),
                    )

                    CreateRoutineStep.ConfigureParameter -> PrimaryActionButton(
                        text = stringResource(R.string.create_routine_add_action_cta),
                        icon = SmartHomeIcons.Add,
                        onClick = onNext,
                        modifier = Modifier.weight(1.5f),
                    )

                    else -> PrimaryActionButton(
                        text = stringResource(R.string.create_routine_next),
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onNext,
                        modifier = Modifier.weight(1.35f),
                    )
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
) {
    Button(
        onClick = onClick,
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

private val sampleDevices = listOf(
    RoutineDeviceOption(
        nameRes = R.string.create_routine_device_fridge,
        roomRes = R.string.create_routine_device_fridge_room,
        icon = Icons.Outlined.Kitchen,
    ),
    RoutineDeviceOption(
        nameRes = R.string.create_routine_device_living_light,
        roomRes = R.string.create_routine_device_living_room,
        icon = SmartHomeIcons.Lightbulb,
    ),
    RoutineDeviceOption(
        nameRes = R.string.create_routine_device_ac,
        roomRes = R.string.create_routine_device_bedroom,
        icon = Icons.Outlined.Air,
    ),
)

private val sampleActions = listOf(
    RoutineActionOption(
        labelRes = R.string.create_routine_action_freezer_temperature,
        icon = Icons.Default.AcUnit,
    ),
    RoutineActionOption(
        labelRes = R.string.create_routine_action_temperature,
        icon = Icons.Outlined.Thermostat,
    ),
    RoutineActionOption(
        labelRes = R.string.create_routine_action_mode,
        icon = Icons.Default.GridView,
    ),
)

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
