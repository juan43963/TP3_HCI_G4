package com.grupo4.smarthome.data.remote.dto.room

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class RoomUpdateDto(
    val name: String,
    // No reenviamos `home`: la asociación al hogar se administra con endpoints aparte
    val metadata: JsonObject = buildJsonObject { },
)
