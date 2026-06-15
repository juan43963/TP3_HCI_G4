package com.grupo4.smarthome.data.local

sealed interface SessionState {
    data object Loading : SessionState
    data class Authenticated(val data: SessionData) : SessionState
    data object Unauthenticated : SessionState
}
