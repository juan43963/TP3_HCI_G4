package com.grupo4.smarthome.data.remote.api

import com.grupo4.smarthome.core.network.ApiRoutes
import com.grupo4.smarthome.data.remote.dto.device.DeviceCreateDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonArray

class DevicesApi(
    private val client: HttpClient,
) {
    suspend fun getRoomDevices(roomId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.roomDevices(roomId)))

    suspend fun getDevice(deviceId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.deviceById(deviceId)))

    suspend fun createDevice(request: DeviceCreateDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.Devices)) {
            setBody(request)
        }

    suspend fun updateDevice(deviceId: String, request: DeviceUpdateDto): HttpResponse =
        client.put(ApiRoutes.resolve(ApiRoutes.deviceById(deviceId))) {
            setBody(request)
        }

    suspend fun deleteDevice(deviceId: String): HttpResponse =
        client.delete(ApiRoutes.resolve(ApiRoutes.deviceById(deviceId)))

    suspend fun addDeviceToRoom(roomId: String, deviceId: String): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.roomDeviceAssociation(roomId, deviceId)))

    suspend fun removeDeviceFromRoom(deviceId: String): HttpResponse =
        client.delete(ApiRoutes.resolve(ApiRoutes.roomDeviceRemoval(deviceId)))

    suspend fun getDeviceTypes(): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.DeviceTypes))

    suspend fun getDeviceType(deviceTypeId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.deviceTypeById(deviceTypeId)))

    suspend fun executeAction(deviceId: String, actionName: String, params: JsonArray): HttpResponse =
        client.patch(ApiRoutes.resolve(ApiRoutes.deviceAction(deviceId, actionName))) {
            setBody(params)
        }

    suspend fun getDeviceState(deviceId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.deviceState(deviceId)))

    suspend fun getLogs(limit: Int, offset: Int): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.deviceLogs(limit, offset)))
}
