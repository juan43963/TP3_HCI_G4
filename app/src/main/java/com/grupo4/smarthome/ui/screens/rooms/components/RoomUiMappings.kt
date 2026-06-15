package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionNames
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun roomTypeLabel(type: RoomType): String = stringResource(
    when (type) {
        RoomType.LIVING -> R.string.rooms_type_living
        RoomType.BEDROOM -> R.string.rooms_type_bedroom
        RoomType.KITCHEN -> R.string.rooms_type_kitchen
        RoomType.BATHROOM -> R.string.rooms_type_bathroom
        RoomType.OFFICE -> R.string.rooms_type_office
        RoomType.OTHER -> R.string.rooms_type_other
    },
)

fun roomTypeIcon(type: RoomType): ImageVector = when (type) {
    RoomType.LIVING -> SmartHomeIcons.Living
    RoomType.BEDROOM -> SmartHomeIcons.Bedroom
    RoomType.KITCHEN -> SmartHomeIcons.Kitchen
    RoomType.BATHROOM -> SmartHomeIcons.Bathroom
    RoomType.OFFICE -> SmartHomeIcons.Office
    RoomType.OTHER -> SmartHomeIcons.Rooms
}


@Composable
fun deviceTypeLabel(type: DeviceTypeInfo): String {
    deviceTypeLabelRes(type.canonicalName)?.let { return stringResource(it) }
    val raw = type.name.trim()
    return if (raw.isBlank()) stringResource(R.string.rooms_device_type_generic) else raw.toReadableLabel()
}

private fun String.toReadableLabel(): String =
    replace('_', ' ')
        .replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), " ")
        .lowercase()
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }


@Composable
fun deviceActionLabel(actionName: String): String {
    actionTitleRes(actionName)?.let { return stringResource(it) }
    return knownActionLabelRes(actionName)?.let { stringResource(it) } ?: actionName.toReadableLabel()
}

@Composable
fun deviceActionOptionLabel(value: String): String {
    val res = when (value.lowercase()) {
        "cool" -> R.string.rooms_action_value_cool
        "heat" -> R.string.rooms_action_value_heat
        "fan" -> R.string.rooms_action_value_fan
        "dry" -> R.string.rooms_action_value_dry
        "auto" -> R.string.rooms_action_value_auto
        "silent" -> R.string.rooms_action_value_silent
        "low" -> R.string.rooms_action_value_low
        "mid" -> R.string.rooms_action_value_mid
        "high" -> R.string.rooms_action_value_high
        "default" -> R.string.rooms_action_value_default
        "vacation" -> R.string.rooms_action_value_vacation
        "party" -> R.string.rooms_action_value_party
        "conventional" -> R.string.rooms_action_value_conventional
        "top" -> R.string.rooms_action_value_top
        "bottom" -> R.string.rooms_action_value_bottom
        "off" -> R.string.rooms_action_value_off
        "economic" -> R.string.rooms_action_value_economic
        "full" -> R.string.rooms_action_value_full
        "vacuum" -> R.string.rooms_action_value_vacuum
        "mop" -> R.string.rooms_action_value_mop
        "clasica" -> R.string.rooms_action_value_classical
        "latina" -> R.string.rooms_action_value_latina
        else -> null
    }
    return res?.let { stringResource(it) } ?: value
}

@Composable
fun deviceActionSubtitle(actionName: String): String? {
    val res = when {
        actionName.equals("setTemperature", ignoreCase = true) -> R.string.rooms_action_sub_temperature
        actionName.equals("setHeat", ignoreCase = true) -> R.string.rooms_action_sub_heat
        actionName.equals("setGrill", ignoreCase = true) -> R.string.rooms_action_sub_grill
        actionName.equals("setConvection", ignoreCase = true) -> R.string.rooms_action_sub_convection
        actionName.equals("setMode", ignoreCase = true) -> R.string.rooms_action_sub_mode
        actionName.equals("setGenre", ignoreCase = true) -> R.string.rooms_action_sub_genre
        actionName.equals("setFanSpeed", ignoreCase = true) -> R.string.rooms_action_sub_fan_speed
        actionName.equals("setVerticalSwing", ignoreCase = true) ||
            actionName.equals("setHorizontalSwing", ignoreCase = true) -> R.string.rooms_action_sub_swing
        actionName.equals("setColor", ignoreCase = true) -> R.string.rooms_action_sub_color
        actionName.equals("dispense", ignoreCase = true) -> R.string.rooms_action_sub_dispense
        actionName.lowercase().contains("securitycode") -> R.string.rooms_action_sub_security_code
        actionName.equals("armStay", ignoreCase = true) ||
            actionName.equals("armHome", ignoreCase = true) -> R.string.rooms_action_sub_arm_stay
        actionName.equals("armAway", ignoreCase = true) -> R.string.rooms_action_sub_arm_away
        actionName.equals("disarm", ignoreCase = true) -> R.string.rooms_action_sub_disarm
        else -> null
    }
    return res?.let { stringResource(it) }
}

