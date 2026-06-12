package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.NotificationEvent
import kotlinx.coroutines.flow.Flow

interface NotificationsRepository {
    suspend fun getRecentEvents(limit: Int = 50): AppResult<List<NotificationEvent>>

    fun realtimeEvents(): Flow<NotificationEvent>
}
