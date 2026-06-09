package com.grupo4.smarthome.data.remote.dto.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val name: String,
    val metadata: JsonObject = buildJsonObject { },
)
