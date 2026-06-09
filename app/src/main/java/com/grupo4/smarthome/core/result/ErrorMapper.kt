package com.grupo4.smarthome.core.result

import com.grupo4.smarthome.core.network.ApiJson
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import java.io.IOException

object ErrorMapper {
    fun fromHttp(status: HttpStatusCode, body: String?): ApiError {
        val parsed = parseBody(body)
        val kind = kindFor(status, parsed?.code)
        return ApiError(
            kind = kind,
            description = parsed?.description ?: defaultDescription(kind),
            httpStatus = status.value,
            apiCode = parsed?.code,
        )
    }

    fun fromThrowable(throwable: Throwable): ApiError {
        val kind = when (throwable) {
            is HttpRequestTimeoutException,
            is IOException -> ErrorKind.Network
            else -> ErrorKind.Unknown
        }
        return ApiError(
            kind = kind,
            description = defaultDescription(kind),
        )
    }

    private fun kindFor(status: HttpStatusCode, apiCode: Int?): ErrorKind =
        when {
            status == HttpStatusCode.BadRequest || apiCode == 2 -> ErrorKind.Validation
            status == HttpStatusCode.Conflict || apiCode == 3 -> ErrorKind.Conflict
            status == HttpStatusCode.Unauthorized -> ErrorKind.Unauthorized
            status == HttpStatusCode.Forbidden || apiCode == 6 -> ErrorKind.Forbidden
            status == HttpStatusCode.NotFound || apiCode == 5 -> ErrorKind.NotFound
            status.value in 500..599 || apiCode == 4 -> ErrorKind.Server
            else -> ErrorKind.Unknown
        }

    private fun parseBody(body: String?): ApiErrorBody? {
        if (body.isNullOrBlank()) return null
        val wrapped = runCatching {
            ApiJson.Instance.decodeFromString(ApiErrorResponseBody.serializer(), body).error
        }.getOrNull()
        if (wrapped != null) return wrapped
        return runCatching {
            ApiJson.Instance.decodeFromString(ApiErrorBody.serializer(), body)
        }.getOrNull()
    }

    private fun defaultDescription(kind: ErrorKind): String =
        when (kind) {
            ErrorKind.Validation -> "Revisá los datos ingresados."
            ErrorKind.Conflict -> "Ya existe un recurso con esos datos."
            ErrorKind.Unauthorized -> "No se pudo autenticar la solicitud."
            ErrorKind.Forbidden -> "No tenés permiso para realizar esta acción."
            ErrorKind.NotFound -> "No se encontró el recurso solicitado."
            ErrorKind.Network -> "No se pudo conectar con el servidor."
            ErrorKind.Server -> "El servidor no pudo completar la solicitud."
            ErrorKind.Unknown -> "Ocurrió un error inesperado."
        }
}

@Serializable
private data class ApiErrorResponseBody(
    val error: ApiErrorBody? = null,
)

@Serializable
private data class ApiErrorBody(
    val code: Int? = null,
    val description: String? = null,
)
