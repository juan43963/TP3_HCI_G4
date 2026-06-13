package com.grupo4.smarthome.data.model

/**
 * Nombres canónicos de los tipos de dispositivo del catálogo (`GET /devicetypes`), en minúsculas.
 *
 * Única fuente de verdad para comparar contra `type.name`: ninguna otra parte del código debe comparar
 * strings de tipo a mano. [canonical] normaliza los alias conocidos del back-end (p. ej. `light`→`lamp`,
 * `fridge`→`refrigerator`) a estas claves estables; el resto del proyecto (íconos, labels, controles,
 * categorías de historial/notificaciones) mapea siempre sobre el resultado de [canonical].
 */
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

    /** Normaliza el `name` crudo del catálogo a una clave canónica estable (ver constantes arriba). */
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
