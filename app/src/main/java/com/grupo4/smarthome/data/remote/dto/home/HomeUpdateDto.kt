package com.grupo4.smarthome.data.remote.dto.home

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class HomeUpdateDto(
    val name: String,
    val metadata: JsonObject = buildJsonObject { },
)
