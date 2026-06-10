package com.grupo4.smarthome.data.remote.dto.routine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class RoutineActionDto(
    val device: RoutineDeviceRefDto? = null,
    val actionName: String? = null,
    // Params posicionales y heterogéneos ([], [80], ["cool"]): los dejamos crudos como JsonArray
    val params: JsonArray? = null,
)

// Ref propio (no EntityRefDto) porque GET /routines devuelve el device expandido con su nombre
@Serializable
data class RoutineDeviceRefDto(
    val id: String? = null,
    val name: String? = null,
)
