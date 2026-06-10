package com.grupo4.smarthome.data.remote.dto.routine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RoutineDto(
    val id: String? = null,
    val name: String,
    val actions: List<RoutineActionDto> = emptyList(),
    val metadata: JsonObject? = null,
)
