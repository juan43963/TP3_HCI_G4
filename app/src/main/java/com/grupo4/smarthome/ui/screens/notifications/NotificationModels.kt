package com.grupo4.smarthome.ui.screens.notifications

import androidx.compose.ui.graphics.vector.ImageVector
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

enum class NotificationCategory(
    val icon: ImageVector,
    val labelRes: Int,
    val descriptionRes: Int,
) {
    SECURITY(SmartHomeIcons.Shield, R.string.notifications_category_security, R.string.notifications_category_security_description),
    DEVICES(SmartHomeIcons.Devices, R.string.notifications_category_devices, R.string.notifications_category_devices_description),
}

data class NotificationUi(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val message: String,
    val timeLabel: String,
    val read: Boolean = false,
)

data class NotificationPreferences(
    val enabled: Boolean = true,
    val security: Boolean = true,
    val devices: Boolean = true,
    val showInAppToast: Boolean = true,
) {
    fun isEnabled(category: NotificationCategory): Boolean = when (category) {
        NotificationCategory.SECURITY -> security
        NotificationCategory.DEVICES -> devices
    }

    fun withCategory(category: NotificationCategory, value: Boolean): NotificationPreferences = when (category) {
        NotificationCategory.SECURITY -> copy(security = value)
        NotificationCategory.DEVICES -> copy(devices = value)
    }
}

val sampleNotifications: List<NotificationUi> = listOf(
    NotificationUi(
        id = "1",
        category = NotificationCategory.SECURITY,
        title = "Puerta de entrada desbloqueada",
        message = "Martín desbloqueó la puerta de entrada.",
        timeLabel = "hace 2 min",
        read = false,
    ),
    NotificationUi(
        id = "3",
        category = NotificationCategory.DEVICES,
        title = "Aire acondicionado encendido",
        message = "Cuarto principal · programado a 22°.",
        timeLabel = "hace 40 min",
        read = false,
    ),
    NotificationUi(
        id = "5",
        category = NotificationCategory.DEVICES,
        title = "Aspiradora volvió a la base",
        message = "Oficina · terminó la limpieza programada.",
        timeLabel = "ayer",
        read = true,
    ),
)
