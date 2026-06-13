package com.grupo4.smarthome.data.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

// Estados de la API que representan "apagado/inactivo". Cualquier otro status se interpreta como encendido.
private val OffStatuses = setOf(
    "off", "closed", "stopped", "disarmed", "docked", "idle", "paused", "standby",
)

/**
 * Estado dinámico de un dispositivo tal como lo devuelve la API: un mapa clave→valor cuyo contenido
 * varía por tipo (status, brightness, volume, mode, position, genre, temperature, lock, ...).
 *
 * Envuelve el mapa crudo (`raw`) para que la UI NUNCA toque `JsonElement` directamente: el resto del
 * código accede vía los helpers tipados ([int]/[string]/[bool]) o los accessors de conveniencia de
 * [Device]. Detalle técnico: se modela como `value class` para evitar una capa de indirección extra.
 */
@JvmInline
value class DeviceState(val raw: Map<String, JsonElement> = emptyMap()) {

    private fun primitive(key: String): JsonPrimitive? = raw[key] as? JsonPrimitive

    /** Entero del estado (p. ej. "brightness", "volume", "temperature"), o null si no aplica. */
    fun int(key: String): Int? = primitive(key)?.intOrNull

    /** Booleano del estado, o null si la clave no es un booleano. */
    fun bool(key: String): Boolean? = primitive(key)?.booleanOrNull

    /** String del estado (solo si el valor es realmente un string JSON), o null. */
    fun string(key: String): String? = primitive(key)?.takeIf { it.isString }?.content

    /** Status normalizado en minúsculas, si existe. */
    val status: String? get() = string("status")?.lowercase()

    /**
     * Infiere si el dispositivo está encendido/activo. Si hay `status`, lo evalúa contra [OffStatuses].
     * Si no hay status, cae a indicadores que solo tienen sentido con el equipo encendido
     * (brightness/volume/level > 0).
     *
     * `temperature` se excluye a propósito: es una lectura de régimen permanente (una heladera siempre
     * reporta temperatura aunque no sea un dispositivo on/off), así que tratarla como "encendido" daría
     * falsos positivos. Los tipos cuyo encendido dependa de temperatura deben traer `status`.
     */
    fun isOn(): Boolean {
        status?.let { return it !in OffStatuses }
        val brightness = int("brightness")
        val volume = int("volume")
        val level = int("level")
        return (brightness != null && brightness > 0) ||
            (volume != null && volume > 0) ||
            (level != null && level > 0)
    }

    /** Estado de cerradura (puertas): true=locked, false=unlocked, null si no aplica. */
    fun locked(): Boolean? {
        val lock = string("lock")?.lowercase() ?: return null
        return lock == "locked"
    }

    /** Copia con `key` actualizada. Privado: la UI usa los writers tipados de abajo, no JsonElement. */
    private fun with(key: String, value: JsonElement): DeviceState = DeviceState(raw + (key to value))

    // Writers tipados para optimistic updates de la UI (sin tocar JsonElement directamente).
    fun withInt(key: String, value: Int): DeviceState = with(key, JsonPrimitive(value))
    fun withString(key: String, value: String): DeviceState = with(key, JsonPrimitive(value))
    fun withBool(key: String, value: Boolean): DeviceState = with(key, JsonPrimitive(value))

    companion object {
        val Empty = DeviceState(emptyMap())
    }
}
