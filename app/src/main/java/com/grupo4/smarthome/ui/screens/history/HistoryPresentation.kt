package com.grupo4.smarthome.ui.screens.history

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.data.model.Room
import java.time.Instant

private data class DeviceInfo(
    val name: String,
    val roomName: String,
    val type: HistoryDeviceType,
)


fun List<DeviceLog>.toHistoryEntries(
    context: Context,
    rooms: List<Room>,
): List<HistoryEntryUi> {
    val lookup = buildDeviceLookup(rooms)
    return map { log ->
        val info = lookup[log.deviceId]
        HistoryEntryUi(
            id = log.id,
            action = actionLabel(context, log.actionName),
            deviceName = info?.name ?: context.getString(R.string.history_unknown_device),
            roomName = info?.roomName ?: context.getString(R.string.history_unknown_room),
            deviceType = info?.type ?: HistoryDeviceType.OTHER,
            timeLabel = relativeTime(context, log.timestamp),
        )
    }
}

private fun buildDeviceLookup(rooms: List<Room>): Map<String, DeviceInfo> =
    buildMap {
        rooms.forEach { room ->
            room.devices.forEach { device ->
                put(device.id, DeviceInfo(device.name, room.name, device.type.toHistoryType()))
            }
        }
    }

private fun DeviceTypeInfo.toHistoryType(): HistoryDeviceType =
    when (canonicalName) {
        DeviceTypeNames.VACUUM -> HistoryDeviceType.VACUUM
        DeviceTypeNames.SPEAKER -> HistoryDeviceType.SPEAKER
        DeviceTypeNames.FAUCET -> HistoryDeviceType.FAUCET
        DeviceTypeNames.BLINDS -> HistoryDeviceType.BLINDS
        DeviceTypeNames.LIGHT -> HistoryDeviceType.LIGHT
        DeviceTypeNames.OVEN -> HistoryDeviceType.OVEN
        DeviceTypeNames.AC -> HistoryDeviceType.AC
        DeviceTypeNames.DOOR -> HistoryDeviceType.DOOR
        DeviceTypeNames.ALARM -> HistoryDeviceType.ALARM
        DeviceTypeNames.FRIDGE -> HistoryDeviceType.FRIDGE
        else -> HistoryDeviceType.OTHER
    }

fun actionLabel(context: Context, actionName: String): String =
    context.getString(actionLabelRes(actionName) ?: R.string.notification_event_device_changed)

@StringRes
private fun actionLabelRes(actionName: String): Int? =
    when (actionName) {
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
        "statusChanged" -> R.string.notification_event_device_changed
        "volumeChanged" -> R.string.action_event_volume_changed
        "brightnessChanged" -> R.string.action_event_brightness_changed
        "colorChanged" -> R.string.action_event_color_changed
        "temperatureChanged" -> R.string.action_event_temperature_changed
        "modeChanged" -> R.string.action_event_mode_changed
        "opened" -> R.string.action_event_opened
        "closed" -> R.string.action_event_closed
        "locked" -> R.string.action_event_locked
        "unlocked" -> R.string.action_event_unlocked
        else -> null
    }

private fun relativeTime(context: Context, isoTimestamp: String): String {
    val millis = runCatching { Instant.parse(isoTimestamp).toEpochMilli() }.getOrNull()
        ?: return isoTimestamp
    return DateUtils.getRelativeTimeSpanString(
        millis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
}
