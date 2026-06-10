package com.grupo4.smarthome.data.remote.dto.device

import com.grupo4.smarthome.data.remote.dto.room.EntityRefDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// Dispositivo tal como lo devuelve la API. `type` y `room` son referencias livianas por id;
// `state` es heterogéneo según el tipo, por eso lo dejamos como JsonObject y lo leemos en el mapper.
@Serializable
data class DeviceDto(
    val id: String? = null,
    val name: String,
    val type: EntityRefDto? = null,
    val state: JsonObject? = null,
    val room: EntityRefDto? = null,
    val metadata: JsonObject? = null,
)
