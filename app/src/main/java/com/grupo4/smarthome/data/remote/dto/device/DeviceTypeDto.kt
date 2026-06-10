package com.grupo4.smarthome.data.remote.dto.device

import kotlinx.serialization.Serializable

// Catálogo de tipos soportados por el back-end (GET /devicetypes). Usamos `name` para mapear
// contra el enum local DeviceType y `actions` para resolver qué acción ejecutar sin hardcodear.
@Serializable
data class DeviceTypeDto(
    val id: String? = null,
    val name: String? = null,
    val powerUsage: Int? = null,
    val actions: List<DeviceTypeActionDto> = emptyList(),
)

@Serializable
data class DeviceTypeActionDto(
    val name: String? = null,
    val params: List<DeviceTypeActionParamDto> = emptyList(),
)

@Serializable
data class DeviceTypeActionParamDto(
    val name: String? = null,
    val type: String? = null,
    val min: Int? = null,
    val max: Int? = null,
)
