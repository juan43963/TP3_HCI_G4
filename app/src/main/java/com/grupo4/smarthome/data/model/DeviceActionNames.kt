package com.grupo4.smarthome.data.model

object DeviceActionNames {
    const val SET_BRIGHTNESS = "setBrightness"
    const val SET_TEMPERATURE = "setTemperature"
    const val SET_VOLUME = "setVolume"

    const val LOCK = "lock"
    const val UNLOCK = "unlock"

    val ON_CANDIDATES = listOf("turnOn", "open", "play", "start", "armAway", "arm", "up")
    val OFF_CANDIDATES = listOf("turnOff", "close", "pause", "stop", "dock", "disarm", "down")

    fun supportsToggle(type: DeviceTypeInfo): Boolean {
        if (type.isDoor) return type.supports(LOCK) || type.supports(UNLOCK)
        if (type.canonicalName == DeviceTypeNames.ALARM) return false
        val hasOn = ON_CANDIDATES.any { type.supports(it) }
        val hasOff = OFF_CANDIDATES.any { type.supports(it) }
        return hasOn && hasOff
    }

    fun gatesConfigurationWhenOff(type: DeviceTypeInfo): Boolean = when (type.canonicalName) {
        DeviceTypeNames.LIGHT, DeviceTypeNames.AC, DeviceTypeNames.OVEN, DeviceTypeNames.FAUCET -> true
        else -> false
    }

    fun primaryValueAction(canonicalType: String): String? = when (canonicalType) {
        DeviceTypeNames.LIGHT -> SET_BRIGHTNESS
        DeviceTypeNames.AC -> SET_TEMPERATURE
        DeviceTypeNames.SPEAKER -> SET_VOLUME
        else -> null
    }

    fun isStateRefreshAction(actionName: String): Boolean =
        actionName.equals("getState", ignoreCase = true)

    fun isReadOnlyAction(actionName: String): Boolean =
        actionName.trimStart().startsWith("get", ignoreCase = true)
}

sealed interface ActionArg {
    data class IntArg(val value: Int) : ActionArg
    data class NumberArg(val value: Double) : ActionArg
    data class StringArg(val value: String) : ActionArg
    data class BoolArg(val value: Boolean) : ActionArg
}

data class DeviceActionResult(
    val device: Device,
    val resultText: String? = null,
)
