package com.grupo4.smarthome.ui.screens.routines

import androidx.annotation.StringRes
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Blinds
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grupo4.smarthome.R
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
    Custom(R.string.create_routine_category_custom, SmartHomeIcons.CustomRoutine),
}

private enum class AddRoutineTabletActivation(@StringRes val labelRes: Int, val icon: ImageVector) {
    Scheduled(R.string.create_routine_activation_schedule, SmartHomeIcons.Schedule),
    Manual(R.string.create_routine_activation_manual, SmartHomeIcons.ManualTrigger),
}

private data class AddRoutineTabletDevice(
    val name: String,
    val room: String,
    val icon: ImageVector,
)


@Composable
fun BoxScope.AddRoutineTabletOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            AddRoutineTabletDialogContent(onDismiss = onDismiss)
        }
    }
}

@Composable
private fun AddRoutineTabletDialogContent(onDismiss: () -> Unit) {
    var currentStep by rememberSaveable { mutableStateOf(AddRoutineTabletStep.Details) }
    var routineName by rememberSaveable { mutableStateOf("") }
    var routineDescription by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(AddRoutineTabletCategory.Morning) }
    var activation by rememberSaveable { mutableStateOf(AddRoutineTabletActivation.Scheduled) }
    var selectedDevice by rememberSaveable { mutableStateOf(2) }
    var actionAdded by rememberSaveable { mutableStateOf(true) }

    val goBack: () -> Unit = {
        currentStep = when (currentStep) {
            AddRoutineTabletStep.Details -> AddRoutineTabletStep.Details
            AddRoutineTabletStep.ActionsSummary -> AddRoutineTabletStep.Details
            AddRoutineTabletStep.SelectDevice -> AddRoutineTabletStep.ActionsSummary
            AddRoutineTabletStep.SelectAction -> AddRoutineTabletStep.SelectDevice
            AddRoutineTabletStep.ConfigureParameter -> AddRoutineTabletStep.SelectAction
        }
    }

    Surface(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 6.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AddRoutineTabletHeader(
                currentStep = currentStep,
                onClose = onDismiss,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (currentStep) {
                    AddRoutineTabletStep.Details -> AddRoutineTabletDetailsStep(
                        routineName = routineName,
                        onRoutineNameChange = { routineName = it },
                        routineDescription = routineDescription,
                        onRoutineDescriptionChange = { routineDescription = it },
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        activation = activation,
                        onActivationSelected = { activation = it },
                    )

                    AddRoutineTabletStep.ActionsSummary -> AddRoutineTabletActionsSummaryStep(
                        actionAdded = actionAdded,
                        onAddAction = { currentStep = AddRoutineTabletStep.SelectDevice },
                    )

                    AddRoutineTabletStep.SelectDevice -> AddRoutineTabletSelectDeviceStep(
                        selectedIndex = selectedDevice,
                        onSelected = { selectedDevice = it },
                    )

                    AddRoutineTabletStep.SelectAction -> AddRoutineTabletSelectActionStep(
                        deviceName = addRoutineTabletDevices[selectedDevice].name,
                        onSelected = { currentStep = AddRoutineTabletStep.ConfigureParameter },
                    )

                    AddRoutineTabletStep.ConfigureParameter -> AddRoutineTabletConfigureParameterStep()
                }
            }

            AddRoutineTabletFooter(
                currentStep = currentStep,
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
                    actionAdded = true
                },
                onCreate = onDismiss,
            )
        }
    }
}


@Composable
private fun AddRoutineTabletHeader(
    currentStep: AddRoutineTabletStep,
    onClose: () -> Unit,
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
            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AddRoutineTabletHeroIcon()
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
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = SmartHomeIcons.Close,
                    contentDescription = stringResource(R.string.create_routine_close_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        AddRoutineTabletStepper(currentStep = currentStep)
    }
}

