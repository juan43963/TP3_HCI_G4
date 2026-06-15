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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.grupo4.smarthome.data.model.DeviceActionParamInfo

/**
 * Panel "Más acciones": header (nombre/categoría/estado) + una tarjeta por sección. La primera es el
 * par encender/apagar (turnOn/turnOff u open/close); el resto, una tarjeta por acción del catálogo con
 * su control. Excluye la acción de valor primario (slider que ya está en la card: setBrightness/
 * setTemperature/setVolume) para los tipos que la muestran en la card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceActionsSheet(
    device: Device,
    onAction: (actionName: String, args: List<ActionArg>) -> Unit,
    onDismiss: () -> Unit,
    // En tablet la card no tiene el slider primario, así que el sheet también lo incluye. También es la
    // señal de que estamos en tablet: ahí el panel se muestra como card flotante centrada (no bottom sheet).
    includePrimaryAction: Boolean = false,
) {
    if (includePrimaryAction) {
        DeviceActionsCenteredCard(device = device, onAction = onAction, onDismiss = onDismiss)
    } else {
        DeviceActionsModalSheet(device = device, onAction = onAction, onDismiss = onDismiss)
    }
}

// Teléfono: panel como hoja inferior (bottom sheet).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceActionsModalSheet(
    device: Device,
    onAction: (String, List<ActionArg>) -> Unit,
    onDismiss: () -> Unit,
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
            DeviceActionsBody(device = device, onAction = onAction, includePrimaryAction = false)
        }
    }
}

// Tablet: card flotante centrada en el medio de la pantalla, con X arriba a la derecha y alto acotado + scroll interno.
@Composable
private fun DeviceActionsCenteredCard(
    device: Device,
    onAction: (String, List<ActionArg>) -> Unit,
    onDismiss: () -> Unit,
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
                // Header fijo arriba junto a la X: al scrollear el cuerpo, sigue visible de qué dispositivo se trata.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    SheetHeader(device = device, showStatus = false, modifier = Modifier.weight(1f))
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
                    DeviceActionsBody(device = device, onAction = onAction, includePrimaryAction = true)
                }
            }
        }
    }
}

// Cuerpo compartido: header + una tarjeta por sección. Igual en tablet y teléfono.
@Composable
private fun ColumnScope.DeviceActionsBody(
    device: Device,
    onAction: (String, List<ActionArg>) -> Unit,
    includePrimaryAction: Boolean,
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

    // Resto de acciones: menos las del par de arriba y la acción de valor primario (slider de la
    // card). En tablet (includePrimaryAction) NO se excluye, porque la card no tiene el slider.
    val canonical = device.type.canonicalName
    val primary = if (includePrimaryAction) null else DeviceActionNames.primaryValueAction(canonical)
    val rest = device.type.actions.filter { action ->
        // getState se resuelve solo como refresco; no lo mostramos como acción.
        !DeviceActionNames.isStateRefreshAction(action.name) &&
            (pair == null || (!action.name.equals(pair.onName, ignoreCase = true) &&
                !action.name.equals(pair.offName, ignoreCase = true))) &&
            (primary == null || !action.name.equals(primary, ignoreCase = true))
    }
    // Acciones sin parámetros (start/pause/dock/stop/next/...) van como botones circulares en grupo.
    val triggers = rest.filter { DeviceActionRegistry.controlFor(it, canonical) is DeviceActionControl.Trigger }
    val others = rest.filter { DeviceActionRegistry.controlFor(it, canonical) !is DeviceActionControl.Trigger }
    if (triggers.isNotEmpty()) {
        SectionCard { TriggerGroup(actions = triggers, onAction = onAction) }
    }
    others.forEach { action ->
        SectionCard {
            ActionSection(action = action, device = device, onAction = onAction)
        }
    }
}

@Composable
private fun SheetHeader(device: Device, showStatus: Boolean = true, modifier: Modifier = Modifier) {
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
            if (showStatus) {
                Spacer(modifier = Modifier.height(6.dp))
                DeviceStatusRow(device = device)
            }
        }
    }
}

// Tarjeta de sección: superficie redondeada con borde suave; el contenido se apila con espaciado parejo.
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
private fun SectionTitleColumn(title: String, subtitle: String?, modifier: Modifier = Modifier) {
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
private fun SectionIconTile(icon: ImageVector) {
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

// Ícono por acción para el header de sección (alarma). null = sin ícono (resto de los tipos).
private fun sectionActionIcon(actionName: String): ImageVector? = when {
    actionName.lowercase().contains("securitycode") -> Icons.Outlined.Lock
    actionName.equals("armStay", ignoreCase = true) ||
        actionName.equals("armHome", ignoreCase = true) -> Icons.Outlined.Home
    actionName.equals("armAway", ignoreCase = true) -> Icons.Outlined.Shield
    actionName.equals("disarm", ignoreCase = true) -> Icons.Outlined.GppBad
    else -> null
}

@Composable
private fun SubLabel(text: String) {
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
) {
    val label = deviceActionLabel(action.name)
    val subtitle = deviceActionSubtitle(action.name)
    when (val control = DeviceActionRegistry.controlFor(action, device.type.canonicalName)) {
        is DeviceActionControl.Trigger ->
            TriggerControl(label = label) { onAction(action.name, emptyList()) }

        is DeviceActionControl.Numeric ->
            NumericSection(label = label, subtitle = subtitle, control = control) { value ->
                onAction(action.name, listOf(ActionArg.IntArg(value)))
            }

        is DeviceActionControl.Choice -> {
            SectionHeader(label, subtitle)
            ChoiceBody(
                suggestions = control.suggestions,
                currentValue = device.state.string(stateKeyFor(action.name)),
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

        is DeviceActionControl.Code -> {
            val icon = sectionActionIcon(action.name)
            if (control.fields <= 1) {
                // Armar/desarmar: input de código + Apply inline a la derecha del título.
                InlineCodeSection(label = label, subtitle = subtitle, icon = icon, length = control.length) { code ->
                    onAction(action.name, listOf(ActionArg.StringArg(code)))
                }
            } else {
                // Cambiar código de seguridad: inputs apilados (actual/nuevo) + Apply full-width.
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

// Clave del estado para resaltar el valor actual: "setMode" -> "mode", "setVerticalSwing" -> "verticalSwing".
private fun stateKeyFor(actionName: String): String =
    actionName.removePrefix("set").removePrefix("Set").replaceFirstChar { it.lowercase() }

// --- Par on/off (power / open-close) ---

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

// --- Controles (cuerpos sin label: el título lo pone la tarjeta) ---

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

// Grupo "Quick controls": acciones sin parámetros como botones circulares en grilla que envuelve
// (play/pause/stop/resume/next/previous/dock/getPlaylist...).
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

@Composable
private fun ColumnScope.NumericSection(
    label: String,
    subtitle: String?,
    control: DeviceActionControl.Numeric,
    onApply: (Int) -> Unit,
) {
    val min = control.fallbackMin
    val max = control.fallbackMax.coerceAtLeast(min + 1)
    var value by remember(label) { mutableStateOf(min.toFloat()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) SubLabel(subtitle)
        }
        Text(
            text = value.toInt().toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StepButton(symbol = "−") {
            value = (value - 1).coerceAtLeast(min.toFloat())
            onApply(value.toInt())
        }
        Slider(
            value = value,
            onValueChange = { value = it },
            valueRange = min.toFloat()..max.toFloat(),
            onValueChangeFinished = { onApply(value.toInt()) },
            modifier = Modifier.weight(1f),
        )
        StepButton(symbol = "+") {
            value = (value + 1).coerceAtMost(max.toFloat())
            onApply(value.toInt())
        }
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, RoomsSoftBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(text = symbol, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceBody(
    suggestions: List<String>,
    currentValue: String?,
    onApply: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (suggestions.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { suggestion ->
                    FilterChip(
                        selected = currentValue?.equals(suggestion, ignoreCase = true) == true,
                        onClick = { text = suggestion; onApply(suggestion) },
                        label = { Text(deviceActionOptionLabel(suggestion)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.rooms_device_custom_value)) },
            )
            ApplyButton(enabled = text.isNotBlank(), onClick = { onApply(text.trim()) })
        }
    }
}

@Composable
private fun ColorBody(currentValue: String?, onApply: (String) -> Unit) {
    var text by remember { mutableStateOf(currentValue.orEmpty()) }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.rooms_device_color)) },
            )
            Box(
                modifier = Modifier
                    .size(56.dp, 52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(parseHexColor(text) ?: MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, RoomsSoftBorder, RoundedCornerShape(12.dp)),
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ColorPresets.forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(hex) ?: MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, RoomsSoftBorder, CircleShape)
                        .clickable { text = hex },
                )
            }
        }
        ApplyButton(
            enabled = text.isNotBlank(),
            onClick = { onApply(text.trim()) },
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DispenseBody(onApply: (List<ActionArg>) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SubLabel(stringResource(R.string.rooms_device_quantity))
        OutlinedTextField(
            value = amount,
            onValueChange = { new -> amount = new.filter { it.isDigit() || it == '.' } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.rooms_device_enter_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        SubLabel(stringResource(R.string.rooms_device_quick_amounts))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DispenseQuickAmounts.forEach { quick ->
                FilterChip(
                    selected = amount == quick,
                    onClick = { amount = quick },
                    label = { Text(quick) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
        SubLabel(stringResource(R.string.rooms_device_unit))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DispenseUnits.forEach { u ->
                FilterChip(
                    selected = unit.equals(u, ignoreCase = true),
                    onClick = { unit = u },
                    label = { Text(u) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
        Button(
            onClick = {
                val amountArg = amount.toIntOrNull()?.let { ActionArg.IntArg(it) }
                    ?: amount.toDoubleOrNull()?.let { ActionArg.NumberArg(it) }
                    ?: ActionArg.StringArg(amount)
                onApply(listOf(amountArg, ActionArg.StringArg(unit)))
            },
            enabled = amount.isNotBlank() && unit.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = stringResource(R.string.rooms_device_apply), fontWeight = FontWeight.Bold)
        }
    }
}

// Cambiar código de seguridad: inputs apilados (actual/nuevo) + Apply full-width.
@Composable
private fun MultiCodeBody(
    fields: Int,
    length: Int,
    onApply: (List<String>) -> Unit,
) {
    val values = remember(fields) { mutableStateOf(List(fields.coerceAtLeast(1)) { "" }) }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.value.forEachIndexed { index, field ->
            OutlinedTextField(
                value = field,
                onValueChange = { new ->
                    val digits = new.filter(Char::isDigit).take(length)
                    values.value = values.value.toMutableList().also { it[index] = digits }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                // Código sensible: teclado numérico y enmascarado; nunca se loguea.
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                placeholder = {
                    Text(
                        if (index == 0) stringResource(R.string.rooms_device_code_current)
                        else stringResource(R.string.rooms_device_code_new),
                    )
                },
            )
        }
        Button(
            onClick = { onApply(values.value.map { it.trim() }) },
            enabled = values.value.all { it.length == length },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = stringResource(R.string.rooms_device_apply), fontWeight = FontWeight.Bold)
        }
    }
}

// Armar/desarmar: ícono + título/subtítulo (izquierda) + input de código + Apply (inline a la derecha).
@Composable
private fun InlineCodeSection(
    label: String,
    subtitle: String?,
    icon: ImageVector?,
    length: Int,
    onApply: (String) -> Unit,
) {
    var code by remember(label) { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) SectionIconTile(icon)
        SectionTitleColumn(label, subtitle, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(length) },
            modifier = Modifier.width(100.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            placeholder = { Text(stringResource(R.string.rooms_device_code)) },
        )
        TextButton(onClick = { onApply(code.trim()) }, enabled = code.length == length) {
            Text(text = stringResource(R.string.rooms_device_apply), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GenericBody(
    params: List<DeviceActionParamInfo>,
    onApply: (List<ActionArg>) -> Unit,
) {
    val values = remember { mutableStateOf(List(params.size) { "" }) }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        params.forEachIndexed { index, param ->
            val numeric = param.type.lowercase() in setOf("integer", "int", "number", "float", "double")
            OutlinedTextField(
                value = values.value[index],
                onValueChange = { new ->
                    val filtered = if (numeric) new.filter { it.isDigit() || it == '-' || it == '.' } else new
                    values.value = values.value.toMutableList().also { it[index] = filtered }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                label = { Text(param.name) },
            )
        }
        ApplyButton(
            enabled = values.value.all { it.isNotBlank() },
            onClick = {
                val args = params.mapIndexed { index, param ->
                    val raw = values.value[index].trim()
                    val numeric = param.type.lowercase() in setOf("integer", "int", "number", "float", "double")
                    when {
                        !numeric -> ActionArg.StringArg(raw)
                        raw.toIntOrNull() != null -> ActionArg.IntArg(raw.toInt())
                        raw.toDoubleOrNull() != null -> ActionArg.NumberArg(raw.toDouble())
                        else -> ActionArg.StringArg(raw)
                    }
                }
                onApply(args)
            },
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun ApplyButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text = stringResource(R.string.rooms_device_apply), fontWeight = FontWeight.Bold)
    }
}

// Cantidades/unidades sugeridas del prototipo. Editable/seleccionable; si la API espera otras, no bloquea.
private val DispenseQuickAmounts = listOf("0.25", "0.5", "1", "2", "5")
private val DispenseUnits = listOf("ml", "l", "oz", "cup")

// Presets pastel del prototipo (hex). Editable: el campo manda lo que el usuario tenga, así que si la
// API espera otro formato, no lo bloquea (mismo criterio que los chips de Choice).
private val ColorPresets = listOf("#F8BBD0", "#D1C4E9", "#BBDEFB", "#C8E6C9", "#FFF59D", "#FFCCBC")

// Parsea un hex (#RRGGBB o RRGGBB) a Color para la preview/círculos; null si no es válido.
private fun parseHexColor(value: String): Color? {
    val hex = value.trim().removePrefix("#")
    if (hex.length != 6 || hex.any { it.digitToIntOrNull(16) == null }) return null
    return runCatching { Color("FF$hex".toLong(16)) }.getOrNull()
}
