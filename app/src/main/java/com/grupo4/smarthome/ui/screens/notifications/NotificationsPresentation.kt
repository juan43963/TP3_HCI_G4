package com.grupo4.smarthome.ui.screens.notifications

import android.content.Context
import android.text.format.DateUtils
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.NotificationKind
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.ui.screens.history.actionLabel

private data class DeviceLabel(val name: String, val roomName: String)

fun List<FeedNotification>.toNotificationUi(
    context: Context,
    rooms: List<Room>,
): List<NotificationUi> {
    val deviceLookup = buildDeviceLabels(rooms)
    return map { it.toNotificationUi(context, deviceLookup) }
}

private fun FeedNotification.toNotificationUi(
    context: Context,
    lookup: Map<String, DeviceLabel>,
): NotificationUi {
    val device = event.deviceId?.let { lookup[it] }
    val deviceName = device?.name ?: context.getString(R.string.history_unknown_device)
    val roomName = device?.roomName ?: context.getString(R.string.history_unknown_room)
    val title: String
    val message: String
    when (event.kind) {
        NotificationKind.DEVICE_ACTION -> {
            val action = event.actionName?.let { actionLabel(context, it) }
                ?: context.getString(R.string.notification_event_device_changed)
            title = deviceName
            message = context.getString(R.string.notification_event_action_message, action, roomName)
        }
        NotificationKind.DEVICE_CREATED -> {
            title = context.getString(R.string.notification_event_device_added)
            message = deviceName
        }
        NotificationKind.DEVICE_UPDATED -> {
            title = context.getString(R.string.notification_event_device_updated)
            message = deviceName
        }
        NotificationKind.DEVICE_DELETED -> {
            title = context.getString(R.string.notification_event_device_removed)
            message = deviceName
        }
    }
    return NotificationUi(
        id = event.id,
        category = category,
        title = title,
        message = message,
        timeLabel = relativeLabel(event.timestampMillis),
        read = read,
    )
}

private fun buildDeviceLabels(rooms: List<Room>): Map<String, DeviceLabel> =
    buildMap {
        rooms.forEach { room ->
            room.devices.forEach { put(it.id, DeviceLabel(it.name, room.name)) }
        }
    }

private fun relativeLabel(millis: Long): String =
    DateUtils.getRelativeTimeSpanString(
        millis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
