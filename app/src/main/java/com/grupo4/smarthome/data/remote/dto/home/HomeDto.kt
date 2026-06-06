package com.grupo4.smarthome.data.remote.dto.home

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class HomeDto(
    val id: String? = null,
    val name: String,
    val metadata: JsonObject? = null,
    val sharedWith: List<SharedUserDto>? = null,
)
