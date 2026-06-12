package com.grupo4.smarthome.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.qr.QrDevicePayloadParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Scanning)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableStateFlow<ScanNavEvent?>(null)
    val navEvent: StateFlow<ScanNavEvent?> = _navEvent.asStateFlow()

    // Dedup: mientras resolvemos una lectura no procesamos otra, y nunca reprocesamos el mismo raw.
    private var isProcessing = false
    private var lastHandledRaw: String? = null

    // Llamado por el analizador de CameraX (hilo de ML Kit). Solo recibe el raw + los devices del
    // hogar activo; jamás toca estado de Compose. La navegación se emite como evento consumible.
    fun onCodeDetected(raw: String, homeDevices: List<Device>) {
        if (isProcessing || raw == lastHandledRaw) return
        lastHandledRaw = raw

        val payload = QrDevicePayloadParser.parse(raw)
        if (payload == null) {
            _uiState.value = ScanUiState.Invalid
            return
        }

        isProcessing = true
        _uiState.value = ScanUiState.Processing
        val existing = homeDevices.firstOrNull { it.qrId != null && it.qrId == payload.qrId }
        _navEvent.value = if (existing != null) {
            ScanNavEvent.OpenDevice(existing.id)
        } else {
            ScanNavEvent.CreateDevice(payload)
        }
    }

    fun consumeNavEvent() {
        _navEvent.value = null
    }

    // Reactiva el escáner tras un QR inválido (botón "Escanear de nuevo").
    fun resumeScanning() {
        isProcessing = false
        lastHandledRaw = null
        _uiState.value = ScanUiState.Scanning
    }
}

class ScanViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            return ScanViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
