package com.grupo4.smarthome.data.qr

import com.grupo4.smarthome.data.model.QrDevicePayload
import java.net.URI
import java.net.URLDecoder

object QrDevicePayloadParser {
    private const val SCHEME = "smarthome"
    private const val HOST = "device"

    fun parse(raw: String): QrDevicePayload? {
        val uri = runCatching { URI(raw.trim()) }.getOrNull() ?: return null
        if (!uri.scheme.equals(SCHEME, ignoreCase = true)) return null
        if (!uri.host.equals(HOST, ignoreCase = true)) return null

        val params = parseQuery(uri.rawQuery)
        val qrId = params["id"]?.takeIf { it.isNotBlank() } ?: return null
        return QrDevicePayload(
            qrId = qrId,
            typeName = params["type"]?.takeIf { it.isNotBlank() }?.trim(),
            suggestedName = params["name"]?.takeIf { it.isNotBlank() },
        )
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        return rawQuery.split("&").mapNotNull { pair ->
            val separator = pair.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            decode(pair.substring(0, separator)) to decode(pair.substring(separator + 1))
        }.toMap()
    }

    private fun decode(value: String): String =
        runCatching { URLDecoder.decode(value, Charsets.UTF_8.name()) }.getOrDefault(value)
}
