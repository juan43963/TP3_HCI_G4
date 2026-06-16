package com.grupo4.smarthome.data.local

import com.grupo4.smarthome.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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

    val sessionState: StateFlow<SessionState> =
        sessionDataStore.session
            .map { if (it != null) SessionState.Authenticated(it) else SessionState.Unauthenticated }
            .stateIn(scope, SharingStarted.Eagerly, SessionState.Loading)

    fun currentToken(): String? = session.value?.token

    val tokenFlow: Flow<String?> = session.map { it?.token }

    suspend fun saveSession(token: String, user: User) {
        sessionDataStore.saveSession(token, user)
    }

    suspend fun updateUser(user: User) {
        val token = session.value?.token ?: return
        sessionDataStore.saveSession(token, user)
    }

    suspend fun clearSession() {
        sessionDataStore.clearSession()
    }

    suspend fun expireSession() = clearSession()
}
