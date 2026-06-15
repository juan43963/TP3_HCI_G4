package com.grupo4.smarthome.ui.screens.rooms.components

import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionNames
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.ui.util.DeviceValueRanges

object DeviceActionRegistry {

    private val AC_MODES = listOf("cool", "heat", "fan")
    private val VACUUM_MODES = listOf("vacuum", "mop")
    private val FRIDGE_MODES = listOf("default", "vacation", "party")
    private val VERTICAL_SWINGS = listOf("auto", "22", "45", "67", "90")
    private val HORIZONTAL_SWINGS = listOf("auto", "-90", "-45", "0", "45", "90")
    private val FAN_SPEEDS = listOf("auto", "25", "50", "75", "100")
    private val OVEN_HEAT = listOf("conventional", "top", "bottom")
    private val OVEN_GRILL = listOf("off", "economic", "full")
    private val OVEN_CONVECTION = listOf("off", "economic", "conventional")
    private val GENRES = listOf("clasica", "country", "dance", "latina", "pop", "rock")

    fun controlFor(action: DeviceActionInfo, canonicalType: String): DeviceActionControl {
        val name = action.name
        val params = action.params
        // supportedValues del catálogo si la API los trae; si no, lista hardcodeada (fallback).
        fun choiceValues(fallback: List<String>): List<String> =
            params.firstOrNull()?.supportedValues?.takeIf { it.isNotEmpty() } ?: fallback
        return when {
            DeviceActionNames.isReadOnlyAction(name) -> DeviceActionControl.Trigger

            name.normalizedAction().contains("securitycode") ->
                DeviceActionControl.Code(fields = 2)

            canonicalType == DeviceTypeNames.ALARM ->
                DeviceActionControl.Code(fields = 1)

            name.equals("setColor", ignoreCase = true) -> DeviceActionControl.ColorPicker

            name.equals("dispense", ignoreCase = true) -> DeviceActionControl.Dispense

            name.equals("setLocation", ignoreCase = true) -> DeviceActionControl.RoomPicker

            name.equals("setMode", ignoreCase = true) -> DeviceActionControl.Choice(choiceValues(modesFor(canonicalType)))
            name.equals("setGenre", ignoreCase = true) -> DeviceActionControl.Choice(choiceValues(GENRES))
            name.equals("setHeat", ignoreCase = true) -> DeviceActionControl.Choice(choiceValues(OVEN_HEAT))
            name.equals("setGrill", ignoreCase = true) -> DeviceActionControl.Choice(choiceValues(OVEN_GRILL))
            name.equals("setConvection", ignoreCase = true) -> DeviceActionControl.Choice(choiceValues(OVEN_CONVECTION))
            name.equals("setVerticalSwing", ignoreCase = true) -> DeviceActionControl.Choice(choiceValues(VERTICAL_SWINGS))
            name.equals("setHorizontalSwing", ignoreCase = true) -> DeviceActionControl.Choice(choiceValues(HORIZONTAL_SWINGS))
            name.equals("setFanSpeed", ignoreCase = true) -> DeviceActionControl.Choice(choiceValues(FAN_SPEEDS))

            isNumeric(action) -> {
                val param = params.first()
                val (min, max) = numericRange(name, canonicalType, param.name)
                DeviceActionControl.Numeric(
                    fallbackMin = param.min ?: min,
                    fallbackMax = param.max ?: max,
                )
            }

            params.isEmpty() -> DeviceActionControl.Trigger

            else -> DeviceActionControl.Generic
        }
    }

    private fun modesFor(canonicalType: String): List<String> = when (canonicalType) {
        DeviceTypeNames.AC -> AC_MODES
        DeviceTypeNames.VACUUM -> VACUUM_MODES
        DeviceTypeNames.FRIDGE -> FRIDGE_MODES
        else -> emptyList()
    }

    private fun isNumeric(action: DeviceActionInfo): Boolean {
        val param = action.params.singleOrNull() ?: return false
        return param.type.lowercase() in setOf("integer", "int", "number", "float", "double")
    }

    private fun numericRange(actionName: String, canonicalType: String, paramName: String): Pair<Int, Int> {
        val n = actionName.lowercase()
        val p = paramName.lowercase()
        fun has(token: String) = token in n || token in p
        val range = when {
            has("volume") -> DeviceValueRanges.SPEAKER_VOLUME
            has("brightness") -> DeviceValueRanges.LIGHT_BRIGHTNESS
            has("freezer") -> DeviceValueRanges.FREEZER_TEMPERATURE
            has("temperature") -> when (canonicalType) {
                DeviceTypeNames.AC -> DeviceValueRanges.AC_TEMPERATURE
                DeviceTypeNames.OVEN -> DeviceValueRanges.OVEN_TEMPERATURE
                DeviceTypeNames.FRIDGE -> DeviceValueRanges.FRIDGE_TEMPERATURE
                else -> DeviceValueRanges.PERCENT
            }
            has("position") || has("level") || has("intensity") || has("dispense") -> DeviceValueRanges.PERCENT
            else -> DeviceValueRanges.PERCENT
        }
        return range.first to range.last
    }

    private fun String.normalizedAction(): String = lowercase().replace("_", "").replace("-", "")
}

sealed interface DeviceActionControl {
    data object Trigger : DeviceActionControl

    data class Numeric(val fallbackMin: Int, val fallbackMax: Int) : DeviceActionControl

    data class Choice(val suggestions: List<String>) : DeviceActionControl

    data object ColorPicker : DeviceActionControl

    data object Dispense : DeviceActionControl

    data object RoomPicker : DeviceActionControl

    data class Code(val length: Int = 4, val fields: Int = 1) : DeviceActionControl

    data object Generic : DeviceActionControl
}

data class RoomOption(val id: String, val name: String)
