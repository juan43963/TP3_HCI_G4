package com.grupo4.smarthome.data.remote.dto.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UserUpdateDto(
    val name: String,
    val metadata: JsonObject? = null,
)
