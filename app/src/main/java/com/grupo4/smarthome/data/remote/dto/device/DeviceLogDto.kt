package com.grupo4.smarthome.data.remote.dto.device

import kotlinx.serialization.Serializable

@Serializable
data class DeviceLogDto(
    val id: String? = null,
    val timestamp: String? = null,
    val deviceId: String? = null,
    val actionName: String? = null,
)