@Composable
private fun AddRoutineTabletHeroIcon() {
    Box(
        modifier = Modifier
            .size(52.dp)
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
    routineDescription: String,
    onRoutineDescriptionChange: (String) -> Unit,
    selectedCategory: AddRoutineTabletCategory,
    onCategorySelected: (AddRoutineTabletCategory) -> Unit,
    activation: AddRoutineTabletActivation,
    onActivationSelected: (AddRoutineTabletActivation) -> Unit,
) {
    AddRoutineTabletField(
        label = stringResource(R.string.create_routine_name_label),
        value = routineName,
        onValueChange = onRoutineNameChange,
        placeholder = stringResource(R.string.create_routine_name_placeholder),
    )
    AddRoutineTabletField(
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
        AddRoutineTabletActivation.entries.forEach { option ->
            AddRoutineTabletPill(
                label = stringResource(option.labelRes),
                icon = option.icon,
                selected = activation == option,
                modifier = Modifier.weight(1f),
                onClick = { onActivationSelected(option) },
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AddRoutineTabletScheduleCard(modifier = Modifier.weight(1f))
        AddRoutineTabletRepeatCard(modifier = Modifier.weight(1.4f))
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
private fun AddRoutineTabletScheduleCard(modifier: Modifier = Modifier) {
    AddRoutineTabletInfoCard(modifier = modifier) {
        AddRoutineTabletCardTitle(
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
private fun AddRoutineTabletRepeatCard(modifier: Modifier = Modifier) {
    val dayInitials = stringArrayResource(R.array.routines_day_initials)
    AddRoutineTabletInfoCard(modifier = modifier) {
        AddRoutineTabletCardTitle(
            icon = SmartHomeIcons.Calendar,
            text = stringResource(R.string.create_routine_repeat_label),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
    actionAdded: Boolean,
    onAddAction: () -> Unit,
) {
    if (actionAdded) {
        AddRoutineTabletActionSummaryRow(
            number = "1",
            title = stringResource(R.string.create_routine_device_living_light),
            subtitle = "turnOff",
        )
        AddRoutineTabletActionSummaryRow(
            number = "2",
            title = "Parlante",
            subtitle = "play",
        )
    }
    AddRoutineTabletAddActionCard(onClick = onAddAction)
}

@Composable
private fun AddRoutineTabletActionSummaryRow(
    number: String,
    title: String,
    subtitle: String,
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
            Icon(
                imageVector = SmartHomeIcons.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
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
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    AddRoutineTabletSubstepLabel(stringResource(R.string.create_routine_device_step_label))
    AddRoutineTabletSearchField()
    Text(
        text = stringResource(R.string.create_routine_devices_title),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    val rows = addRoutineTabletDevices.chunked(3)
    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            row.forEach { device ->
                val index = addRoutineTabletDevices.indexOf(device)
                AddRoutineTabletDeviceTile(
                    device = device,
                    selected = selectedIndex == index,
                    onClick = { onSelected(index) },
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
private fun AddRoutineTabletSearchField() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
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
private fun AddRoutineTabletDeviceTile(
    device: AddRoutineTabletDevice,
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
                    imageVector = device.icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = device.room,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
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
    onSelected: (Int) -> Unit,
) {
    AddRoutineTabletSubstepLabel(stringResource(R.string.create_routine_action_step_label))
    Text(
        text = stringResource(R.string.create_routine_action_question, deviceName),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    addRoutineTabletActions.forEachIndexed { index, action ->
        AddRoutineTabletActionRow(
            label = action,
            selected = index == 0,
            onClick = { onSelected(index) },
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
private fun AddRoutineTabletConfigureParameterStep() {
    AddRoutineTabletSubstepLabel(stringResource(R.string.create_routine_parameters_step_label))
    Text(
        text = stringResource(R.string.create_routine_parameter_prompt, "play"),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = "Esta acción no requiere parámetros.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
                AddRoutineTabletStep.ActionsSummary -> AddRoutineTabletPrimaryButton(
                    text = stringResource(R.string.create_routine_create),
                    icon = SmartHomeIcons.Add,
                    onClick = onCreate,
                )

                AddRoutineTabletStep.ConfigureParameter -> AddRoutineTabletPrimaryButton(
                    text = stringResource(R.string.create_routine_add_action_cta),
                    icon = SmartHomeIcons.Add,
                    onClick = onNext,
                )

                else -> AddRoutineTabletPrimaryButton(
                    text = stringResource(R.string.create_routine_next),
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onNext,
                )
            }
        }
    }
}

@Composable
private fun AddRoutineTabletPrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
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


private val addRoutineTabletDevices = listOf(
    AddRoutineTabletDevice("Persiana", "Baño principal", Icons.Outlined.Blinds),
    AddRoutineTabletDevice("Lámpara", "Baño principal", SmartHomeIcons.Lightbulb),
    AddRoutineTabletDevice("Lámpara", "Comedor", SmartHomeIcons.Lightbulb),
    AddRoutineTabletDevice("Alarma", "Comedor", Icons.Outlined.NotificationsActive),
    AddRoutineTabletDevice("Aire acond.", "Comedor", Icons.Outlined.Air),
    AddRoutineTabletDevice("Parlante", "Comedor", SmartHomeIcons.Speaker),
    AddRoutineTabletDevice("Heladera", "Cocina", Icons.Outlined.Kitchen),
    AddRoutineTabletDevice("Aire acond.", "Living", Icons.Outlined.Air),
    AddRoutineTabletDevice("Parlante", "Living", SmartHomeIcons.Speaker),
)

private val addRoutineTabletActions = listOf(
    "play",
    "stop",
    "pause",
    "resume",
    "setVolume",
    "nextSong",
    "previousSong",
    "setGenre",
    "getPlaylist",
)


@Preview(name = "Add routine tablet", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun AddRoutineTabletPreview() {
    SmartHomeTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AddRoutineTabletDialogContent(onDismiss = {})
        }
    }
}
