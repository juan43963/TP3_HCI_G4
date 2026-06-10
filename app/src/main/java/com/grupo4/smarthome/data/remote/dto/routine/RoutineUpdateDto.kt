package com.grupo4.smarthome.data.remote.dto.routine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class RoutineUpdateDto(
    // El PUT exige la rutina completa: nombre y acciones siempre van
    val name: String,
    val actions: List<RoutineActionDto>,
    val metadata: JsonObject = buildJsonObject { },
)
