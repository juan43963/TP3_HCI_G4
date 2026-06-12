package com.grupo4.smarthome.ui.screens.scan

import com.grupo4.smarthome.data.model.QrDevicePayload

// Estado visual de la resolución del QR (lo que muestra el overlay sobre la cámara).
// El permiso y la disponibilidad de cámara los maneja la pantalla (concierne a Android/Compose),
// mientras que estos estados los gobierna el ViewModel según el parseo y el match.
sealed interface ScanUiState {
    data object Scanning : ScanUiState
    data object Processing : ScanUiState
    data object Invalid : ScanUiState
}

// Navegación como evento consumible (se dispara con LaunchedEffect, nunca desde el analizador de frames).
sealed interface ScanNavEvent {
    data class OpenDevice(val deviceId: String) : ScanNavEvent
    data class CreateDevice(val payload: QrDevicePayload) : ScanNavEvent
}
