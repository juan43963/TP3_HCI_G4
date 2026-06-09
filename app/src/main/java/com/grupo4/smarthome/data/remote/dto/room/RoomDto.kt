package com.grupo4.smarthome.data.remote.dto.room

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RoomDto(
    val id: String? = null,
    val name: String,
    val home: EntityRefDto? = null,
    val metadata: JsonObject? = null,
)
