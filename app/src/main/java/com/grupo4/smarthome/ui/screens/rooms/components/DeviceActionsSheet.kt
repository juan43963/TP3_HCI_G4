package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.ArrowCircleUp
import androidx.compose.material.icons.outlined.GppBad
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.ModalCloseButton
import com.grupo4.smarthome.data.model.ActionArg
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionNames
import com.grupo4.smarthome.data.model.DeviceTypeNames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceActionsSheet(
    device: Device,
    onAction: (actionName: String, args: List<ActionArg>) -> Unit,
    onDismiss: () -> Unit,
    includePrimaryAction: Boolean = false,
    roomOptions: List<RoomOption> = emptyList(),
) {
    if (includePrimaryAction) {
        DeviceActionsCenteredCard(device = device, onAction = onAction, onDismiss = onDismiss, roomOptions = roomOptions)
    } else {
        DeviceActionsModalSheet(device = device, onAction = onAction, onDismiss = onDismiss, roomOptions = roomOptions)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceActionsModalSheet(
    device: Device,
    onAction: (String, List<ActionArg>) -> Unit,
    onDismiss: () -> Unit,
    roomOptions: List<RoomOption>,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SheetHeader(device = device)
            DeviceActionsBody(device = device, onAction = onAction, includePrimaryAction = false, roomOptions = roomOptions)
        }
    }
}

