package com.grupo4.smarthome.data.remote.dto.room

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class RoomCreateDto(
    val name: String,
    // Siempre se envía con id no nulo (EntityRefDto(homeId)) para asociar la habitación al hogar
    val home: EntityRefDto,
    val metadata: JsonObject = buildJsonObject { },
)
