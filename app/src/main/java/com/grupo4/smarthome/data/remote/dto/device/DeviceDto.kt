package com.grupo4.smarthome.data.remote.dto.device

import com.grupo4.smarthome.data.remote.dto.room.EntityRefDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DeviceDto(
    val id: String? = null,
    val name: String,
    val type: EntityRefDto? = null,
    val state: JsonObject? = null,
    val room: EntityRefDto? = null,
    val metadata: JsonObject? = null,
)
