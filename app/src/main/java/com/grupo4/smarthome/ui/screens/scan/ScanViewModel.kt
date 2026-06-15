package com.grupo4.smarthome.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.QrDevicePayload
import com.grupo4.smarthome.data.qr.QrDevicePayloadParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Scanning)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableStateFlow<ScanNavEvent?>(null)
    val navEvent: StateFlow<ScanNavEvent?> = _navEvent.asStateFlow()

    private var isProcessing = false
    private var lastHandledRaw: String? = null

    private var pendingPayload: QrDevicePayload? = null

    fun onCodeDetected(raw: String, homeDevices: List<Device>, devicesLoaded: Boolean) {
        if (isProcessing || raw == lastHandledRaw) return
        lastHandledRaw = raw

        val payload = QrDevicePayloadParser.parse(raw)
        if (payload == null) {
            _uiState.value = ScanUiState.Invalid
            return
        }

        isProcessing = true
        _uiState.value = ScanUiState.Processing
        resolve(payload, homeDevices, devicesLoaded)
    }

    fun onDevicesUpdated(homeDevices: List<Device>, devicesLoaded: Boolean) {
        val payload = pendingPayload ?: return
        resolve(payload, homeDevices, devicesLoaded)
    }

    private fun resolve(payload: QrDevicePayload, homeDevices: List<Device>, devicesLoaded: Boolean) {
        val existing = homeDevices.firstOrNull { it.qrId != null && it.qrId == payload.qrId }
        when {
            existing != null -> {
                pendingPayload = null
                _navEvent.value = ScanNavEvent.OpenDevice(existing.id)
            }
            devicesLoaded -> {
                pendingPayload = null
                _navEvent.value = ScanNavEvent.CreateDevice(payload)
            }
            else -> pendingPayload = payload
        }
    }

    fun consumeNavEvent() {
        _navEvent.value = null
    }

    fun resumeScanning() {
        isProcessing = false
        lastHandledRaw = null
        pendingPayload = null
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
