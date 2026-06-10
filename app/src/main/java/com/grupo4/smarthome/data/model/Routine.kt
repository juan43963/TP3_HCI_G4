package com.grupo4.smarthome.data.model

data class Routine(
    val id: String,
    val name: String,
    val actions: List<RoutineAction>,
    val category: RoutineCategory,
    val time: String,            // "HH:mm" 24h; la UI lo formatea a AM/PM
    val days: List<Boolean>,     // 7 flags, lunes primero
    val favorite: Boolean = false,
    val enabled: Boolean = true,
    val homeId: String = "",     // hogar dueño; se preserva en metadata al actualizar
)

data class RoutineAction(
    val deviceId: String,
    val deviceName: String,      // viene expandido en GET /routines; "" si falta
    val actionName: String,
    val params: List<String>,    // posicionales; el mapper los serializa a número si parsean
)

enum class RoutineCategory {
    MORNING,
    NIGHT,
    COMFORT,
    SECURITY,
    CUSTOM,
}
