package com.grupo4.smarthome.data.model

// Contenido ya parseado de un QR de dispositivo SmartHome. Modelo de dominio: la UI/VM nunca ven
// el Uri ni el string crudo. `type` y `suggestedName` son opcionales porque el flujo de creación
// permite completarlos manualmente; `qrId` es lo único obligatorio para identificar el QR.
data class QrDevicePayload(
    val qrId: String,
    val type: DeviceType? = null,
    val suggestedName: String? = null,
)
