package com.grupo4.smarthome.data.model

import com.grupo4.smarthome.ui.screens.notifications.NotificationCategory

enum class NotificationKind {
    DEVICE_ACTION,
    DEVICE_CREATED,
    DEVICE_UPDATED,
    DEVICE_DELETED,
}

data class NotificationEvent(
    val id: String,
    val kind: NotificationKind,
    val timestampMillis: Long,
    val deviceId: String? = null,
    val actionName: String? = null,
)

fun NotificationEvent.categoryFor(deviceType: DeviceType?): NotificationCategory =
    when (deviceType) {
        DeviceType.DOOR, DeviceType.ALARM -> NotificationCategory.SECURITY
        else -> NotificationCategory.DEVICES
    }
