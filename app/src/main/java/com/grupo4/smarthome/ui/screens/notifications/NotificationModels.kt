package com.grupo4.smarthome.ui.screens.notifications

import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.graphics.vector.ImageVector
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

/**
 * Familias de notificación que el usuario puede activar o desactivar.
 * Cada una tiene su propio ícono y textos para mantener consistencia con el resto del TP.
 */
enum class NotificationCategory(
    val icon: ImageVector,
    val labelRes: Int,
    val descriptionRes: Int,
) {
    SECURITY(SmartHomeIcons.Shield, R.string.notifications_category_security, R.string.notifications_category_security_description),
    DEVICES(SmartHomeIcons.Devices, R.string.notifications_category_devices, R.string.notifications_category_devices_description),
    ROUTINES(SmartHomeIcons.Routines, R.string.notifications_category_routines, R.string.notifications_category_routines_description),
    HOME(SmartHomeIcons.Home, R.string.notifications_category_home, R.string.notifications_category_home_description),
}

/**
 * Una notificación in-app mostrada en el centro de notificaciones.
 *
 * Si [invitationHome] no es null, la notificación es una invitación a un hogar y se
 * muestra con acciones "Aceptar" / "Rechazar" dentro de la propia tarjeta.
 */
data class NotificationUi(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val message: String,
    val timeLabel: String,
    val read: Boolean = false,
    val invitationHome: String? = null,
)

/**
 * Preferencias de notificación controladas por el usuario.
 *
 * Heurística "control y libertad de usuario": es el usuario quien decide qué recibir.
 * [showInAppToast] cubre el caso en que está viendo el dispositivo que cambió (Toast);
 * cuando no lo está viendo, el aviso aparece como badge en la campana.
 */
data class NotificationPreferences(
    val enabled: Boolean = true,
    val security: Boolean = true,
    val devices: Boolean = true,
    val routines: Boolean = true,
    val home: Boolean = true,
    val showInAppToast: Boolean = true,
) {
    fun isEnabled(category: NotificationCategory): Boolean = when (category) {
        NotificationCategory.SECURITY -> security
        NotificationCategory.DEVICES -> devices
        NotificationCategory.ROUTINES -> routines
        NotificationCategory.HOME -> home
    }

    fun withCategory(category: NotificationCategory, value: Boolean): NotificationPreferences = when (category) {
        NotificationCategory.SECURITY -> copy(security = value)
        NotificationCategory.DEVICES -> copy(devices = value)
        NotificationCategory.ROUTINES -> copy(routines = value)
        NotificationCategory.HOME -> copy(home = value)
    }
}

/** Permite que las preferencias sobrevivan a cambios de configuración (rotación). */
val NotificationPreferencesSaver = listSaver<NotificationPreferences, Boolean>(
    save = { listOf(it.enabled, it.security, it.devices, it.routines, it.home, it.showInAppToast) },
    restore = {
        NotificationPreferences(
            enabled = it[0],
            security = it[1],
            devices = it[2],
            routines = it[3],
            home = it[4],
            showInAppToast = it[5],
        )
    },
)

/** Datos de ejemplo para previsualizar el centro de notificaciones. */
val sampleNotifications: List<NotificationUi> = listOf(
    NotificationUi(
        id = "0",
        category = NotificationCategory.HOME,
        title = "Invitación a un hogar",
        message = "Martín te invitó a unirte a “Casa de la playa”.",
        timeLabel = "ahora",
        read = false,
        invitationHome = "Casa de la playa",
    ),
    NotificationUi(
        id = "1",
        category = NotificationCategory.SECURITY,
        title = "Puerta de entrada desbloqueada",
        message = "Martín desbloqueó la puerta de entrada.",
        timeLabel = "hace 2 min",
        read = false,
    ),
    NotificationUi(
        id = "2",
        category = NotificationCategory.ROUTINES,
        title = "Rutina “Llegada a casa” ejecutada",
        message = "Se encendieron las luces del living y se ajustó el clima.",
        timeLabel = "hace 15 min",
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
        id = "4",
        category = NotificationCategory.HOME,
        title = "Nuevo miembro en el hogar",
        message = "Lucía aceptó tu invitación a “Casa Principal”.",
        timeLabel = "hace 3 h",
        read = true,
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
