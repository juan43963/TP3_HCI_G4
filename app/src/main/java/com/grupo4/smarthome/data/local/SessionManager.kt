package com.grupo4.smarthome.data.local

import com.grupo4.smarthome.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SessionManager(
    private val sessionDataStore: SessionDataStore,
    scope: CoroutineScope,
) {
    val session: StateFlow<SessionData?> =
        sessionDataStore.session.stateIn(scope, SharingStarted.Eagerly, null)

    // Arranca en Loading hasta la primera emisión real de DataStore: define la pantalla inicial
    val sessionState: StateFlow<SessionState> =
        sessionDataStore.session
            .map { if (it != null) SessionState.Authenticated(it) else SessionState.Unauthenticated }
            .stateIn(scope, SharingStarted.Eagerly, SessionState.Loading)

    // Token actual leído de forma síncrona para el header Authorization: Bearer
    fun currentToken(): String? = session.value?.token

    suspend fun saveSession(token: String, user: User) {
        sessionDataStore.saveSession(token, user)
    }

    suspend fun clearSession() {
        sessionDataStore.clearSession()
    }
}
