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
    // AC: valores reales de la spec de la cátedra (ventilación/frío/calor; el estado los guarda como string).
    private val AC_MODES = listOf("cool", "heat", "fan")
    private val VACUUM_MODES = listOf("vacuum", "mop")
    private val FRIDGE_MODES = listOf("default", "vacation", "party")
    // Aspas verticales y horizontales tienen valores DISTINTOS según la spec.
    private val VERTICAL_SWINGS = listOf("auto", "22", "45", "67", "90")
    private val HORIZONTAL_SWINGS = listOf("auto", "-90", "-45", "0", "45", "90")
    private val FAN_SPEEDS = listOf("auto", "25", "50", "75", "100")
    private val OVEN_HEAT = listOf("conventional", "top", "bottom")
    private val OVEN_GRILL = listOf("off", "economic", "full")
    private val OVEN_CONVECTION = listOf("off", "economic", "conventional")
    private val GENRES = listOf("clasica", "country", "dance", "latina", "pop", "rock")

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

            // Cambiar el código siempre recibe [código actual, código nuevo], aunque el catálogo declare
            // mal la cantidad de parámetros.
            name.normalizedAction().contains("securitycode") ->
                DeviceActionControl.Code(fields = 2)

            // Armar/desarmar siempre recibe un único código. La API lo exige incluso cuando el catálogo
            // declara cero parámetros.
            canonicalType == DeviceTypeNames.ALARM ->
                DeviceActionControl.Code(fields = 1)

            // Color de la lámpara: selector con presets + input editable.
            name.equals("setColor", ignoreCase = true) -> DeviceActionControl.ColorPicker

            // Dispensar (grifo): cantidad + unidad.
            name.equals("dispense", ignoreCase = true) -> DeviceActionControl.Dispense

            // Cambiar ubicación (aspiradora): recibe el id de una habitación → selector de rooms.
            name.equals("setLocation", ignoreCase = true) -> DeviceActionControl.RoomPicker

            // Discretos editables + sugerencias (grill/convección también son modos, no on/off duros).
            name.equals("setMode", ignoreCase = true) -> DeviceActionControl.Choice(modesFor(canonicalType))
            name.equals("setGenre", ignoreCase = true) -> DeviceActionControl.Choice(GENRES)
            name.equals("setHeat", ignoreCase = true) -> DeviceActionControl.Choice(OVEN_HEAT)
            name.equals("setGrill", ignoreCase = true) -> DeviceActionControl.Choice(OVEN_GRILL)
            name.equals("setConvection", ignoreCase = true) -> DeviceActionControl.Choice(OVEN_CONVECTION)
            name.equals("setVerticalSwing", ignoreCase = true) -> DeviceActionControl.Choice(VERTICAL_SWINGS)
            name.equals("setHorizontalSwing", ignoreCase = true) -> DeviceActionControl.Choice(HORIZONTAL_SWINGS)
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
                DeviceTypeNames.AC -> 18 to 38
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

    /** Selector de habitación (setLocation, aspiradora): chips con las rooms; envía el id de la elegida. */
    data object RoomPicker : DeviceActionControl

    /** Inputs validados de código (no se loguean). [fields] = cantidad de campos (p. ej. actual + nuevo). */
    data class Code(val length: Int = 4, val fields: Int = 1) : DeviceActionControl

    /** Fallback: param(s) desconocidos / multi-param -> inputs de texto libres por cada param del catálogo. */
    data object Generic : DeviceActionControl
}

/** Opción liviana de habitación para el selector de `setLocation` (id real + nombre visible). */
data class RoomOption(val id: String, val name: String)
