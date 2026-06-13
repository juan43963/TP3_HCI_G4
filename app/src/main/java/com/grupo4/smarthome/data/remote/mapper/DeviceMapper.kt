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

/**
 * Convierte un [DeviceDto] al modelo de dominio [Device].
 *
 * @param resolveType mapea el `type.id` (referencia liviana del DTO) al [DeviceTypeInfo] del catálogo
 *   cacheado. Si no se puede resolver, devolvemos null para no renderizar tipos desconocidos; el
 *   repository es responsable de filtrar esos nulls sin crashear.
 * @param fallbackRoomId roomId a usar cuando la respuesta no trae `room.id` (p. ej. acciones/state).
 *
 * El estado heterogéneo (`state`) se guarda tal cual en [DeviceState]; los accessors tipados viven en
 * [DeviceState]/[Device], así que la UI nunca toca el `JsonObject` crudo.
 */
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

/**
 * Convierte un tipo del catálogo (`GET /devicetypes`) al dominio [DeviceTypeInfo].
 *
 * Fallback explícito del id: si el back-end no manda `id` (o viene en blanco) devolvemos null y el
 * repository descarta el tipo, porque sin id no se podría crear un dispositivo de ese tipo. Así nunca
 * queda un [DeviceTypeInfo] con `id` vacío circulando por la app. Si falta `name` (pero hay id),
 * usamos el id como name para no perder el tipo ni dejar un name vacío sin decidir.
 */
fun DeviceTypeDto.toDomain(): DeviceTypeInfo? {
    val typeId = id?.takeIf { it.isNotBlank() } ?: return null
    return DeviceTypeInfo(
        id = typeId,
        name = name?.takeIf { it.isNotBlank() } ?: typeId,
        powerUsage = powerUsage,
        actions = actions.mapNotNull { it.toDomain() },
    )
}

/**
 * Descartamos acciones sin nombre (no se podrían ejecutar). Los params, en cambio, NO se descartan: la
 * API puede devolverlos posicionales sin `name`, así que generamos un nombre fallback estable por
 * posición (`param1`, `param2`, ...) para preservar el orden y la cantidad de parámetros.
 */
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

// Construye el metadata para POST/PUT preservando el previo y escribiendo qrId solo si viene.
// El merge evita pisar otras claves que la API pueda guardar.
fun buildDeviceMetadata(previous: JsonObject?, qrId: String?): JsonObject = buildJsonObject {
    previous?.forEach { (key, value) -> put(key, value) }
    if (qrId != null) put("qrId", JsonPrimitive(qrId))
}

private fun JsonObject?.readString(key: String): String? =
    (this?.get(key) as? JsonPrimitive)?.content

// Descartamos logs sin id (no se pueden identificar como entrada de la lista)
fun DeviceLogDto.toDomain(): DeviceLog? {
    val logId = id ?: return null
    return DeviceLog(
        id = logId,
        timestamp = timestamp.orEmpty(),
        deviceId = deviceId.orEmpty(),
        actionName = actionName.orEmpty(),
    )
}
