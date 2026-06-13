package com.grupo4.smarthome.data.model

// Contenido ya parseado de un QR de dispositivo SmartHome. Modelo de dominio: la UI/VM nunca ven
// el Uri ni el string crudo. `typeName` es el hint de tipo crudo del QR (un nombre, p. ej. "lamp"):
// la UI lo resuelve contra el catálogo real; puede no matchear y se completa manualmente. `qrId` es
// lo único obligatorio para identificar el QR.
data class QrDevicePayload(
    val qrId: String,
    val typeName: String? = null,
    val suggestedName: String? = null,
)
