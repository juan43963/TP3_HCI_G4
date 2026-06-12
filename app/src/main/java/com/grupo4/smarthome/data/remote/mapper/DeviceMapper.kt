package com.grupo4.smarthome.data.remote.mapper

import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.remote.dto.device.DeviceDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceLogDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put

// Estados de la API que representan "apagado/inactivo". Cualquier otro status se interpreta como encendido.
private val OffStatuses = setOf(
    "off", "closed", "stopped", "disarmed", "docked", "idle", "paused", "standby",
)

/**
 * Convierte un [DeviceDto] al modelo de dominio [Device].
 *
 * @param resolveType mapea el id del tipo (viene en `type.id`) al enum local usando el catálogo
 *   cacheado. Si no se puede resolver, devolvemos null para no renderizar tipos desconocidos; el
 *   repository es responsable de filtrar esos nulls sin crashear.
 * @param fallbackRoomId roomId a usar cuando la respuesta no trae `room.id` (p. ej. acciones/state).
 *
 * Nota: los favoritos de dispositivo (`isFavorite`) quedan fuera de alcance en esta etapa; la API
 * los guardaría en metadata, pero por ahora mantenemos el default local (false).
 */
fun DeviceDto.toDomain(
    resolveType: (String?) -> DeviceType?,
    fallbackRoomId: String,
): Device? {
    val deviceType = resolveType(type?.id) ?: return null
    val deviceState = state
    return Device(
        id = id.orEmpty(),
        name = name,
        type = deviceType,
        roomId = room?.id ?: fallbackRoomId,
        isOn = deviceState.readIsOn(),
        brightness = deviceState.readInt("brightness"),
        temperature = deviceState.readInt("temperature"),
        volume = deviceState.readInt("volume"),
        locked = deviceState.readLocked(),
        qrId = metadata.readString("qrId"),
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

private fun JsonObject?.readInt(key: String): Int? =
    (this?.get(key) as? JsonPrimitive)?.intOrNull

// Lógica defensiva: si hay `status`, lo usamos contra OffStatuses. Si no, inferimos actividad
// a partir de valores que solo tienen sentido encendido (brightness/volume/level/temperature).
private fun JsonObject?.readIsOn(): Boolean {
    val status = readString("status")?.lowercase()
    if (status != null) return status !in OffStatuses
    val brightness = readInt("brightness")
    val volume = readInt("volume")
    val level = readInt("level")
    val temperature = readInt("temperature")
    return (brightness != null && brightness > 0) ||
        (volume != null && volume > 0) ||
        (level != null && level > 0) ||
        temperature != null
}

private fun JsonObject?.readLocked(): Boolean? {
    val lock = readString("lock")?.lowercase() ?: return null
    return lock == "locked"
}

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
