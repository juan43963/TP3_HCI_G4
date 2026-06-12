package com.grupo4.smarthome.data.repository

import android.util.Log
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.NotificationEvent
import com.grupo4.smarthome.data.model.NotificationKind
import com.grupo4.smarthome.data.remote.socket.SocketEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import java.time.Instant

class NotificationsRepositoryImpl(
    private val devicesRepository: DevicesRepository,
    private val socketEvents: Flow<SocketEvent>,
    private val localActionTracker: LocalActionTracker,
) : NotificationsRepository {

    override suspend fun getRecentEvents(limit: Int): AppResult<List<NotificationEvent>> =
        when (val result = devicesRepository.getLogs(limit = limit, offset = 0)) {
            is AppResult.Success -> AppResult.Success(
                result.data.map { it.toNotificationEvent() }
                    .onEach { it.logCreateEventIfRelevant(source = "logs") }
            )
            is AppResult.Error -> AppResult.Error(result.error)
        }

    override fun realtimeEvents(): Flow<NotificationEvent> =
        socketEvents
            .mapNotNull { it.toNotificationEvent() }
            .onEach { it.logCreateEventIfRelevant(source = "realtime") }
            .filterNot { it.isLocalEcho() }
            .onEach {
                if (it.kind == NotificationKind.DEVICE_CREATED) {
                    Log.d("CreateDeviceAudit", "CREATE_FEED_ADD source=realtime deviceId=${it.deviceId} kind=${it.kind}")
                }
            }

    private fun NotificationEvent.logCreateEventIfRelevant(source: String) {
        val raw = actionName
        val looksLikeCreate = kind == NotificationKind.DEVICE_CREATED ||
            raw?.lowercase()?.let { "creat" in it || "add" in it } == true
        if (looksLikeCreate) {
            Log.d(
                "CreateDeviceAudit",
                "CREATE_NOTIF_EVENT source=$source eventName=$raw deviceId=$deviceId kind=$kind",
            )
        }
    }

    private fun NotificationEvent.isLocalEcho(): Boolean {
        val id = deviceId ?: run {
            if (kind == NotificationKind.DEVICE_CREATED) {
                Log.d("CreateDeviceAudit", "CREATE_SUPPRESS_MISS deviceId=null kind=$kind reason=null-deviceId")
            }
            return false
        }
        val suppress = when (kind) {
            NotificationKind.DEVICE_ACTION,
            NotificationKind.DEVICE_CREATED,
            NotificationKind.DEVICE_UPDATED,
            NotificationKind.DEVICE_DELETED -> localActionTracker.shouldSuppress(id, kind)
        }
        if (kind == NotificationKind.DEVICE_CREATED) {
            if (suppress) {
                Log.d("CreateDeviceAudit", "CREATE_SUPPRESS_HIT deviceId=$id kind=$kind")
            } else {
                Log.d("CreateDeviceAudit", "CREATE_SUPPRESS_MISS deviceId=$id kind=$kind reason=no-active-mark")
            }
        }
        return suppress
    }
}

private fun DeviceLog.toNotificationEvent(): NotificationEvent =
    NotificationEvent(
        id = id,
        kind = NotificationKind.DEVICE_ACTION,
        timestampMillis = timestamp.toEpochMillisOrNow(),
        deviceId = deviceId,
        actionName = actionName,
    )

private fun SocketEvent.toNotificationEvent(): NotificationEvent? =
    when (this) {
        is SocketEvent.Connected -> null
        is SocketEvent.DeviceCreated -> NotificationEvent(
            id = "created-${deviceId.orEmpty()}-$timestampMillis",
            kind = NotificationKind.DEVICE_CREATED,
            timestampMillis = timestampMillis,
            deviceId = deviceId,
        )
        is SocketEvent.DeviceUpdated -> NotificationEvent(
            id = "updated-${deviceId.orEmpty()}-$timestampMillis",
            kind = NotificationKind.DEVICE_UPDATED,
            timestampMillis = timestampMillis,
            deviceId = deviceId,
        )
        is SocketEvent.DeviceDeleted -> NotificationEvent(
            id = "deleted-${deviceId.orEmpty()}-$timestampMillis",
            kind = NotificationKind.DEVICE_DELETED,
            timestampMillis = timestampMillis,
            deviceId = deviceId,
        )
        is SocketEvent.DeviceEvent -> NotificationEvent(
            id = id ?: "event-${deviceId.orEmpty()}-$timestampMillis",
            kind = NotificationKind.DEVICE_ACTION,
            timestampMillis = timestampMillis,
            deviceId = deviceId,
            actionName = eventName,
        )
    }

private fun String.toEpochMillisOrNow(): Long =
    runCatching { Instant.parse(this).toEpochMilli() }.getOrNull() ?: System.currentTimeMillis()
