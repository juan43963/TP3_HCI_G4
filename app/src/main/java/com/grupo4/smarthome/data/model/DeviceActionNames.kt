package com.grupo4.smarthome.data.model

/**
 * Nombres de acciones del catálogo (`GET /devicetypes`) que la lógica de datos necesita conocer, y
 * helpers para resolverlas por tipo. Centraliza el conocimiento de "qué acción es el on/off", "cuál es
 * el slider principal" y "cuál es una consulta de estado", para no esparcir literales por el repo/UI.
 *
 * El renderizado de cada acción (control, label, ícono, opciones discretas) vive en el registry de UI;
 * esto es solo el lado de dominio/datos.
 */
object DeviceActionNames {
    const val SET_BRIGHTNESS = "setBrightness"
    const val SET_TEMPERATURE = "setTemperature"
    const val SET_VOLUME = "setVolume"

    const val LOCK = "lock"
    const val UNLOCK = "unlock"

    // Candidatos de encendido/apagado: se ejecuta el primero que el tipo soporte según el catálogo.
    val ON_CANDIDATES = listOf("turnOn", "open", "play", "start", "armAway", "arm")
    val OFF_CANDIDATES = listOf("turnOff", "close", "pause", "stop", "dock", "disarm")

    /**
     * ¿El tipo tiene un on/off real para mostrar un toggle/estado en la card? Puertas → lock/unlock;
     * el resto necesita al menos un candidato de encendido Y uno de apagado. La heladera (solo
     * temperatura/modo) devuelve false: no es un dispositivo on/off.
     */
    fun supportsToggle(type: DeviceTypeInfo): Boolean {
        if (type.isDoor) return type.supports(LOCK) || type.supports(UNLOCK)
        val hasOn = ON_CANDIDATES.any { type.supports(it) }
        val hasOff = OFF_CANDIDATES.any { type.supports(it) }
        return hasOn && hasOff
    }

    /** Acción del slider primario de la card según el tipo canónico; null = sin slider primario. */
    fun primaryValueAction(canonicalType: String): String? = when (canonicalType) {
        DeviceTypeNames.LIGHT -> SET_BRIGHTNESS
        DeviceTypeNames.AC -> SET_TEMPERATURE
        DeviceTypeNames.SPEAKER -> SET_VOLUME
        else -> null
    }

    /**
     * Solo "obtener estado": se resuelve como un refresco (GET), nunca como PATCH ni como mutación.
     */
    fun isStateRefreshAction(actionName: String): Boolean =
        actionName.equals("getState", ignoreCase = true)

    /**
     * Acción de solo lectura (`getState`, `getPlaylist`, `getStatus`, ...): no muta el estado, por lo que
     * NO debe disparar optimistic update. Puede ejecutarse si está en el catálogo y mostrar su resultado.
     */
    fun isReadOnlyAction(actionName: String): Boolean =
        actionName.trimStart().startsWith("get", ignoreCase = true)
}

/**
 * Valor posicional para ejecutar una acción con parámetros. La UI construye estos (no `JsonElement`); el
 * repository los serializa al `JsonArray` posicional que espera la API (`PATCH /devices/{id}/{action}`).
 */
sealed interface ActionArg {
    data class IntArg(val value: Int) : ActionArg
    data class NumberArg(val value: Double) : ActionArg
    data class StringArg(val value: String) : ActionArg
    data class BoolArg(val value: Boolean) : ActionArg
}

/**
 * Resultado de ejecutar una acción.
 *
 * [device] es el estado refrescado tras la acción. [resultText] trae el cuerpo de respuesta SOLO para
 * acciones de solo lectura ([DeviceActionNames.isReadOnlyAction], p. ej. `getPlaylist`/`getStatus`) que
 * devuelven datos para mostrar; es null para las acciones que únicamente mutan estado. Así la UI puede
 * mostrar el resultado de una consulta sin que se descarte silenciosamente.
 */
data class DeviceActionResult(
    val device: Device,
    val resultText: String? = null,
)
