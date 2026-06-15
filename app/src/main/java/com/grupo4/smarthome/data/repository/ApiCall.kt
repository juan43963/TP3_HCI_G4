package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.local.SessionManager
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

/**
 * Envuelve un request HTTP que devuelve un cuerpo a parsear en caso de éxito.
 *
 * Centraliza el patrón repetido en la capa de datos:
 * - `runCatching` + mapeo de excepciones a [AppResult.Error] vía [ErrorMapper.fromThrowable].
 * - Mapeo de respuestas no-2xx vía [ErrorMapper.fromHttp].
 * - Expiración de sesión ante 401 ([expireSessionOnUnauthorized]) — garantizada por construcción,
 *   no hay que recordar encadenarla en cada endpoint nuevo.
 *
 * [parse] es `suspend` para permitir efectos de éxito (guardar sesión, refrescar caché, etc.).
 */
internal suspend fun <T> apiCall(
    sessionManager: SessionManager,
    request: suspend () -> HttpResponse,
    parse: suspend (String) -> T,
): AppResult<T> =
    runCatching {
        val response = request()
        val body = response.bodyAsText()
        if (response.status.isSuccess()) {
            AppResult.Success(parse(body))
        } else {
            AppResult.Error(ErrorMapper.fromHttp(response.status, body))
        }
    }.getOrElse { throwable ->
        AppResult.Error(ErrorMapper.fromThrowable(throwable))
    }.expireSessionOnUnauthorized(sessionManager)

/**
 * Variante de [apiCall] para endpoints sin cuerpo relevante en éxito (delete/execute).
 * [onSuccess] permite efectos posteriores al 2xx (p. ej. actualizar la caché en memoria).
 */
internal suspend fun apiCallUnit(
    sessionManager: SessionManager,
    request: suspend () -> HttpResponse,
    onSuccess: suspend () -> Unit = {},
): AppResult<Unit> =
    runCatching {
        val response = request()
        if (response.status.isSuccess()) {
            onSuccess()
            AppResult.Success(Unit)
        } else {
            AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
        }
    }.getOrElse { throwable ->
        AppResult.Error(ErrorMapper.fromThrowable(throwable))
    }.expireSessionOnUnauthorized(sessionManager)

/**
 * Para los refrescos internos de caché que toleran el fallo: devuelve `null` ante error o
 * respuesta no-2xx (expirando la sesión si el motivo fue 401), en vez de propagar el error.
 */
internal suspend fun <T> apiFetchOrNull(
    sessionManager: SessionManager,
    request: suspend () -> HttpResponse,
    parse: (String) -> T,
): T? =
    runCatching {
        val response = request()
        if (response.status.isSuccess()) {
            parse(response.bodyAsText())
        } else {
            sessionManager.expireSessionIfUnauthorized(response.status)
            null
        }
    }.getOrNull()