@Composable
private fun DeviceActionsCenteredCard(
    device: Device,
    onAction: (String, List<ActionArg>) -> Unit,
    onDismiss: () -> Unit,
    roomOptions: List<RoomOption>,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    SheetHeader(device = device, modifier = Modifier.weight(1f))
                    ModalCloseButton(onDismiss = onDismiss)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    DeviceActionsBody(device = device, onAction = onAction, includePrimaryAction = true, roomOptions = roomOptions)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DeviceActionsBody(
    device: Device,
    onAction: (String, List<ActionArg>) -> Unit,
    includePrimaryAction: Boolean,
    roomOptions: List<RoomOption>,
) {
    val pair = onOffPairFor(device)
    if (pair != null) {
        SectionCard {
            SectionHeader(
                title = stringResource(R.string.rooms_device_power),
                subtitle = stringResource(R.string.rooms_action_sub_power),
            )
            PowerPair(device = device, pair = pair, onAction = onAction)
        }
    }

    val canonical = device.type.canonicalName
    val primary = if (includePrimaryAction) null else DeviceActionNames.primaryValueAction(canonical)
    val rest = device.type.actions.filter { action ->
        (pair == null || (!action.name.equals(pair.onName, ignoreCase = true) &&
                !action.name.equals(pair.offName, ignoreCase = true))) &&
            (primary == null || !action.name.equals(primary, ignoreCase = true))
    }
    val triggers = rest.filter { DeviceActionRegistry.controlFor(it, canonical) is DeviceActionControl.Trigger }
    val others = rest.filter { DeviceActionRegistry.controlFor(it, canonical) !is DeviceActionControl.Trigger }
    if (triggers.isNotEmpty()) {
        SectionCard { TriggerGroup(actions = triggers, onAction = onAction) }
    }
    val configurable = !DeviceActionNames.gatesConfigurationWhenOff(device.type) || device.isOn
    if (configurable) {
        others.forEach { action ->
            SectionCard {
                ActionSection(action = action, device = device, onAction = onAction, roomOptions = roomOptions)
            }
        }
    } else if (others.isNotEmpty()) {
        SectionCard {
            SectionHeader(
                title = stringResource(R.string.rooms_device_turn_on_to_configure),
                subtitle = null,
            )
        }
    }
}

@Composable
private fun SheetHeader(device: Device, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.padding(top = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(RoomsIconTileGradient, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = deviceTypeIcon(device.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = deviceCategoryLabel(device.type),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RoomsSoftBorder),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String?, icon: ImageVector? = null) {
    if (icon == null) {
        SectionTitleColumn(title, subtitle)
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionIconTile(icon)
            SectionTitleColumn(title, subtitle, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
internal fun SectionTitleColumn(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) SubLabel(subtitle)
    }
}

@Composable
internal fun SectionIconTile(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(RoomsIconTileGradient),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun sectionActionIcon(actionName: String): ImageVector? = when {
    actionName.lowercase().contains("securitycode") -> Icons.Outlined.Lock
    actionName.equals("armStay", ignoreCase = true) ||
        actionName.equals("armHome", ignoreCase = true) -> Icons.Outlined.Home
    actionName.equals("armAway", ignoreCase = true) -> Icons.Outlined.Shield
    actionName.equals("disarm", ignoreCase = true) -> Icons.Outlined.GppBad
    else -> null
}

@Composable
internal fun SubLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ColumnScope.ActionSection(
    action: DeviceActionInfo,
    device: Device,
    onAction: (String, List<ActionArg>) -> Unit,
    roomOptions: List<RoomOption> = emptyList(),
) {
    val label = deviceActionLabel(action.name)
    val subtitle = deviceActionSubtitle(action.name)
    when (val control = DeviceActionRegistry.controlFor(action, device.type.canonicalName)) {
        is DeviceActionControl.Trigger ->
            TriggerControl(label = label) { onAction(action.name, emptyList()) }

        is DeviceActionControl.Numeric ->
            NumericSection(
                label = label,
                subtitle = subtitle,
                control = control,
                currentValue = numericCurrentValue(device, action.name),
            ) { value ->
                onAction(action.name, listOf(ActionArg.IntArg(value)))
            }

        is DeviceActionControl.Choice -> {
            SectionHeader(label, subtitle)
            ChoiceBody(
                suggestions = control.suggestions,
                currentValue = device.state.string(stateKeyFor(action.name)),
                allowCustomValue = !(
                    device.type.canonicalName == DeviceTypeNames.FRIDGE &&
                        action.name.equals("setMode", ignoreCase = true)
                    ),
            ) { value -> onAction(action.name, listOf(ActionArg.StringArg(value))) }
        }

        is DeviceActionControl.ColorPicker -> {
            SectionHeader(label, subtitle)
            ColorBody(currentValue = device.state.string(stateKeyFor(action.name))) { value ->
                onAction(action.name, listOf(ActionArg.StringArg(value)))
            }
        }

        is DeviceActionControl.Dispense -> {
            SectionHeader(label, subtitle)
            DispenseBody { args -> onAction(action.name, args) }
        }

        is DeviceActionControl.RoomPicker -> {
            SectionHeader(label, subtitle)
            if (roomOptions.isNotEmpty()) {
                RoomPickerBody(
                    options = roomOptions,
                    currentId = device.state.string(stateKeyFor(action.name)),
                ) { roomId -> onAction(action.name, listOf(ActionArg.StringArg(roomId))) }
            } else {
                GenericBody(params = action.params) { args -> onAction(action.name, args) }
            }
        }

        is DeviceActionControl.Code -> {
            val icon = sectionActionIcon(action.name)
            if (control.fields <= 1) {
                InlineCodeSection(label = label, subtitle = subtitle, icon = icon, length = control.length) { code ->
                    onAction(action.name, listOf(ActionArg.StringArg(code)))
                }
            } else {
                SectionHeader(label, subtitle, icon)
                MultiCodeBody(fields = control.fields, length = control.length) { codes ->
                    onAction(action.name, codes.map { ActionArg.StringArg(it) })
                }
            }
        }

        is DeviceActionControl.Generic -> {
            SectionHeader(label, subtitle)
            GenericBody(params = action.params) { args -> onAction(action.name, args) }
        }
    }
}

private fun stateKeyFor(actionName: String): String =
    actionName.removePrefix("set").removePrefix("Set").replaceFirstChar { it.lowercase() }

private fun numericCurrentValue(device: Device, actionName: String): Int? {
    val key = stateKeyFor(actionName)
    device.state.int(key)?.let { return it }
    return when (key) {
        "position" -> device.state.int("level")
        "level" -> device.state.int("position")
        else -> null
    }
}


private data class OnOffPair(
    val onName: String,
    val offName: String,
    val onIcon: ImageVector,
    val offIcon: ImageVector,
)

private fun onOffPairFor(device: Device): OnOffPair? {
    val type = device.type
    val turnOn = type.action("turnOn")?.name
    val turnOff = type.action("turnOff")?.name
    if (turnOn != null && turnOff != null) {
        return OnOffPair(turnOn, turnOff, Icons.Outlined.PowerSettingsNew, Icons.Outlined.PowerSettingsNew)
    }
    val open = type.action("open")?.name
    val close = type.action("close")?.name
    if (open != null && close != null) {
        return OnOffPair(open, close, Icons.Outlined.ArrowCircleUp, Icons.Outlined.ArrowCircleDown)
    }
    if (type.canonicalName == DeviceTypeNames.BLINDS) {
        val up = type.action("up")?.name
        val down = type.action("down")?.name
        if (up != null && down != null) {
            return OnOffPair(up, down, Icons.Outlined.ArrowCircleUp, Icons.Outlined.ArrowCircleDown)
        }
    }
    return null
}

@Composable
private fun PowerPair(
    device: Device,
    pair: OnOffPair,
    onAction: (String, List<ActionArg>) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PowerButton(
            label = deviceActionLabel(pair.onName),
            icon = pair.onIcon,
            active = device.isOn,
            onClick = { onAction(pair.onName, emptyList()) },
            modifier = Modifier.weight(1f),
        )
        PowerButton(
            label = deviceActionLabel(pair.offName),
            icon = pair.offIcon,
            active = !device.isOn,
            onClick = { onAction(pair.offName, emptyList()) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PowerButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content: @Composable () -> Unit = {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontWeight = FontWeight.Bold, maxLines = 1)
    }
    if (active) {
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
        ) { content() }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RoomsSoftBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        ) { content() }
    }
}


@Composable
private fun TriggerControl(label: String, onRun: () -> Unit) {
    Button(
        onClick = onRun,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TriggerGroup(
    actions: List<DeviceActionInfo>,
    onAction: (String, List<ActionArg>) -> Unit,
) {
    Text(
        text = stringResource(R.string.rooms_device_quick_controls),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        actions.forEach { action ->
            Column(
                modifier = Modifier.width(68.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CircleButton(icon = triggerIcon(action.name)) { onAction(action.name, emptyList()) }
                Text(
                    text = deviceActionLabel(action.name),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CircleButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(26.dp),
        )
    }
}

private fun triggerIcon(actionName: String): ImageVector = when {
    actionName.equals("start", ignoreCase = true) || actionName.equals("play", ignoreCase = true) ||
        actionName.equals("resume", ignoreCase = true) -> Icons.Outlined.PlayArrow
    actionName.equals("pause", ignoreCase = true) -> Icons.Outlined.Pause
    actionName.equals("stop", ignoreCase = true) -> Icons.Outlined.Stop
    actionName.equals("dock", ignoreCase = true) ||
        actionName.equals("returnToBase", ignoreCase = true) -> Icons.Outlined.Home
    actionName.equals("nextSong", ignoreCase = true) -> Icons.Outlined.SkipNext
    actionName.equals("previousSong", ignoreCase = true) -> Icons.Outlined.SkipPrevious
    DeviceActionNames.isReadOnlyAction(actionName) -> Icons.Outlined.Refresh
    else -> Icons.Outlined.PlayArrow
}

