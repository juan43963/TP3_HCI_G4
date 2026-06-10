package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceType

interface DevicesRepository {
    // Lista los dispositivos de una habitación, ya mapeados al modelo de dominio
    suspend fun getRoomDevices(roomId: String): AppResult<List<Device>>

    // Crea un dispositivo y lo asocia a la habitación (resuelve el deviceTypeId real contra el catálogo)
    suspend fun createDevice(roomId: String, type: DeviceType, name: String): AppResult<Device>

    // Renombra un dispositivo preservando su metadata
    suspend fun renameDevice(deviceId: String, name: String): AppResult<Device>

    // Borra un dispositivo (DELETE /devices/{deviceId})
    suspend fun deleteDevice(deviceId: String): AppResult<Unit>

    // Enciende/apaga (o lock/unlock) eligiendo la acción según el catálogo del tipo
    suspend fun toggleDevice(device: Device): AppResult<Device>

    // Ajusta un valor (brillo/temperatura/volumen) con la acción posicional correspondiente
    suspend fun setDeviceValue(device: Device, value: Int): AppResult<Device>
}
