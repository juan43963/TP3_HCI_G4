package com.grupo4.smarthome.data.model

data class Routine(
    val id: String,
    val name: String,
    val actions: List<RoutineAction>,
    val category: RoutineCategory,
    val time: String,
    val days: List<Boolean>,
    val favorite: Boolean = false,
    val enabled: Boolean = true,
    val homeId: String = "",
)

data class RoutineAction(
    val deviceId: String,
    val deviceName: String,
    val actionName: String,
    val params: List<String>,
)

enum class RoutineCategory {
    MORNING,
    NIGHT,
    COMFORT,
    SECURITY,
    CUSTOM,
}
