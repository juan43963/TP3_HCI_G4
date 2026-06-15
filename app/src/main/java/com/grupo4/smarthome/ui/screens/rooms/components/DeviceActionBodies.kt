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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.ActionArg
import com.grupo4.smarthome.data.model.DeviceActionParamInfo

@Composable
internal fun ColumnScope.NumericSection(
    label: String,
    subtitle: String?,
    control: DeviceActionControl.Numeric,
    currentValue: Int?,
    onApply: (Int) -> Unit,
) {
    val min = control.fallbackMin
    val max = control.fallbackMax.coerceAtLeast(min + 1)
    var value by remember(label, currentValue) { mutableStateOf((currentValue?.coerceIn(min, max) ?: min).toFloat()) }
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
internal fun ChoiceBody(
    suggestions: List<String>,
    currentValue: String?,
    allowCustomValue: Boolean = true,
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
        if (allowCustomValue) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomPickerBody(
    options: List<RoomOption>,
    currentId: String?,
    onApply: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { room ->
            FilterChip(
                selected = currentId == room.id,
                onClick = { onApply(room.id) },
                label = { Text(room.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Composable
internal fun ColorBody(currentValue: String?, onApply: (String) -> Unit) {
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
internal fun DispenseBody(onApply: (List<ActionArg>) -> Unit) {
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

@Composable
internal fun MultiCodeBody(
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

@Composable
internal fun InlineCodeSection(
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
internal fun GenericBody(
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

private val DispenseQuickAmounts = listOf("25", "50", "75", "100")
private val DispenseUnits = listOf("ml", "cl", "dl", "l")

private val ColorPresets = listOf("#F8BBD0", "#D1C4E9", "#BBDEFB", "#C8E6C9", "#FFF59D", "#FFCCBC")

private fun parseHexColor(value: String): Color? {
    val hex = value.trim().removePrefix("#")
    if (hex.length != 6 || hex.any { it.digitToIntOrNull(16) == null }) return null
    return runCatching { Color("FF$hex".toLong(16)) }.getOrNull()
}
