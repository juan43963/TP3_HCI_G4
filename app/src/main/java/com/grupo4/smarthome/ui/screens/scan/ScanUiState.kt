package com.grupo4.smarthome.ui.screens.scan

import com.grupo4.smarthome.data.model.QrDevicePayload

sealed interface ScanUiState {
    data object Scanning : ScanUiState
    data object Processing : ScanUiState
    data object Invalid : ScanUiState
}

sealed interface ScanNavEvent {
    data class OpenDevice(val deviceId: String) : ScanNavEvent
    data class CreateDevice(val payload: QrDevicePayload) : ScanNavEvent
}
