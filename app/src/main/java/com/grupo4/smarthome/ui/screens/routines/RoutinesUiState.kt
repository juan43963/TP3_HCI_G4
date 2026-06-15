package com.grupo4.smarthome.ui.screens.routines

import androidx.annotation.StringRes
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory as DomainCategory
import com.grupo4.smarthome.ui.screens.rooms.components.deviceTypeIcon
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

data class RoutinesUiState(
    val routines: List<Routine> = emptyList(),
    val devices: List<Device> = emptyList(),
    val deviceRoomNames: Map<String, String> = emptyMap(),
    val deviceActions: Map<String, List<DeviceActionInfo>> = emptyMap(),
    val isLoading: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val infoMessage: String? = null,
    val executingRoutineId: String? = null,
)

val RoutinesUiState.deviceNamesById: Map<String, String>
    get() = devices.associate { it.id to it.name }

val RoutinesUiState.deviceTypesById: Map<String, DeviceTypeInfo>
    get() = devices.associate { it.id to it.type }


fun Routine.toRoutineUi(deviceNamesById: Map<String, String>): RoutineUi {
    val actionItems = actions.map { it.toActionItemUi(deviceNamesById) }
    return RoutineUi(
        id = id,
        name = name,
        category = category.toUiCategory(),
        time = formatRoutineTime(time),
        actions = actionItems.take(2),
        extraActions = (actionItems.size - 2).coerceAtLeast(0),
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

fun Routine.toDetailUi(deviceNamesById: Map<String, String>, deviceTypesById: Map<String, DeviceTypeInfo>): RoutineDetailUi {
    val detailActions = actions.map { action ->
        val icon = deviceTypesById[action.deviceId]?.let { deviceTypeIcon(it) } ?: SmartHomeIcons.Routines
        RoutineActionUi(
            icon = icon,
            action = action.toActionItemUi(deviceNamesById),
        )
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

private fun RoutineAction.toActionItemUi(deviceNamesById: Map<String, String>) =
    RoutineActionItemUi(
        actionName = actionName,
        deviceName = deviceName.ifBlank { deviceNamesById[deviceId].orEmpty() },
        params = params,
    )
