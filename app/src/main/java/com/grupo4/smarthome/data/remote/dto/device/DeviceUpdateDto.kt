package com.grupo4.smarthome.data.remote.dto.device

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class DeviceUpdateDto(
    val name: String,
    val metadata: JsonObject = buildJsonObject { },
)
