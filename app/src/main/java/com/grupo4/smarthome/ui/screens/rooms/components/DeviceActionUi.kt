package com.grupo4.smarthome.ui.screens.rooms.components

import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionNames
import com.grupo4.smarthome.data.model.DeviceTypeNames

/**
 * Registry app-side que describe CÓMO se renderiza/ejecuta cada acción del catálogo en la card.
 *
 * El catálogo (`GET /devicetypes`) dice qué acciones existen y el nombre/tipo de sus params, pero NO los
 * valores discretos permitidos (modos, géneros, fuentes de calor, grill, convección, swings, fanSpeed) ni
 * los rangos numéricos. Por eso para discretos usamos [DeviceActionControl.Choice] (editable + sugerencias,
 * nunca fuerza un 400) y para numéricos sin min/max del catálogo aplicamos rangos fallback por
 * actionName + tipo + paramName. Todo editable: si la API acepta otros valores, no los bloqueamos.
 *
 * Las listas/rangos sugeridos viven todos acá (único lugar para refinar cuando tengamos los sets reales).
 */
object DeviceActionRegistry {

    // --- Valores SUGERIDOS (no exhaustivos: la API no los documenta). Único lugar para refinar. ---
    private val AC_MODES = listOf("cool", "heat", "fan", "dry", "auto")
    private val VACUUM_MODES = listOf("vacuum", "mop")
    private val FRIDGE_MODES = listOf("default", "vacation", "party")
    private val SWINGS = listOf("auto", "0", "22", "45", "67", "90")
    private val FAN_SPEEDS = listOf("auto", "silent", "low", "mid", "high")
    private val OVEN_HEAT = listOf("conventional", "top", "bottom")
    private val OVEN_GRILL = listOf("off", "economic", "full")
    private val OVEN_CONVECTION = listOf("off", "economic", "conventional")
    private val GENRES = listOf("pop", "rock", "jazz", "classical", "dance", "country")

    /**
     * Resuelve el control para una acción dada, según el tipo canónico (los modos discretos y los rangos
     * numéricos dependen del tipo) y los params del catálogo.
     */
    fun controlFor(action: DeviceActionInfo, canonicalType: String): DeviceActionControl {
        val name = action.name
        val params = action.params
        return when {
            // Solo lectura (getState/getPlaylist/getStatus): botón que dispara y muestra resultado.
            DeviceActionNames.isReadOnlyAction(name) -> DeviceActionControl.Trigger

            // Acciones de alarma (armar/desarmar) y de código de seguridad: requieren el código. La API
            // valida la cantidad de params de forma ESTRICTA y exige >=1 incluso cuando el catálogo
            // declara 0 (p. ej. armAway devuelve: "number of parameters '0' for action 'armAway' is
            // invalid"). Por eso forzamos el control de Código con al menos un campo.
            name.normalizedAction().contains("securitycode") || canonicalType == DeviceTypeNames.ALARM ->
                DeviceActionControl.Code(fields = params.size.coerceAtLeast(1))

            // Color de la lámpara: selector con presets + input editable.
            name.equals("setColor", ignoreCase = true) -> DeviceActionControl.ColorPicker

            // Dispensar (grifo): cantidad + unidad.
            name.equals("dispense", ignoreCase = true) -> DeviceActionControl.Dispense

            // Discretos editables + sugerencias (grill/convección también son modos, no on/off duros).
            name.equals("setMode", ignoreCase = true) -> DeviceActionControl.Choice(modesFor(canonicalType))
            name.equals("setGenre", ignoreCase = true) -> DeviceActionControl.Choice(GENRES)
            name.equals("setHeat", ignoreCase = true) -> DeviceActionControl.Choice(OVEN_HEAT)
            name.equals("setGrill", ignoreCase = true) -> DeviceActionControl.Choice(OVEN_GRILL)
            name.equals("setConvection", ignoreCase = true) -> DeviceActionControl.Choice(OVEN_CONVECTION)
            name.equals("setVerticalSwing", ignoreCase = true) ||
                name.equals("setHorizontalSwing", ignoreCase = true) -> DeviceActionControl.Choice(SWINGS)
            name.equals("setFanSpeed", ignoreCase = true) -> DeviceActionControl.Choice(FAN_SPEEDS)

            // Numérico de un solo parámetro: rango del catálogo (min/max) o fallback por tipo+param.
            isNumeric(action) -> {
                val param = params.first()
                val (min, max) = numericRange(name, canonicalType, param.name)
                DeviceActionControl.Numeric(
                    fallbackMin = param.min ?: min,
                    fallbackMax = param.max ?: max,
                )
            }

            // Sin parámetros: botón simple (play, pause, stop, returnToBase, nextSong, ...).
            params.isEmpty() -> DeviceActionControl.Trigger

            // Fallback: param(s) desconocidos / multi-param (p. ej. dispense) -> inputs de texto libres.
            else -> DeviceActionControl.Generic
        }
    }

