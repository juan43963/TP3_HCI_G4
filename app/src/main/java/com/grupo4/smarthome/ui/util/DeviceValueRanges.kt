package com.grupo4.smarthome.ui.util

/**
 * Rangos de valores por tipo de dispositivo. Única fuente de verdad compartida
 * entre el slider de acciones (DeviceActionRegistry) y el update optimista del
 * estado (RoomsUiState), para que no vuelvan a divergir.
 */
object DeviceValueRanges {
    val AC_TEMPERATURE = 18..38
    val LIGHT_BRIGHTNESS = 0..100
    val SPEAKER_VOLUME = 0..10
    val FREEZER_TEMPERATURE = -20..-8
    val OVEN_TEMPERATURE = 90..230
    val FRIDGE_TEMPERATURE = 2..8
    val PERCENT = 0..100
}
