package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.local.SessionManager
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

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
