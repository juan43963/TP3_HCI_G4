package com.grupo4.smarthome.ui.screens.routines

import androidx.annotation.StringRes
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory as DomainCategory
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

data class RoutinesUiState(
    val routines: List<Routine> = emptyList(),
    // Dispositivos del hogar activo: para crear acciones y resolver nombres legibles
    val devices: List<Device> = emptyList(),
    val deviceRoomNames: Map<String, String> = emptyMap(),
    val deviceActions: Map<DeviceType, List<DeviceActionInfo>> = emptyMap(),
    val isLoading: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val infoMessage: String? = null,
    val executingRoutineId: String? = null,
)

// Nombres de dispositivos por id, para mostrar acciones legibles en las cards
val RoutinesUiState.deviceNamesById: Map<String, String>
    get() = devices.associate { it.id to it.name }

// Tipos de dispositivo por id, para resolver el ícono de cada acción
val RoutinesUiState.deviceTypesById: Map<String, DeviceType>
    get() = devices.associate { it.id to it.type }

// --- Mapeo dominio -> modelos de UI existentes (RoutineUi / RoutineDetailUi) ---

fun Routine.toRoutineUi(deviceNamesById: Map<String, String>): RoutineUi {
    val labels = actions.map { it.toReadableLabel(deviceNamesById) }
    return RoutineUi(
        id = id,
        name = name,
        category = category.toUiCategory(),
        time = formatRoutineTime(time),
        actions = labels.take(2),
        extraActions = (labels.size - 2).coerceAtLeast(0),
        days = days,
        favorite = favorite,
    )
}

fun DomainCategory.toUiCategory(): RoutineCategory = when (this) {
    DomainCategory.MORNING -> RoutineCategory.MORNING
    DomainCategory.NIGHT -> RoutineCategory.NIGHT
    DomainCategory.COMFORT -> RoutineCategory.COMFORT
    DomainCategory.SECURITY -> RoutineCategory.SECURITY
    DomainCategory.CUSTOM -> RoutineCategory.CUSTOM
}

fun RoutineCategory.toDomainCategory(): DomainCategory = when (this) {
    RoutineCategory.MORNING -> DomainCategory.MORNING
    RoutineCategory.NIGHT -> DomainCategory.NIGHT
    RoutineCategory.COMFORT -> DomainCategory.COMFORT
    RoutineCategory.SECURITY -> DomainCategory.SECURITY
    RoutineCategory.CUSTOM -> DomainCategory.CUSTOM
}

fun Routine.toDetailUi(deviceNamesById: Map<String, String>, deviceTypesById: Map<String, DeviceType>): RoutineDetailUi {
    val detailActions = actions.map { action ->
        val device = action.deviceName.ifBlank { deviceNamesById[action.deviceId] ?: "dispositivo" }
        val icon = deviceTypesById[action.deviceId]?.let { deviceTypeIcon(it) } ?: SmartHomeIcons.Routines
        val params = action.params.joinToString(", ")
        val description = action.actionName.toReadableAction() + if (params.isNotBlank()) " • $params" else ""
        RoutineActionUi(icon = icon, name = device, description = description)
    }
    return RoutineDetailUi(
        id = id,
        name = name,
        category = category.toUiCategory(),
        enabled = enabled,
        time = formatRoutineTime(time),
        daysSummary = "",
        days = days,
        actions = detailActions,
        favorite = favorite,
    )
}

// "hh:mm AM/PM" -> "HH:mm" 24h
fun parseDisplayTimeTo24h(display: String): String = runCatching {
    val parts = display.trim().split(" ")
    val hm = parts[0].split(":")
    var hour = hm[0].toInt()
    val minute = hm[1].toInt()
    when (parts.getOrNull(1)?.uppercase()) {
        "PM" -> if (hour != 12) hour += 12
        "AM" -> if (hour == 12) hour = 0
    }
    "%02d:%02d".format(hour, minute)
}.getOrDefault(display)

// "HH:mm" 24h -> "hh:mm AM/PM"
fun formatRoutineTime(time: String): String = runCatching {
    val parts = time.trim().split(":")
    val hour = parts[0].toInt()
    val minute = parts[1].toInt()
    val isAm = hour < 12
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    "%02d:%02d %s".format(displayHour, minute, if (isAm) "AM" else "PM")
}.getOrDefault(time)

private fun RoutineAction.toReadableLabel(deviceNamesById: Map<String, String>): String {
    val device = deviceName.ifBlank { deviceNamesById[deviceId] ?: "dispositivo" }
    return "${actionName.toReadableAction()} $device"
}

internal fun String.toReadableAction(): String = when (this) {
    "turnOn" -> "Encender"
    "turnOff" -> "Apagar"
    "open" -> "Abrir"
    "close" -> "Cerrar"
    "lock" -> "Trabar"
    "unlock" -> "Destrabar"
    "play" -> "Reproducir"
    "pause" -> "Pausar"
    "stop" -> "Detener"
    "start" -> "Iniciar"
    "setBrightness" -> "Cambiar brillo"
    "setColor" -> "Cambiar color"
    "setTemperature" -> "Cambiar temperatura"
    "setVolume" -> "Cambiar volumen"
    "setMode" -> "Cambiar modo"
    else -> this
}