@StringRes
private fun actionTitleRes(actionName: String): Int? = when {
    actionName.equals("open", ignoreCase = true) -> R.string.routine_action_open
    actionName.equals("close", ignoreCase = true) -> R.string.routine_action_close
    actionName.equals("lock", ignoreCase = true) -> R.string.rooms_device_lock
    actionName.equals("unlock", ignoreCase = true) -> R.string.rooms_device_unlock
    actionName.equals("setVerticalSwing", ignoreCase = true) -> R.string.rooms_action_title_vertical_swing
    actionName.equals("setHorizontalSwing", ignoreCase = true) -> R.string.rooms_action_title_horizontal_swing
    actionName.equals("setFanSpeed", ignoreCase = true) -> R.string.rooms_action_title_fan_speed
    actionName.equals("setMode", ignoreCase = true) -> R.string.rooms_action_title_mode
    actionName.equals("setTemperature", ignoreCase = true) -> R.string.rooms_action_title_temperature
    actionName.equals("setBrightness", ignoreCase = true) -> R.string.rooms_action_title_brightness
    actionName.equals("setVolume", ignoreCase = true) -> R.string.rooms_action_title_volume
    actionName.equals("setColor", ignoreCase = true) -> R.string.rooms_action_title_color
    actionName.equals("setFreezerTemperature", ignoreCase = true) -> R.string.rooms_action_title_freezer_temperature
    actionName.equals("setHeat", ignoreCase = true) -> R.string.rooms_action_title_heat
    actionName.equals("setGrill", ignoreCase = true) -> R.string.rooms_action_title_grill
    actionName.equals("setConvection", ignoreCase = true) -> R.string.rooms_action_title_convection
    actionName.lowercase().contains("securitycode") -> R.string.rooms_action_title_security_code
    actionName.equals("armStay", ignoreCase = true) ||
        actionName.equals("armHome", ignoreCase = true) -> R.string.rooms_action_title_arm_stay
    actionName.equals("armAway", ignoreCase = true) -> R.string.rooms_action_title_arm_away
    actionName.equals("disarm", ignoreCase = true) -> R.string.rooms_action_title_disarm
    actionName.equals("up", ignoreCase = true) -> R.string.rooms_action_title_up
    actionName.equals("down", ignoreCase = true) -> R.string.rooms_action_title_down
    actionName.equals("setLevel", ignoreCase = true) -> R.string.rooms_action_title_level
    actionName.equals("setPosition", ignoreCase = true) -> R.string.rooms_action_title_position
    actionName.equals("setLocation", ignoreCase = true) -> R.string.rooms_action_title_location
    actionName.equals("dock", ignoreCase = true) ||
        actionName.equals("returnToBase", ignoreCase = true) -> R.string.rooms_action_title_return_to_base
    actionName.equals("setGenre", ignoreCase = true) -> R.string.rooms_action_title_genre
    actionName.equals("setIntensity", ignoreCase = true) -> R.string.rooms_action_title_intensity
    actionName.equals("dispense", ignoreCase = true) -> R.string.rooms_action_title_dispense
    actionName.equals("resume", ignoreCase = true) -> R.string.rooms_action_title_resume
    actionName.equals("nextSong", ignoreCase = true) -> R.string.rooms_action_title_next_song
    actionName.equals("previousSong", ignoreCase = true) -> R.string.rooms_action_title_previous_song
    actionName.equals("getPlaylist", ignoreCase = true) -> R.string.rooms_action_title_get_playlist
    actionName.equals("getStatus", ignoreCase = true) ||
        actionName.equals("getState", ignoreCase = true) -> R.string.rooms_action_title_get_status
    actionName.lowercase().startsWith("get") -> R.string.rooms_action_title_get_info
    else -> null
}

@StringRes
private fun knownActionLabelRes(actionName: String): Int? = when (actionName) {
    "turnOn" -> R.string.history_action_turn_on
    "turnOff" -> R.string.history_action_turn_off
    "open" -> R.string.history_action_open
    "close" -> R.string.history_action_close
    "lock" -> R.string.history_action_lock
    "unlock" -> R.string.history_action_unlock
    "play" -> R.string.history_action_play
    "pause" -> R.string.history_action_pause
    "stop" -> R.string.history_action_stop
    "start" -> R.string.history_action_start
    "setBrightness" -> R.string.history_action_set_brightness
    "setColor" -> R.string.history_action_set_color
    "setTemperature" -> R.string.history_action_set_temperature
    "setVolume" -> R.string.history_action_set_volume
    "setMode" -> R.string.history_action_set_mode
    else -> null
}

