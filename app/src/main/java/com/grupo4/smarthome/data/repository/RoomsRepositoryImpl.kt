package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.ApiError
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.data.remote.api.RoomsApi
import com.grupo4.smarthome.data.remote.dto.room.EntityRefDto
import com.grupo4.smarthome.data.remote.dto.room.RoomCreateDto
import com.grupo4.smarthome.data.remote.dto.room.RoomDto
import com.grupo4.smarthome.data.remote.dto.room.RoomUpdateDto
import com.grupo4.smarthome.data.remote.mapper.buildRoomMetadata
import com.grupo4.smarthome.data.remote.mapper.toDomain
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.builtins.ListSerializer

class RoomsRepositoryImpl(
    private val roomsApi: RoomsApi,
) : RoomsRepository {
    // Caché en memoria (ADR-0005): preserva type/favorite para reconstruir metadata en updates
    private var cachedRooms: List<Room> = emptyList()

    override suspend fun getRooms(homeId: String): AppResult<List<Room>> =
        runCatching {
            val response = roomsApi.getRoomsByHome(homeId)
            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                val rooms = parseRooms(body)
                cachedRooms = rooms
                AppResult.Success(rooms)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun createRoom(
        homeId: String,
        name: String,
        type: RoomType,
    ): AppResult<Room> =
        runCatching {
            val response = roomsApi.createRoom(
                RoomCreateDto(
                    name = name.trim(),
                    // Siempre con id no nulo para asociar la habitación al hogar al crearla
                    home = EntityRefDto(homeId),
                    metadata = buildRoomMetadata(type = type, isFavorite = false),
                )
            )
            val body = response.bodyAsText()

            if (!response.status.isSuccess()) {
                return@runCatching AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }

            val created = ApiJson.Instance.decodeFromString(RoomDto.serializer(), body).toDomain()

            // Refetch para confirmar que quedó asociada al hogar (ADR-0008)
            val roomsInHome = fetchRoomsOrNull(homeId)

            // Si el refetch falló/null no podemos confirmar nada: nos quedamos con la room creada
            if (roomsInHome == null) {
                return@runCatching AppResult.Success(created)
            }

            cachedRooms = roomsInHome

            // El refetch confirmó que ya está asociada: devolvemos la versión del hogar
            roomsInHome.firstOrNull { it.id == created.id }?.let {
                return@runCatching AppResult.Success(it)
            }

            // Refetch exitoso pero no aparece: recién ahí usamos el fallback de asociación
            if (created.id.isBlank()) {
                return@runCatching AppResult.Success(created)
            }
            val associateResponse = roomsApi.addRoomToHome(homeId, created.id)
            if (!associateResponse.status.isSuccess()) {
                return@runCatching AppResult.Error(
                    ErrorMapper.fromHttp(associateResponse.status, associateResponse.bodyAsText())
                )
            }
            val afterAssociate = fetchRoomsOrNull(homeId)
            afterAssociate?.let { cachedRooms = it }
            AppResult.Success(afterAssociate?.firstOrNull { it.id == created.id } ?: created)
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun renameRoom(
        roomId: String,
        name: String,
        type: RoomType,
    ): AppResult<Room> =
        runCatching {
            // Necesitamos el estado actual para no perder el favorito guardado en metadata
            val current = resolveRoom(roomId)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.NotFound, "No se encontró la habitación.")
                )
            val response = roomsApi.updateRoom(
                roomId,
                RoomUpdateDto(
                    name = name.trim(),
                    metadata = buildRoomMetadata(
                        type = type,
                        isFavorite = current.isFavorite,
                    ),
                )
            )
            parseRoomResponse(response)
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun deleteRoom(homeId: String, roomId: String): AppResult<Unit> =
        runCatching {
            val response = roomsApi.deleteRoom(roomId)
            if (response.status.isSuccess()) {
                // Refetch para reflejar el estado real del back-end (ADR-0008)
                fetchRoomsOrNull(homeId)?.let { cachedRooms = it }
                AppResult.Success(Unit)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun toggleRoomFavorite(roomId: String): AppResult<Room> =
        runCatching {
            // Necesitamos el estado actual completo para no perder nombre ni tipo al togglear
            val current = resolveRoom(roomId)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.NotFound, "No se encontró la habitación.")
                )
            val response = roomsApi.updateRoom(
                roomId,
                RoomUpdateDto(
                    name = current.name.trim(),
                    metadata = buildRoomMetadata(
                        type = current.type,
                        isFavorite = !current.isFavorite,
                    ),
                )
            )
            parseRoomResponse(response)
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    // Parsea la respuesta de un único Room y actualiza la caché; mapea el error si no fue 2xx
    private suspend fun parseRoomResponse(response: HttpResponse): AppResult<Room> {
        val body = response.bodyAsText()
        return if (response.status.isSuccess()) {
            val room = ApiJson.Instance.decodeFromString(RoomDto.serializer(), body).toDomain()
            cachedRooms = if (cachedRooms.any { it.id == room.id }) {
                cachedRooms.map { if (it.id == room.id) room else it }
            } else {
                cachedRooms + room
            }
            AppResult.Success(room)
        } else {
            AppResult.Error(ErrorMapper.fromHttp(response.status, body))
        }
    }

    // Devuelve la room cacheada o, si falta, la trae con GET /rooms/{roomId} para no perder type/favorite
    private suspend fun resolveRoom(roomId: String): Room? {
        cachedRooms.firstOrNull { it.id == roomId }?.let { return it }
        return runCatching {
            val response = roomsApi.getRoom(roomId)
            if (response.status.isSuccess()) {
                ApiJson.Instance.decodeFromString(RoomDto.serializer(), response.bodyAsText()).toDomain()
            } else {
                null
            }
        }.getOrNull()
    }

    // Refetch silencioso usado tras crear/borrar; devuelve null ante cualquier fallo
    private suspend fun fetchRoomsOrNull(homeId: String): List<Room>? =
        runCatching {
            val response = roomsApi.getRoomsByHome(homeId)
            if (response.status.isSuccess()) parseRooms(response.bodyAsText()) else null
        }.getOrNull()

    private fun parseRooms(body: String): List<Room> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(RoomDto.serializer()), body)
            .map { it.toDomain() }
}
