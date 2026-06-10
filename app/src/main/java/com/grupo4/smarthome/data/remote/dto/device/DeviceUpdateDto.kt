package com.grupo4.smarthome.data.remote.dto.device

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

// Body de PUT /devices/{deviceId}: solo nombre y metadata (el tipo no se edita).
@Serializable
data class DeviceUpdateDto(
    val name: String,
    val metadata: JsonObject = buildJsonObject { },
)