@StringRes
private fun deviceTypeLabelRes(canonicalName: String): Int? = when (canonicalName) {
    DeviceTypeNames.VACUUM -> R.string.rooms_device_type_vacuum
    DeviceTypeNames.SPEAKER -> R.string.rooms_device_type_speaker
    DeviceTypeNames.FAUCET -> R.string.rooms_device_type_faucet
    DeviceTypeNames.BLINDS -> R.string.rooms_device_type_blinds
    DeviceTypeNames.LIGHT -> R.string.rooms_device_type_light
    DeviceTypeNames.OVEN -> R.string.rooms_device_type_oven
    DeviceTypeNames.AC -> R.string.rooms_device_type_ac
    DeviceTypeNames.DOOR -> R.string.rooms_device_type_door
    DeviceTypeNames.ALARM -> R.string.rooms_device_type_alarm
    DeviceTypeNames.FRIDGE -> R.string.rooms_device_type_fridge
    else -> null
}

fun deviceTypeIcon(type: DeviceTypeInfo): ImageVector = when (type.canonicalName) {
    DeviceTypeNames.VACUUM -> SmartHomeIcons.Vacuum
    DeviceTypeNames.SPEAKER -> SmartHomeIcons.Speaker
    DeviceTypeNames.FAUCET -> SmartHomeIcons.Faucet
    DeviceTypeNames.BLINDS -> SmartHomeIcons.Blinds
    DeviceTypeNames.LIGHT -> SmartHomeIcons.Light
    DeviceTypeNames.OVEN -> SmartHomeIcons.Oven
    DeviceTypeNames.AC -> SmartHomeIcons.AirConditioner
    DeviceTypeNames.DOOR -> SmartHomeIcons.Door
    DeviceTypeNames.ALARM -> SmartHomeIcons.Alarm
    DeviceTypeNames.FRIDGE -> SmartHomeIcons.Fridge
    else -> SmartHomeIcons.Devices
}

@Composable
fun deviceCategoryLabel(type: DeviceTypeInfo): String = stringResource(
    when (type.canonicalName) {
        DeviceTypeNames.LIGHT -> R.string.rooms_device_category_lighting
        DeviceTypeNames.ALARM -> R.string.rooms_device_category_security
        DeviceTypeNames.DOOR -> R.string.rooms_device_category_access
        DeviceTypeNames.BLINDS -> R.string.rooms_device_category_window
        DeviceTypeNames.FAUCET -> R.string.rooms_device_category_water
        DeviceTypeNames.AC -> R.string.rooms_device_category_climate
        DeviceTypeNames.SPEAKER -> R.string.rooms_device_category_audio
        DeviceTypeNames.OVEN, DeviceTypeNames.FRIDGE -> R.string.rooms_device_category_kitchen
        DeviceTypeNames.VACUUM -> R.string.rooms_device_category_cleaning
        else -> R.string.rooms_device_category_other
    },
)

data class DeviceStatusUi(@StringRes val labelRes: Int, val active: Boolean)

fun deviceStatuses(device: Device): List<DeviceStatusUi> {
    if (device.type.canonicalName == DeviceTypeNames.ALARM) {
        return when (device.state.status) {
            "armedstay", "armedhome" ->
                listOf(DeviceStatusUi(R.string.rooms_status_alarm_home, active = true))
            "armedaway", "armed" ->
                listOf(DeviceStatusUi(R.string.rooms_status_alarm_regular, active = true))
            "disarmed" ->
                listOf(DeviceStatusUi(R.string.rooms_status_alarm_disarmed, active = false))
            else -> emptyList()
        }
    }
    if (!DeviceActionNames.supportsToggle(device.type)) return emptyList()
    return when (device.type.canonicalName) {
        DeviceTypeNames.DOOR -> buildList {
            when (device.state.status) {
                "open", "opened" -> add(DeviceStatusUi(R.string.rooms_status_open, active = true))
                "opening" -> add(DeviceStatusUi(R.string.rooms_status_opening, active = true))
                "closed" -> add(DeviceStatusUi(R.string.rooms_status_closed, active = false))
                "closing" -> add(DeviceStatusUi(R.string.rooms_status_closing, active = false))
            }
            when (device.state.string("lock")?.lowercase()) {
                "locked" -> add(DeviceStatusUi(R.string.rooms_status_locked, active = true))
                "unlocked" -> add(DeviceStatusUi(R.string.rooms_status_unlocked, active = false))
            }
        }
        DeviceTypeNames.BLINDS -> when (device.state.status) {
            "closing" -> listOf(DeviceStatusUi(R.string.rooms_status_closing, active = false))
            "opening" -> listOf(DeviceStatusUi(R.string.rooms_status_opening, active = true))
            "closed" -> listOf(DeviceStatusUi(R.string.rooms_status_closed, active = false))
            else -> listOf(DeviceStatusUi(R.string.rooms_status_open, active = device.isOn))
        }
        DeviceTypeNames.FAUCET ->
            listOf(
                DeviceStatusUi(
                    if (device.isOn) R.string.rooms_status_faucet_open else R.string.rooms_status_faucet_closed,
                    device.isOn,
                ),
            )
        else ->
            listOf(DeviceStatusUi(if (device.isOn) R.string.rooms_status_on else R.string.rooms_status_off, device.isOn))
    }
}

fun deviceStatus(device: Device): DeviceStatusUi? = deviceStatuses(device).firstOrNull()
