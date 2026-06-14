package com.grupo4.smarthome.data.repository

import android.util.Log
import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.ApiError
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.ErrorMapper
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
    private val localActionTracker: LocalActionTracker = LocalActionTracker(),
) : DevicesRepository {
    // Catálogo de tipos cacheado en memoria (ya mapeado al dominio): resuelve type.id -> DeviceTypeInfo
    // al mapear dispositivos y sirve de fuente para crear y para la UI.
    private var deviceTypesCache: List<DeviceTypeInfo>? = null

    override suspend fun getLogs(limit: Int, offset: Int): AppResult<List<DeviceLog>> =
        runCatching {
            val response = devicesApi.getLogs(limit, offset)
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val dtos = ApiJson.Instance.decodeFromString(ListSerializer(DeviceLogDto.serializer()), body)
                AppResult.Success(dtos.mapNotNull { it.toDomain() })
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun getDeviceTypes(): AppResult<List<DeviceTypeInfo>> =
        runCatching {
            ensureDeviceTypes()?.let { AppResult.Success(it) } ?: catalogError()
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    override suspend fun getRoomDevices(roomId: String): AppResult<List<Device>> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            val response = devicesApi.getRoomDevices(roomId)
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                AppResult.Success(parseDevices(body, roomId, catalog))
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    override suspend fun createDevice(
        roomId: String,
        type: DeviceTypeInfo,
        name: String,
        qrId: String?,
    ): AppResult<Device> =
        runCatching {
            Log.d("CreateDeviceAudit", "CREATE_START roomId=$roomId type=${type.name} name=$name deviceId=unknown ts=${System.currentTimeMillis()}")
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            // El id del tipo ya viene validado (no-blank) desde el catálogo: lo usamos directo.
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
            Log.d("CreateDeviceAudit", "CREATE_RESPONSE deviceId=${created.id} ts=${System.currentTimeMillis()}")

            if (created.id.isNotBlank()) {
                localActionTracker.record(created.id, NotificationKind.DEVICE_CREATED)
                localActionTracker.record(created.id, NotificationKind.DEVICE_ACTION)
            }

            // Sin id no se puede confirmar nada más: devolvemos lo creado tal como vino
            if (created.id.isBlank()) {
                return@runCatching AppResult.Success(created)
            }

            // Refetch para confirmar que quedó asociado a la habitación (mismo criterio que rooms, ADR-0008)
            val devicesInRoom = fetchRoomDevicesOrNull(roomId, catalog)
            if (devicesInRoom == null) {
                return@runCatching AppResult.Success(created.copy(roomId = roomId))
            }
            devicesInRoom.firstOrNull { it.id == created.id }?.let {
                return@runCatching AppResult.Success(it)
            }

            // Refetch exitoso pero no aparece: recién ahí usamos el fallback de asociación
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

    override suspend fun renameDevice(deviceId: String, name: String): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            // Traemos el dispositivo actual para preservar metadata y roomId al editar el nombre.
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

    override suspend fun deleteDevice(deviceId: String): AppResult<Unit> =
        runCatching {
            localActionTracker.record(deviceId, NotificationKind.DEVICE_DELETED)
            val response = devicesApi.deleteDevice(deviceId)
            if (response.status.isSuccess()) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

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
            // "Obtener estado" no se ejecuta como PATCH: se resuelve como refresco (consulta GET).
            if (DeviceActionNames.isStateRefreshAction(actionName)) {
                return@runCatching refreshDevice(device).map { DeviceActionResult(it) }
            }
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            // Validamos que la acción exista en el catálogo del tipo y tomamos su casing EXACTO.
            val resolved = device.type.action(actionName)
                ?: return@runCatching AppResult.Error(ApiError(ErrorKind.Unknown))
            // Las acciones de solo lectura (getPlaylist, ...) no mutan estado: no las registramos como acción.
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
            // Para read-only capturamos el cuerpo (resultado a mostrar); las mutaciones no exponen texto.
            val resultText = if (readOnly) responseBody.takeIf { it.isNotBlank() } else null
            when (val refreshed = refetchDevice(device.id, device.roomId, catalog)) {
                is AppResult.Success -> AppResult.Success(DeviceActionResult(refreshed.data, resultText))
                // La acción ya se ejecutó OK; si el refetch falla pero había resultado, igual lo devolvemos
                // (sobre el device previo) para no perderlo. Si no, propagamos el error del refetch.
                is AppResult.Error ->
                    if (resultText != null) AppResult.Success(DeviceActionResult(device, resultText)) else refreshed
            }
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    override suspend fun refreshDevice(device: Device): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            // Refrescamos vía GET /devices/{id} (devuelve el device completo con su state). Es un GET,
            // nunca un PATCH: cubre el requisito de "obtener estado" sin depender de la forma de /state.
            refetchDevice(device.id, device.roomId, catalog)
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    override suspend fun getActionsFor(type: DeviceTypeInfo): AppResult<List<DeviceActionInfo>> =
        runCatching {
            if (type.actions.isNotEmpty()) return@runCatching AppResult.Success(type.actions)
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            AppResult.Success(catalog.byId(type.id)?.actions.orEmpty())
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    // --- Helpers de red ---

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
        runCatching {
            val response = devicesApi.getDevice(deviceId)
            if (response.status.isSuccess()) {
                ApiJson.Instance.decodeFromString(DeviceDto.serializer(), response.bodyAsText())
            } else {
                null
            }
        }.getOrNull()

    private suspend fun fetchRoomDevicesOrNull(roomId: String, catalog: List<DeviceTypeInfo>): List<Device>? =
        runCatching {
            val response = devicesApi.getRoomDevices(roomId)
            if (response.status.isSuccess()) parseDevices(response.bodyAsText(), roomId, catalog) else null
        }.getOrNull()

    private fun parseDevices(body: String, roomId: String, catalog: List<DeviceTypeInfo>): List<Device> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(DeviceDto.serializer()), body)
            // mapNotNull: descartamos dispositivos cuyo tipo no se pudo resolver, sin crashear
            .mapNotNull { it.toDomain({ catalog.byId(it) }, roomId) }

    // Carga el catálogo una vez y lo cachea; devuelve null si falla (lo traducimos a catalogError())
    private suspend fun ensureDeviceTypes(): List<DeviceTypeInfo>? {
        deviceTypesCache?.let { return it }
        return runCatching {
            val response = devicesApi.getDeviceTypes()
            if (response.status.isSuccess()) {
                ApiJson.Instance
                    .decodeFromString(ListSerializer(DeviceTypeDto.serializer()), response.bodyAsText())
                    .mapNotNull { it.toDomain() }
                    .also { deviceTypesCache = it }
            } else {
                null
            }
        }.getOrNull()
    }

    private fun catalogError(): AppResult<Nothing> =
        AppResult.Error(ApiError(ErrorKind.Server))

    private fun List<DeviceTypeInfo>.byId(typeId: String?): DeviceTypeInfo? =
        firstOrNull { it.id == typeId }

    // Elige la acción de toggle existente en el catálogo del tipo (no hardcodeamos sin verificar);
    // devuelve el nombre con el casing exacto del catálogo o null si el tipo no expone un toggle.
    private fun resolveToggleActionName(device: Device): String? {
        val type = device.type
        if (type.isDoor) {
            val target = if (device.locked == true) DeviceActionNames.UNLOCK else DeviceActionNames.LOCK
            return type.action(target)?.name
        }
        // Alarm arm/disarm actions require a security code, so they cannot be resolved as a no-arg toggle.
        if (type.canonicalName == DeviceTypeNames.ALARM) return null
        val candidates = if (device.isOn) DeviceActionNames.OFF_CANDIDATES else DeviceActionNames.ON_CANDIDATES
        return candidates.firstNotNullOfOrNull { type.action(it)?.name }
    }
}

// Transforma el dato de un Success preservando el Error tal cual (Error es AppResult<Nothing>).
private inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}

// Colapsa el resultado de una acción a solo el device, para toggle/setDeviceValue.
private fun AppResult<DeviceActionResult>.toDevice(): AppResult<Device> = when (this) {
    is AppResult.Success -> AppResult.Success(data.device)
    is AppResult.Error -> this
}
