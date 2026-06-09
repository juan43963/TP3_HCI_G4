package com.grupo4.smarthome.data.remote.dto.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UserDto(
    val id: String? = null,
    val email: String,
    val name: String,
    val isVerified: Boolean = false,
    val createdAt: String? = null,
    val metadata: JsonObject? = null,
)
