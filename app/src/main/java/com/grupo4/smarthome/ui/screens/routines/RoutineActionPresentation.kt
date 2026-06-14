package com.grupo4.smarthome.ui.screens.routines

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.grupo4.smarthome.R

@StringRes
internal fun routineActionLabelRes(actionName: String): Int? {
    val normalized = actionName
        .lowercase()
        .replace("_", "")
        .replace("-", "")

    if ("securitycode" in normalized) {
        return R.string.routine_action_set_security_code
    }

    return when (normalized) {
        "turnon" -> R.string.routine_action_turn_on
        "turnoff" -> R.string.routine_action_turn_off
        "open" -> R.string.routine_action_open
        "close" -> R.string.routine_action_close
        "lock" -> R.string.routine_action_lock
        "unlock" -> R.string.routine_action_unlock
        "play" -> R.string.routine_action_play
        "pause" -> R.string.routine_action_pause
        "stop" -> R.string.routine_action_stop
        "start" -> R.string.routine_action_start
        "resume" -> R.string.routine_action_resume
        "nextsong" -> R.string.routine_action_next_song
        "previoussong" -> R.string.routine_action_previous_song
        "dock" -> R.string.routine_action_dock
        "returntobase" -> R.string.routine_action_return_to_base
        "setbrightness" -> R.string.routine_action_set_brightness
        "setcolor" -> R.string.routine_action_set_color
        "settemperature" -> R.string.routine_action_set_temperature
        "setfreezertemperature" -> R.string.routine_action_set_freezer_temperature
        "setvolume" -> R.string.routine_action_set_volume
        "setmode" -> R.string.routine_action_set_mode
        "setgenre" -> R.string.routine_action_set_genre
        "setheat" -> R.string.routine_action_set_heat
        "setgrill" -> R.string.routine_action_set_grill
        "setconvection" -> R.string.routine_action_set_convection
        "setverticalswing" -> R.string.routine_action_set_vertical_swing
        "sethorizontalswing" -> R.string.routine_action_set_horizontal_swing
        "setfanspeed" -> R.string.routine_action_set_fan_speed
        "setlevel" -> R.string.routine_action_set_level
        "setposition" -> R.string.routine_action_set_position
        "setintensity" -> R.string.routine_action_set_intensity
        "dispense" -> R.string.routine_action_dispense
        "arm" -> R.string.routine_action_arm
        "armstay", "armhome" -> R.string.routine_action_arm_stay
        "armaway" -> R.string.routine_action_arm_away
        "disarm" -> R.string.routine_action_disarm
        "getstate", "getstatus" -> R.string.routine_action_get_state
        "getplaylist" -> R.string.routine_action_get_playlist
        else -> if (normalized.startsWith("get")) {
            R.string.routine_action_get_information
        } else {
            null
        }
    }
}

@Composable
internal fun routineActionLabel(actionName: String): String =
    routineActionLabelRes(actionName)?.let { stringResource(it) }
        ?: actionName.toReadableActionFallback()

@Composable
internal fun routineActionDescription(actionName: String, params: List<String>): String {
    val label = routineActionLabel(actionName)
    val formattedParams = params.joinToString(", ")
    return if (formattedParams.isBlank()) label else "$label • $formattedParams"
}

@Composable
internal fun routineActionCardLabel(action: RoutineActionItemUi): String {
    val deviceName = action.deviceName.ifBlank {
        stringResource(R.string.create_routine_device_generic)
    }
    return "${routineActionLabel(action.actionName)} $deviceName"
}

private fun String.toReadableActionFallback(): String =
    replace('_', ' ')
        .replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), " ")
        .trim()
        .lowercase()
        .replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase() else character.toString()
        }
