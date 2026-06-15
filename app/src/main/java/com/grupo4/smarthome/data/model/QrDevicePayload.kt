package com.grupo4.smarthome.data.model

data class QrDevicePayload(
    val qrId: String,
    val typeName: String? = null,
    val suggestedName: String? = null,
)
