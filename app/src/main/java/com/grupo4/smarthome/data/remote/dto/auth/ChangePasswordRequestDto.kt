package com.grupo4.smarthome.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequestDto(
    val oldPassword: String,
    val newPassword: String,
)
