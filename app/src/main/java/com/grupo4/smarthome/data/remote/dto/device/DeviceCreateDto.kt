package com.grupo4.smarthome.data.remote.dto.device

import com.grupo4.smarthome.data.remote.dto.room.EntityRefDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

// Body de POST /devices. `type.id` es el id real del catálogo (GET /devicetypes);
// `room.id` asocia el dispositivo a la habitación al crearlo (con fallback POST /rooms/{roomId}/devices/{deviceId}).
@Serializable
data class DeviceCreateDto(
    val name: String,
    val type: EntityRefDto,
    val room: EntityRefDto? = null,
    val metadata: JsonObject = buildJsonObject { },
)