    private fun modesFor(canonicalType: String): List<String> = when (canonicalType) {
        DeviceTypeNames.AC -> AC_MODES
        DeviceTypeNames.VACUUM -> VACUUM_MODES
        DeviceTypeNames.FRIDGE -> FRIDGE_MODES
        else -> emptyList()
    }

    // Numérico = exactamente un param cuyo tipo declarado es entero/número (según el catálogo).
    private fun isNumeric(action: DeviceActionInfo): Boolean {
        val param = action.params.singleOrNull() ?: return false
        return param.type.lowercase() in setOf("integer", "int", "number", "float", "double")
    }

    /**
     * Rango fallback cuando el catálogo no trae min/max, resuelto por actionName + tipo canónico + nombre
     * del param. (El freezer se chequea antes que la temperatura genérica porque su param también la contiene.)
     */
    private fun numericRange(actionName: String, canonicalType: String, paramName: String): Pair<Int, Int> {
        val n = actionName.lowercase()
        val p = paramName.lowercase()
        fun has(token: String) = token in n || token in p
        return when {
            has("volume") -> 0 to 10
            has("brightness") -> 0 to 100
            has("freezer") -> -20 to -8
            has("temperature") -> when (canonicalType) {
                DeviceTypeNames.AC -> 16 to 30
                DeviceTypeNames.OVEN -> 90 to 230
                DeviceTypeNames.FRIDGE -> 2 to 8
                else -> 0 to 100
            }
            has("position") || has("level") || has("intensity") || has("dispense") -> 0 to 100
            else -> 0 to 100
        }
    }

    // Normaliza el nombre de acción para matchear alias (quita separadores y baja a minúsculas).
    private fun String.normalizedAction(): String = lowercase().replace("_", "").replace("-", "")
}

/** Cómo renderizar/ejecutar una acción del catálogo en la card. */
sealed interface DeviceActionControl {
    /** Botón simple sin parámetros (play, pause, stop, returnToBase, nextSong, getPlaylist, ...). */
    data object Trigger : DeviceActionControl

    /** Slider/stepper para un parámetro numérico; rango efectivo = catálogo (min/max) o estos fallbacks. */
    data class Numeric(val fallbackMin: Int, val fallbackMax: Int) : DeviceActionControl

    /** Param discreto editable + chips sugeridos (mode, genre, heat, grill, convection, swings, fanSpeed). */
    data class Choice(val suggestions: List<String>) : DeviceActionControl

    /** Selector de color (setColor): presets + input editable de color. */
    data object ColorPicker : DeviceActionControl

    /** Dispensar (grifo): cantidad numérica (+ cantidades rápidas) y unidad. */
    data object Dispense : DeviceActionControl

    /** Inputs validados de código (no se loguean). [fields] = cantidad de campos (p. ej. actual + nuevo). */
    data class Code(val length: Int = 4, val fields: Int = 1) : DeviceActionControl

    /** Fallback: param(s) desconocidos / multi-param -> inputs de texto libres por cada param del catálogo. */
    data object Generic : DeviceActionControl
}
