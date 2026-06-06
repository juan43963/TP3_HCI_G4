package com.grupo4.smarthome.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class SendVerificationRequestDto(
    val email: String,
)
