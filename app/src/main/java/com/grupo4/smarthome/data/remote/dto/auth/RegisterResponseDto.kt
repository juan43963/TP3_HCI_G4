package com.grupo4.smarthome.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponseDto(
    val user: UserDto? = null,
    val token: String? = null,
)
