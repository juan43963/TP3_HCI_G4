package com.grupo4.smarthome.data.model

object DeviceTypeNames {
    const val LIGHT = "lamp"
    const val AC = "ac"
    const val SPEAKER = "speaker"
    const val VACUUM = "vacuum"
    const val FAUCET = "faucet"
    const val BLINDS = "blinds"
    const val OVEN = "oven"
    const val DOOR = "door"
    const val ALARM = "alarm"
    const val FRIDGE = "refrigerator"

    fun canonical(rawName: String?): String {
        val normalized = rawName?.trim()?.lowercase()?.replace(" ", "")?.replace("_", "")
            ?: return ""
        return when (normalized) {
            "light", "lamp" -> LIGHT
            "ac", "airconditioner", "aircond" -> AC
            "fridge", "refrigerator" -> FRIDGE
            else -> normalized
        }
    }
}
