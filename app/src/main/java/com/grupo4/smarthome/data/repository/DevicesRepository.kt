package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.ActionArg
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionResult
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.DeviceTypeInfo

interface DevicesRepository {
    // Lista los dispositivos de una habitación, ya mapeados al modelo de dominio
    suspend fun getRoomDevices(roomId: String): AppResult<List<Device>>

    // Historial de acciones de todos los dispositivos, paginado y ordenado por la API
    suspend fun getLogs(limit: Int = 50, offset: Int = 0): AppResult<List<DeviceLog>>

    // Catálogo de tipos soportados (GET /devicetypes): fuente de verdad para crear y para la UI
    suspend fun getDeviceTypes(): AppResult<List<DeviceTypeInfo>>

    // Crea un dispositivo del tipo dado (usa su id real de catálogo) y lo asocia a la habitación.
    // qrId opcional: si viene de un QR, se persiste en metadata para reconocer el device al reescanear.
    suspend fun createDevice(
        roomId: String,
        type: DeviceTypeInfo,
        name: String,
        qrId: String? = null,
    ): AppResult<Device>

    // Renombra un dispositivo preservando su metadata
    suspend fun renameDevice(deviceId: String, name: String): AppResult<Device>

    // Borra un dispositivo (DELETE /devices/{deviceId})
    suspend fun deleteDevice(deviceId: String): AppResult<Unit>

    // Enciende/apaga (o lock/unlock) eligiendo la acción de toggle según el catálogo del tipo
    suspend fun toggleDevice(device: Device): AppResult<Device>

    // Ajusta el valor primario del tipo (brillo/temperatura/volumen) con su acción posicional
    suspend fun setDeviceValue(device: Device, value: Int): AppResult<Device>

    // Ejecuta cualquier acción del catálogo con parámetros posicionales y refresca el estado real.
    // Si la acción es "obtener estado" (getState) se resuelve como refresco, nunca como PATCH.
    // Devuelve el device refrescado y, para acciones de solo lectura, el cuerpo de respuesta a mostrar.
    suspend fun executeDeviceAction(
        device: Device,
        actionName: String,
        args: List<ActionArg> = emptyList(),
    ): AppResult<DeviceActionResult>

    // Refresca el estado real del dispositivo (consulta GET, sin mutar) — usado por "obtener estado".
    suspend fun refreshDevice(device: Device): AppResult<Device>

    // Acciones que admite un tipo según el catálogo; usado al armar rutinas
    suspend fun getActionsFor(type: DeviceTypeInfo): AppResult<List<DeviceActionInfo>>
}
