package com.grupo4.smarthome.data.remote.socket

import com.grupo4.smarthome.core.network.ApiConstants
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import java.time.Instant

class SmartHomeSocketClient(
    private val tokenProvider: () -> String?,
) {
    fun events(): Flow<SocketEvent> = callbackFlow {
        val auth = buildMap {
            tokenProvider()?.let { put("token", it) }
            put("apiKey", ApiConstants.ApiKey)
        }
        val options = IO.Options().apply { this.auth = auth }
        val socket = runCatching { IO.socket(ApiConstants.SocketUrl, options) }.getOrNull()
        if (socket == null) {
            close()
            return@callbackFlow
        }

        val listeners = mutableListOf<Pair<String, Emitter.Listener>>()
        fun register(event: String, mapper: (JSONObject?) -> SocketEvent?) {
            val listener = Emitter.Listener { args ->
                val payload = args.firstOrNull() as? JSONObject
                mapper(payload)?.let { trySend(it) }
            }
            socket.on(event, listener)
            listeners += event to listener
        }

        register("connected") { SocketEvent.Connected(it.stringOrNull("userId")) }
        register("deviceCreated") {
            SocketEvent.DeviceCreated(it.deviceId(), it.timestamp())
        }
        register("deviceUpdated") {
            SocketEvent.DeviceUpdated(it.deviceId(), it.timestamp())
        }
        register("deviceDeleted") {
            SocketEvent.DeviceDeleted(it.stringOrNull("deviceId") ?: it.deviceId(), it.timestamp())
        }
        register("deviceEvent") { payload ->
            val data = payload?.optJSONObject("data")
            SocketEvent.DeviceEvent(
                id = payload.stringOrNull("id"),
                deviceId = data.stringOrNull("deviceId"),
                eventName = data.stringOrNull("event"),
                timestampMillis = data.timestamp(),
            )
        }

        socket.connect()

        awaitClose {
            listeners.forEach { (event, listener) -> socket.off(event, listener) }
            socket.disconnect()
            socket.close()
        }
    }
}

private fun JSONObject?.stringOrNull(key: String): String? =
    this?.takeIf { it.has(key) && !it.isNull(key) }?.optString(key)

private fun JSONObject?.deviceId(): String? =
    this?.optJSONObject("device")?.stringOrNull("id") ?: this.stringOrNull("deviceId")

private fun JSONObject?.timestamp(): Long {
    val raw = this.stringOrNull("timestamp")
    return raw?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
        ?: System.currentTimeMillis()
}
