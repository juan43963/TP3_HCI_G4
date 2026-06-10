package com.grupo4.smarthome.data.remote.dto.routine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class RoutineCreateDto(
    val name: String,
    val actions: List<RoutineActionDto>,
    val metadata: JsonObject = buildJsonObject { },
)
