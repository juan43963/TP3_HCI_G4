package com.grupo4.smarthome.data.remote.dto.device

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
    val `return`: JsonElement? = null,
)

@Serializable
data class DeviceTypeActionParamDto(
    val name: String? = null,
    val type: String? = null,
    val min: JsonElement? = null,
    val max: JsonElement? = null,
    val minValue: JsonElement? = null,
    val maxValue: JsonElement? = null,
    val supportedValues: List<JsonElement>? = null,
    val description: String? = null,
    val example: JsonElement? = null,
)
