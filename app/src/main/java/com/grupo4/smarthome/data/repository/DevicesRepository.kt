package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.DeviceType

interface DevicesRepository {
    // Lista los dispositivos de una habitación, ya mapeados al modelo de dominio
    suspend fun getRoomDevices(roomId: String): AppResult<List<Device>>

    // Historial de acciones de todos los dispositivos, paginado y ordenado por la API
    suspend fun getLogs(limit: Int = 50, offset: Int = 0): AppResult<List<DeviceLog>>

    // Crea un dispositivo y lo asocia a la habitación (resuelve el deviceTypeId real contra el catálogo).
    // qrId opcional: si viene de un QR, se persiste en metadata para reconocer el device al reescanear.
    suspend fun createDevice(
        roomId: String,
        type: DeviceType,
        name: String,
        qrId: String? = null,
    ): AppResult<Device>

    // Renombra un dispositivo preservando su metadata
    suspend fun renameDevice(deviceId: String, name: String): AppResult<Device>

    // Borra un dispositivo (DELETE /devices/{deviceId})
    suspend fun deleteDevice(deviceId: String): AppResult<Unit>

    // Enciende/apaga (o lock/unlock) eligiendo la acción según el catálogo del tipo
    suspend fun toggleDevice(device: Device): AppResult<Device>

    // Ajusta un valor (brillo/temperatura/volumen) con la acción posicional correspondiente
    suspend fun setDeviceValue(device: Device, value: Int): AppResult<Device>

    // Acciones que admite un tipo según el catálogo (GET /devicetypes); usado al armar rutinas
    suspend fun getActionsFor(type: DeviceType): AppResult<List<DeviceActionInfo>>
}
