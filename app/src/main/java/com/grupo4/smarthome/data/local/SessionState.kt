package com.grupo4.smarthome.data.local

// Estado de sesión al arrancar: Loading distingue "leyendo DataStore" de "sin sesión"
sealed interface SessionState {
    data object Loading : SessionState
    data class Authenticated(val data: SessionData) : SessionState
    data object Unauthenticated : SessionState
}
