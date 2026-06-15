package com.grupo4.smarthome.data.remote.mapper

import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionParamInfo
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.DeviceState
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.remote.dto.device.DeviceDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceLogDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceTypeActionDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceTypeDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put

fun DeviceDto.toDomain(
    resolveType: (String?) -> DeviceTypeInfo?,
    fallbackRoomId: String,
): Device? {
    val typeInfo = resolveType(type?.id) ?: return null
    return Device(
        id = id.orEmpty(),
        name = name,
        type = typeInfo,
        roomId = room?.id ?: fallbackRoomId,
        state = DeviceState(state ?: JsonObject(emptyMap())),
        qrId = metadata.readString("qrId"),
    )
}

fun DeviceTypeDto.toDomain(): DeviceTypeInfo? {
    val typeId = id?.takeIf { it.isNotBlank() } ?: return null
    return DeviceTypeInfo(
        id = typeId,
        name = name?.takeIf { it.isNotBlank() } ?: typeId,
        powerUsage = powerUsage,
        actions = actions.mapNotNull { it.toDomain() },
    )
}

private fun DeviceTypeActionDto.toDomain(): DeviceActionInfo? {
    val actionName = name?.takeIf { it.isNotBlank() } ?: return null
    return DeviceActionInfo(
        name = actionName,
        params = params.mapIndexed { index, param ->
            // Solo extraemos min/max numéricos cuando el tipo del parámetro es numérico.
            // Para strings (color "#000000", securityCode "0000") el dominio queda sin min/max.
            val numericType = param.type?.lowercase() in NUMERIC_PARAM_TYPES
            DeviceActionParamInfo(
                name = param.name?.takeIf { it.isNotBlank() } ?: "param${index + 1}",
                type = param.type.orEmpty(),
                min = if (numericType) (param.minValue ?: param.min).asIntOrNull() else null,
                max = if (numericType) (param.maxValue ?: param.max).asIntOrNull() else null,
                supportedValues = param.supportedValues
                    ?.mapNotNull { (it as? JsonPrimitive)?.content }
                    .orEmpty(),
            )
        },
    )
}

fun buildDeviceMetadata(previous: JsonObject?, qrId: String?): JsonObject = buildJsonObject {
    previous?.forEach { (key, value) -> put(key, value) }
    if (qrId != null) put("qrId", JsonPrimitive(qrId))
}

private fun JsonObject?.readString(key: String): String? =
    (this?.get(key) as? JsonPrimitive)?.content

private val NUMERIC_PARAM_TYPES = setOf("integer", "int", "number", "float", "double")

// Convierte a Int? solo si el JsonElement es número o string numérico ("16").
// Un decimal (2.5), un string no numérico ("#FFFFFF") o JsonNull devuelven null → se usa el fallback.
private fun JsonElement?.asIntOrNull(): Int? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.intOrNull ?: primitive.contentOrNull?.toIntOrNull()
}

fun DeviceLogDto.toDomain(): DeviceLog? {
    val logId = id ?: return null
    return DeviceLog(
        id = logId,
        timestamp = timestamp.orEmpty(),
        deviceId = deviceId.orEmpty(),
        actionName = actionName.orEmpty(),
    )
}
