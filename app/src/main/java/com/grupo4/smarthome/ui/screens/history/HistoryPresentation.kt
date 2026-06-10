package com.grupo4.smarthome.ui.screens.history

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.model.Room
import java.time.Instant

// Datos de un dispositivo que el historial necesita para mostrar nombre/habitación/icono a partir del deviceId
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

private fun DeviceType.toHistoryType(): HistoryDeviceType =
    when (this) {
        DeviceType.VACUUM -> HistoryDeviceType.VACUUM
        DeviceType.SPEAKER -> HistoryDeviceType.SPEAKER
        DeviceType.FAUCET -> HistoryDeviceType.FAUCET
        DeviceType.BLINDS -> HistoryDeviceType.BLINDS
        DeviceType.LIGHT -> HistoryDeviceType.LIGHT
        DeviceType.OVEN -> HistoryDeviceType.OVEN
        DeviceType.AC -> HistoryDeviceType.AC
        DeviceType.DOOR -> HistoryDeviceType.DOOR
        DeviceType.ALARM -> HistoryDeviceType.ALARM
        DeviceType.FRIDGE -> HistoryDeviceType.FRIDGE
    }

// Para acciones desconocidas mostramos el nombre técnico crudo (mejor que ocultar la entrada)
private fun actionLabel(context: Context, actionName: String): String =
    actionLabelRes(actionName)?.let { context.getString(it) } ?: actionName

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
