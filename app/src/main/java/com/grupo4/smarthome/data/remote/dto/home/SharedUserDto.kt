package com.grupo4.smarthome.data.remote.dto.home

import kotlinx.serialization.Serializable

@Serializable
data class SharedUserDto(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val sharedAt: String? = null,
)
