package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.ActionArg
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionResult
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.DeviceTypeInfo

interface DevicesRepository {
    suspend fun getRoomDevices(roomId: String): AppResult<List<Device>>

    suspend fun getLogs(limit: Int = 50, offset: Int = 0): AppResult<List<DeviceLog>>

    suspend fun getDeviceTypes(): AppResult<List<DeviceTypeInfo>>

    suspend fun createDevice(
        roomId: String,
        type: DeviceTypeInfo,
        name: String,
        qrId: String? = null,
    ): AppResult<Device>

    suspend fun renameDevice(deviceId: String, name: String): AppResult<Device>

    suspend fun deleteDevice(deviceId: String): AppResult<Unit>

    suspend fun toggleDevice(device: Device): AppResult<Device>

    suspend fun setDeviceValue(device: Device, value: Int): AppResult<Device>

    suspend fun executeDeviceAction(
        device: Device,
        actionName: String,
        args: List<ActionArg> = emptyList(),
    ): AppResult<DeviceActionResult>

    suspend fun refreshDevice(device: Device): AppResult<Device>

    suspend fun getActionsFor(type: DeviceTypeInfo): AppResult<List<DeviceActionInfo>>
}
