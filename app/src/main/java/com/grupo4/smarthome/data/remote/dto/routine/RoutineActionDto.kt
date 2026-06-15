package com.grupo4.smarthome.data.remote.dto.routine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class RoutineActionDto(
    val device: RoutineDeviceRefDto? = null,
    val actionName: String? = null,
    val params: JsonArray? = null,
)

@Serializable
data class RoutineDeviceRefDto(
    val id: String? = null,
    val name: String? = null,
)
