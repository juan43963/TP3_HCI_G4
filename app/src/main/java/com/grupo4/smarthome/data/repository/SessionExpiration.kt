package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.data.local.SessionManager
import io.ktor.http.HttpStatusCode

internal suspend fun <T> AppResult<T>.expireSessionOnUnauthorized(
    sessionManager: SessionManager,
): AppResult<T> {
    if (this is AppResult.Error && error.kind == ErrorKind.Unauthorized) {
        sessionManager.expireSession()
    }
    return this
}

internal suspend fun SessionManager.expireSessionIfUnauthorized(status: HttpStatusCode) {
    if (status == HttpStatusCode.Unauthorized) {
        expireSession()
    }
}
