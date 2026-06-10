package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.ApiError
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceActionInfo
import com.grupo4.smarthome.data.model.DeviceActionParamInfo
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.remote.api.DevicesApi
import com.grupo4.smarthome.data.remote.dto.device.DeviceCreateDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceTypeDto
import com.grupo4.smarthome.data.remote.dto.device.DeviceUpdateDto
import com.grupo4.smarthome.data.remote.dto.room.EntityRefDto
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
) : DevicesRepository {
    // Catálogo de tipos cacheado en memoria: lo necesitamos tanto para resolver el deviceTypeId al
    // crear como para mapear type.id -> enum local y para saber qué acciones admite cada tipo.
    private var deviceTypesCache: List<DeviceTypeDto>? = null

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
        type: DeviceType,
        name: String,
    ): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            val typeId = catalog.typeIdFor(type)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.Unknown)
                )

            val response = devicesApi.createDevice(
                DeviceCreateDto(
                    name = name.trim(),
                    type = EntityRefDto(typeId),
                    room = EntityRefDto(roomId),
                )
            )
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runCatching AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }

            val created = ApiJson.Instance.decodeFromString(DeviceDto.serializer(), body)
                .toDomain({ catalog.localTypeFor(it) }, roomId)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.Unknown)
                )

            // Sin id no se puede confirmar nada más: devolvemos lo creado tal como vino
            if (created.id.isBlank()) {
                return@runCatching AppResult.Success(created)
            }

            // Refetch para confirmar que quedó asociado a la habitación (mismo criterio que rooms, ADR-0008)
            val devicesInRoom = fetchRoomDevicesOrNull(roomId, catalog)

            // Si el refetch falló/null no podemos confirmar: nos quedamos con el dispositivo creado
            if (devicesInRoom == null) {
                return@runCatching AppResult.Success(created.copy(roomId = roomId))
            }

            // El refetch confirmó que ya está asociado: devolvemos la versión de la habitación
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

            // Asociación OK: refetch de nuevo y devolvemos el dispositivo real de la habitación
            val afterAssociate = fetchRoomDevicesOrNull(roomId, catalog)
            AppResult.Success(
                afterAssociate?.firstOrNull { it.id == created.id } ?: created.copy(roomId = roomId)
            )
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    override suspend fun renameDevice(deviceId: String, name: String): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            // Traemos el dispositivo actual para preservar metadata y roomId al editar el nombre.
            // Si no se pudo leer, abortamos con NotFound en lugar de pisar la metadata con un objeto vacío.
            val current = fetchDeviceDto(deviceId)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.NotFound)
                )
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
            val response = devicesApi.deleteDevice(deviceId)
            if (response.status.isSuccess()) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    override suspend fun toggleDevice(device: Device): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            val actions = catalog.actionsFor(device.type)
            val actionName = resolveToggleAction(device, actions)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.Unknown)
                )
            val response = devicesApi.executeAction(device.id, actionName, buildJsonArray { })
            if (!response.status.isSuccess()) {
                return@runCatching AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
            refetchDevice(device.id, device.roomId, catalog)
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    override suspend fun setDeviceValue(device: Device, value: Int): AppResult<Device> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            val actionName = valueActionFor(device.type)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.Unknown)
                )
            if (actionName !in catalog.actionsFor(device.type)) {
                return@runCatching AppResult.Error(
                    ApiError(ErrorKind.Unknown)
                )
            }
            val response = devicesApi.executeAction(device.id, actionName, buildJsonArray { add(value) })
            if (!response.status.isSuccess()) {
                return@runCatching AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
            refetchDevice(device.id, device.roomId, catalog)
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    override suspend fun getActionsFor(type: DeviceType): AppResult<List<DeviceActionInfo>> =
        runCatching {
            val catalog = ensureDeviceTypes() ?: return@runCatching catalogError()
            val actions = catalog.firstOrNull { it.name?.lowercase() in type.apiNames() }
                ?.actions
                ?.mapNotNull { action ->
                    action.name?.let { name ->
                        DeviceActionInfo(
                            name = name,
                            params = action.params.mapNotNull { param ->
                                param.name?.let { DeviceActionParamInfo(it, param.type.orEmpty(), param.min, param.max) }
                            },
                        )
                    }
                }
                .orEmpty()
            AppResult.Success(actions)
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }

    // --- Helpers de red ---

    // Trae el estado actualizado tras una acción (GET /devices/{id}) y lo mapea al dominio
    private suspend fun refetchDevice(
        deviceId: String,
        fallbackRoomId: String,
        catalog: List<DeviceTypeDto>,
    ): AppResult<Device> {
        val response = devicesApi.getDevice(deviceId)
        return parseDeviceResponse(response, catalog, fallbackRoomId)
    }

    private suspend fun parseDeviceResponse(
        response: HttpResponse,
        catalog: List<DeviceTypeDto>,
        fallbackRoomId: String,
    ): AppResult<Device> {
        val body = response.bodyAsText()
        return if (response.status.isSuccess()) {
            val device = ApiJson.Instance.decodeFromString(DeviceDto.serializer(), body)
                .toDomain({ catalog.localTypeFor(it) }, fallbackRoomId)
                ?: return AppResult.Error(
                    ApiError(ErrorKind.Unknown)
                )
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

    // Refetch silencioso de los dispositivos de una habitación; devuelve null ante cualquier fallo
    private suspend fun fetchRoomDevicesOrNull(roomId: String, catalog: List<DeviceTypeDto>): List<Device>? =
        runCatching {
            val response = devicesApi.getRoomDevices(roomId)
            if (response.status.isSuccess()) parseDevices(response.bodyAsText(), roomId, catalog) else null
        }.getOrNull()

    private fun parseDevices(body: String, roomId: String, catalog: List<DeviceTypeDto>): List<Device> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(DeviceDto.serializer()), body)
            // mapNotNull: descartamos dispositivos cuyo tipo no se pudo resolver, sin crashear
            .mapNotNull { it.toDomain({ catalog.localTypeFor(it) }, roomId) }

    // Carga el catálogo una vez y lo cachea; devuelve null si falla (lo traducimos a catalogError())
    private suspend fun ensureDeviceTypes(): List<DeviceTypeDto>? {
        deviceTypesCache?.let { return it }
        return runCatching {
            val response = devicesApi.getDeviceTypes()
            if (response.status.isSuccess()) {
                ApiJson.Instance
                    .decodeFromString(ListSerializer(DeviceTypeDto.serializer()), response.bodyAsText())
                    .also { deviceTypesCache = it }
            } else {
                null
            }
        }.getOrNull()
    }

    private fun catalogError(): AppResult<Nothing> =
        AppResult.Error(ApiError(ErrorKind.Server))

    // --- Resolución tipo/acción contra el catálogo ---

    private fun List<DeviceTypeDto>.localTypeFor(typeId: String?): DeviceType? =
        firstOrNull { it.id == typeId }?.let { apiNameToLocalType(it.name) }

    private fun List<DeviceTypeDto>.typeIdFor(type: DeviceType): String? =
        firstOrNull { it.name?.lowercase() in type.apiNames() }?.id

    private fun List<DeviceTypeDto>.actionsFor(type: DeviceType): List<String> =
        firstOrNull { it.name?.lowercase() in type.apiNames() }
            ?.actions
            ?.mapNotNull { it.name }
            .orEmpty()

    // Elige la acción de toggle existente en el catálogo del tipo (no hardcodeamos sin verificar)
    private fun resolveToggleAction(device: Device, actions: List<String>): String? {
        if (device.type == DeviceType.DOOR) {
            val target = if (device.locked == true) "unlock" else "lock"
            return target.takeIf { it in actions }
        }
        val onCandidates = listOf("turnOn", "open", "play", "start", "armAway", "arm")
        val offCandidates = listOf("turnOff", "close", "pause", "stop", "dock", "disarm")
        val candidates = if (device.isOn) offCandidates else onCandidates
        return candidates.firstOrNull { it in actions }
    }

    private fun valueActionFor(type: DeviceType): String? = when (type) {
        DeviceType.LIGHT -> "setBrightness"
        DeviceType.AC -> "setTemperature"
        DeviceType.SPEAKER -> "setVolume"
        else -> null
    }

    // Alias del enum local contra los `name` del catálogo de la API (case-insensitive).
    // Incluimos variantes por si el backend nombra distinto (p. ej. AC / air conditioner).
    private fun DeviceType.apiNames(): List<String> = when (this) {
        DeviceType.LIGHT -> listOf("lamp", "light")
        DeviceType.FRIDGE -> listOf("refrigerator", "fridge")
        DeviceType.AC -> listOf("ac", "air_conditioner", "air conditioner", "airconditioner")
        else -> listOf(name.lowercase())
    }

    private fun apiNameToLocalType(name: String?): DeviceType? = when (name?.lowercase()) {
        "lamp", "light" -> DeviceType.LIGHT
        "ac", "air_conditioner", "air conditioner", "airconditioner" -> DeviceType.AC
        "alarm" -> DeviceType.ALARM
        "blinds" -> DeviceType.BLINDS
        "door" -> DeviceType.DOOR
        "faucet" -> DeviceType.FAUCET
        "oven" -> DeviceType.OVEN
        "refrigerator", "fridge" -> DeviceType.FRIDGE
        "speaker" -> DeviceType.SPEAKER
        "vacuum" -> DeviceType.VACUUM
        else -> null
    }
}
