package com.grupo4.smarthome.data.model

data class DeviceLog(
    val id: String,
    val timestamp: String,
    val deviceId: String,
    val actionName: String,
)
