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

    // Dedup: mientras resolvemos una lectura no procesamos otra, y nunca reprocesamos el mismo raw.
    private var isProcessing = false
    private var lastHandledRaw: String? = null

    // QR válido leído pero cuya clasificación quedó pendiente porque los devices del hogar todavía no
    // estaban cargados. Lo reintentamos cuando lleguen (ver [onDevicesUpdated]) para no clasificar un
    // dispositivo ya existente como "nuevo" solo por haberlo leído durante la carga inicial.
    private var pendingPayload: QrDevicePayload? = null

    // Llamado por el analizador de CameraX (hilo de ML Kit). Recibe el raw, los devices del hogar activo
    // y si esa lista ya terminó de cargar; jamás toca estado de Compose. La navegación se emite como
    // evento consumible.
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

    // Reintenta clasificar el QR pendiente cuando la lista de devices del hogar se actualiza (típicamente
    // al terminar la carga inicial). Sin pendiente no hace nada.
    fun onDevicesUpdated(homeDevices: List<Device>, devicesLoaded: Boolean) {
        val payload = pendingPayload ?: return
        resolve(payload, homeDevices, devicesLoaded)
    }

    // Un match positivo siempre es confiable -> OpenDevice. Si no hay match, solo damos el QR por "nuevo"
    // (CreateDevice) cuando los devices YA cargaron; si todavía están cargando dejamos el QR pendiente y
    // seguimos en Processing hasta que lleguen, así un dispositivo existente nunca se clasifica como nuevo.
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
            // Devices aún cargando: esperamos a [onDevicesUpdated]. La UI sigue mostrando Processing.
            else -> pendingPayload = payload
        }
    }

    fun consumeNavEvent() {
        _navEvent.value = null
    }

    // Reactiva el escáner tras un QR inválido (botón "Escanear de nuevo").
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
