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

// Categoriza la notificación según el nombre canónico del tipo (ver DeviceTypeNames): puerta y alarma
// son de seguridad, el resto dispositivos. Recibe el canonicalName para no acoplarse al catálogo.
fun NotificationEvent.categoryFor(canonicalTypeName: String?): NotificationCategory =
    when (canonicalTypeName) {
        DeviceTypeNames.DOOR, DeviceTypeNames.ALARM -> NotificationCategory.SECURITY
        else -> NotificationCategory.DEVICES
    }
