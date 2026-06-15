package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.ApiError
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.model.ActionArg
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionNames
import com.grupo4.smarthome.data.model.DeviceActionResult
import com.grupo4.smarthome.data.model.DeviceLog
import com.grupo4.smarthome.data.model.DeviceTypeInfo
import com.grupo4.smarthome.data.model.DeviceTypeNames
import com.grupo4.smarthome.data.model.NotificationKind
import com.grupo4.smarthome.data.remote.api.DevicesApi
import com.grupo4.smarthome.data.remote.dto.device.DeviceCreateDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceLogDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceTypeDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceUpdateDto
import com.grupo4.smarthome.data.remote.dto.room.EntityRefDto
import com.grupo4.smarthome.data.remote.mapper.buildDeviceMetadata
import com.grupo4.smarthome.data.remote.mapper.toDomain
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class DevicesRepositoryImpl(
    private val devicesApi: DevicesApi,
    private val sessionManager: SessionManager,
    private val localActionTracker: LocalActionTracker = LocalActionTracker(),
) : DevicesRepository {
    private var deviceTypesCache: List<DeviceTypeInfo>? = null

    override suspend fun getLogs(limit: Int, offset: Int): AppResult<List<DeviceLog>> =
        apiCall(sessionManager, { devicesApi.getLogs(limit, offset) }) { body ->
            ApiJson.Instance.decodeFromString(ListSerializer(DeviceLogDto.serializer()), body)
                .mapNotNull { it.toDomain() }
        }

    override suspend fun getDeviceTypes(): AppResult<List<DeviceTypeInfo>> =
        ensureDeviceTypes()?.let { AppResult.Success(it) } ?: catalogError()

    override suspend fun getRoomDevices(roomId: String): AppResult<List<Device>> {
        val catalog = ensureDeviceTypes() ?: return catalogError()
        return apiCall(sessionManager, { devicesApi.getRoomDevices(roomId) }) { body ->
            parseDevices(body, roomId, catalog)
        }
    }

    override suspend fun createDevice(
        roomId: String,
        type: DeviceTypeInfo,
        name: String,
        qrId: String?,
    ): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            val response = devicesApi.createDevice(
                DeviceCreateDto(
                    name = name.trim(),
                    type = EntityRefDto(type.id),
                    room = EntityRefDto(roomId),
                    metadata = buildDeviceMetadata(previous = null, qrId = qrId),
                )
            )
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runCatching AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }

            val created = ApiJson.Instance.decodeFromString(DeviceDto.serializer(), body)
                .toDomain({ catalog.byId(it) }, roomId)
                ?: return@runCatching AppResult.Error(ApiError(ErrorKind.Unknown))

            if (created.id.isNotBlank()) {
                localActionTracker.record(created.id, NotificationKind.DEVICE_CREATED)
                localActionTracker.record(created.id, NotificationKind.DEVICE_ACTION)
            }

            if (created.id.isBlank()) {
                return@runCatching AppResult.Success(created)
            }

            val devicesInRoom = fetchRoomDevicesOrNull(roomId, catalog)
            if (devicesInRoom == null) {
                return@runCatching AppResult.Success(created.copy(roomId = roomId))
            }
            devicesInRoom.firstOrNull { it.id == created.id }?.let {
                return@runCatching AppResult.Success(it)
            }

            val associateResponse = devicesApi.addDeviceToRoom(roomId, created.id)
            if (!associateResponse.status.isSuccess()) {
                return@runCatching AppResult.Error(
                    ErrorMapper.fromHttp(associateResponse.status, associateResponse.bodyAsText())
                )
            }
            val afterAssociate = fetchRoomDevicesOrNull(roomId, catalog)
            AppResult.Success(
                afterAssociate?.firstOrNull { it.id == created.id } ?: created.copy(roomId = roomId)
            )
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun renameDevice(deviceId: String, name: String): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            val current = fetchDeviceDto(deviceId)
                ?: return@runCatching AppResult.Error(ApiError(ErrorKind.NotFound))
            localActionTracker.record(deviceId, NotificationKind.DEVICE_UPDATED)
            val response = devicesApi.updateDevice(
                deviceId,
                DeviceUpdateDto(
                    name = name.trim(),
                    metadata = current.metadata ?: buildJsonObject { },
                )
            )
            parseDeviceResponse(response, catalog, current.room?.id.orEmpty())
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun deleteDevice(deviceId: String): AppResult<Unit> {
        localActionTracker.record(deviceId, NotificationKind.DEVICE_DELETED)
        return apiCallUnit(sessionManager, { devicesApi.deleteDevice(deviceId) })
    }

    override suspend fun toggleDevice(device: Device): AppResult<Device> {
        val actionName = resolveToggleActionName(device)
            ?: return AppResult.Error(ApiError(ErrorKind.Unknown))
        return executeDeviceAction(device, actionName).toDevice()
    }

    override suspend fun setDeviceValue(device: Device, value: Int): AppResult<Device> {
        val actionName = DeviceActionNames.primaryValueAction(device.type.canonicalName)
            ?: return AppResult.Error(ApiError(ErrorKind.Unknown))
        return executeDeviceAction(device, actionName, listOf(ActionArg.IntArg(value))).toDevice()
    }

    override suspend fun executeDeviceAction(
        device: Device,
        actionName: String,
        args: List<ActionArg>,
    ): AppResult<DeviceActionResult> =
        runCatching {
            if (DeviceActionNames.isStateRefreshAction(actionName)) {
                return@runCatching refreshDevice(device).map { DeviceActionResult(it) }
            }
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            val resolved = device.type.action(actionName)
                ?: return@runCatching AppResult.Error(ApiError(ErrorKind.Unknown))
            val readOnly = DeviceActionNames.isReadOnlyAction(resolved.name)
            if (!readOnly) {
                localActionTracker.record(device.id, NotificationKind.DEVICE_ACTION)
            }
            val body = buildJsonArray {
                args.forEach { arg ->
                    when (arg) {
                        is ActionArg.IntArg -> add(arg.value)
                        is ActionArg.NumberArg -> add(arg.value)
                        is ActionArg.StringArg -> add(arg.value)
                        is ActionArg.BoolArg -> add(arg.value)
                    }
                }
            }
            val response = devicesApi.executeAction(device.id, resolved.name, body)
            val responseBody = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runCatching AppResult.Error(ErrorMapper.fromHttp(response.status, responseBody))
            }
            val resultText = if (readOnly) responseBody.takeIf { it.isNotBlank() } else null
            when (val refreshed = refetchDevice(device.id, device.roomId, catalog)) {
                is AppResult.Success -> AppResult.Success(DeviceActionResult(refreshed.data, resultText))
                is AppResult.Error ->
                    if (resultText != null) AppResult.Success(DeviceActionResult(device, resultText)) else refreshed
            }
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun refreshDevice(device: Device): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            refetchDevice(device.id, device.roomId, catalog)
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun getActionsFor(type: DeviceTypeInfo): AppResult<List<DeviceActionInfo>> {
        if (type.actions.isNotEmpty()) return AppResult.Success(type.actions)
        val catalog = ensureDeviceTypes() ?: return catalogError()
        return AppResult.Success(catalog.byId(type.id)?.actions.orEmpty())
    }


    private suspend fun refetchDevice(
        deviceId: String,
        fallbackRoomId: String,
        catalog: List<DeviceTypeInfo>,
    ): AppResult<Device> {
        val response = devicesApi.getDevice(deviceId)
        return parseDeviceResponse(response, catalog, fallbackRoomId)
    }

    private suspend fun parseDeviceResponse(
        response: HttpResponse,
        catalog: List<DeviceTypeInfo>,
        fallbackRoomId: String,
    ): AppResult<Device> {
        val body = response.bodyAsText()
        return if (response.status.isSuccess()) {
            val device = ApiJson.Instance.decodeFromString(DeviceDto.serializer(), body)
                .toDomain({ catalog.byId(it) }, fallbackRoomId)
                ?: return AppResult.Error(ApiError(ErrorKind.Unknown))
            AppResult.Success(device)
        } else {
            AppResult.Error(ErrorMapper.fromHttp(response.status, body))
        }
    }

    private suspend fun fetchDeviceDto(deviceId: String): DeviceDto? =
        apiFetchOrNull(sessionManager, { devicesApi.getDevice(deviceId) }) { body ->
            ApiJson.Instance.decodeFromString(DeviceDto.serializer(), body)
        }

    private suspend fun fetchRoomDevicesOrNull(roomId: String, catalog: List<DeviceTypeInfo>): List<Device>? =
        apiFetchOrNull(sessionManager, { devicesApi.getRoomDevices(roomId) }) { body ->
            parseDevices(body, roomId, catalog)
        }

    private fun parseDevices(body: String, roomId: String, catalog: List<DeviceTypeInfo>): List<Device> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(DeviceDto.serializer()), body)
            .mapNotNull { it.toDomain({ catalog.byId(it) }, roomId) }

    private suspend fun ensureDeviceTypes(): List<DeviceTypeInfo>? {
        deviceTypesCache?.let { return it }
        return apiFetchOrNull(sessionManager, { devicesApi.getDeviceTypes() }) { body ->
            ApiJson.Instance
                .decodeFromString(ListSerializer(DeviceTypeDto.serializer()), body)
                .mapNotNull { it.toDomain() }
        }?.also { deviceTypesCache = it }
    }

    private fun catalogError(): AppResult<Nothing> =
        AppResult.Error(ApiError(ErrorKind.Server))

    private fun List<DeviceTypeInfo>.byId(typeId: String?): DeviceTypeInfo? =
        firstOrNull { it.id == typeId }

    private fun resolveToggleActionName(device: Device): String? {
        val type = device.type
        if (type.isDoor) {
            val target = if (device.locked == true) DeviceActionNames.UNLOCK else DeviceActionNames.LOCK
            return type.action(target)?.name
        }
        if (type.canonicalName == DeviceTypeNames.ALARM) return null
        val candidates = if (device.isOn) DeviceActionNames.OFF_CANDIDATES else DeviceActionNames.ON_CANDIDATES
        return candidates.firstNotNullOfOrNull { type.action(it)?.name }
    }
}

private inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}

private fun AppResult<DeviceActionResult>.toDevice(): AppResult<Device> = when (this) {
    is AppResult.Success -> AppResult.Success(data.device)
    is AppResult.Error -> this
}
