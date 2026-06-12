package com.grupo4.smarthome.ui.screens.notifications

import androidx.annotation.StringRes
import com.grupo4.smarthome.data.model.NotificationEvent

data class FeedNotification(
    val event: NotificationEvent,
    val category: NotificationCategory,
    val read: Boolean,
)

data class NotificationsUiState(
    val items: List<FeedNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    @StringRes val errorMessage: Int? = null,
)
