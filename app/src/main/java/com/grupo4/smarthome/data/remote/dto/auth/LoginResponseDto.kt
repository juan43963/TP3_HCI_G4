package com.grupo4.smarthome.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val user: UserDto,
    val token: String,
)
