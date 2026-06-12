package com.grupo4.smarthome.data.remote.socket

sealed interface SocketEvent {
    data class Connected(val userId: String?) : SocketEvent
    data class DeviceCreated(val deviceId: String?, val timestampMillis: Long) : SocketEvent
    data class DeviceUpdated(val deviceId: String?, val timestampMillis: Long) : SocketEvent
    data class DeviceDeleted(val deviceId: String?, val timestampMillis: Long) : SocketEvent
    data class DeviceEvent(
        val id: String?,
        val deviceId: String?,
        val eventName: String?,
        val timestampMillis: Long,
    ) : SocketEvent
}
