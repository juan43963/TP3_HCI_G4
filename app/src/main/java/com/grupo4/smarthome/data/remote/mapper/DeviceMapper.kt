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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
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
            DeviceActionParamInfo(
                name = param.name?.takeIf { it.isNotBlank() } ?: "param${index + 1}",
                type = param.type.orEmpty(),
                min = param.min,
                max = param.max,
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

fun DeviceLogDto.toDomain(): DeviceLog? {
    val logId = id ?: return null
    return DeviceLog(
        id = logId,
        timestamp = timestamp.orEmpty(),
        deviceId = deviceId.orEmpty(),
        actionName = actionName.orEmpty(),
    )
}
