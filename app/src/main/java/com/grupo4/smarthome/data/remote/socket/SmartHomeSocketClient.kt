package com.grupo4.smarthome.data.remote.socket

import com.grupo4.smarthome.core.network.ApiConstants
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.time.Instant

class SmartHomeSocketClient(
    private val tokenFlow: Flow<String?>,
) {
    // Conecta solo cuando hay un token valido y RECONECTA cuando el token cambia
    // (login/logout). Evita el cold-start donde el socket se autenticaba con token
    // null y quedaba mudo toda la sesion. flatMapLatest cancela la conexion vieja
    // y abre una nueva con el token vigente.
    @OptIn(ExperimentalCoroutinesApi::class)
    fun events(): Flow<SocketEvent> =
        tokenFlow
            .map { it?.takeIf { token -> token.isNotBlank() } }
            .distinctUntilChanged()
            .flatMapLatest { token ->
                if (token == null) emptyFlow() else connect(token)
            }

    private fun connect(token: String): Flow<SocketEvent> = callbackFlow {
        val auth = buildMap {
            put("token", token)
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
