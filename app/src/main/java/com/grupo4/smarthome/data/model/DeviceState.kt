package com.grupo4.smarthome.data.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

private val OffStatuses = setOf(
    "off", "closed", "stopped", "disarmed", "docked", "idle", "paused", "standby",
)

@JvmInline
value class DeviceState(val raw: Map<String, JsonElement> = emptyMap()) {

    private fun primitive(key: String): JsonPrimitive? = raw[key] as? JsonPrimitive

    fun int(key: String): Int? = primitive(key)?.intOrNull

    fun bool(key: String): Boolean? = primitive(key)?.booleanOrNull

    fun string(key: String): String? = primitive(key)?.takeIf { it.isString }?.content

    val status: String? get() = string("status")?.lowercase()

    fun isOn(): Boolean {
        status?.let { return it !in OffStatuses }
        val brightness = int("brightness")
        val volume = int("volume")
        val level = int("level")
        return (brightness != null && brightness > 0) ||
            (volume != null && volume > 0) ||
            (level != null && level > 0)
    }

    fun locked(): Boolean? {
        val lock = string("lock")?.lowercase() ?: return null
        return lock == "locked"
    }

    private fun with(key: String, value: JsonElement): DeviceState = DeviceState(raw + (key to value))

    fun withInt(key: String, value: Int): DeviceState = with(key, JsonPrimitive(value))
    fun withString(key: String, value: String): DeviceState = with(key, JsonPrimitive(value))
    fun withBool(key: String, value: Boolean): DeviceState = with(key, JsonPrimitive(value))

    companion object {
        val Empty = DeviceState(emptyMap())
    }
}
