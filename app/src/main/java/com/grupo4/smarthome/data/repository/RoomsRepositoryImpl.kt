package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.ApiError
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.data.remote.api.RoomsApi
import com.grupo4.smarthome.data.remote.dto.room.EntityRefDto
import com.grupo4.smarthome.data.remote.dto.room.RoomCreateDto
import com.grupo4.smarthome.data.remote.dto.room.RoomDto
import com.grupo4.smarthome.data.remote.dto.room.RoomUpdateDto
import com.grupo4.smarthome.data.remote.mapper.buildRoomMetadata
import com.grupo4.smarthome.data.remote.mapper.toDomain
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.builtins.ListSerializer

class RoomsRepositoryImpl(
    private val roomsApi: RoomsApi,
    private val sessionManager: SessionManager,
) : RoomsRepository {
    private var cachedRooms: List<Room> = emptyList()

    override suspend fun getRooms(homeId: String): AppResult<List<Room>> =
        apiCall(sessionManager, { roomsApi.getRoomsByHome(homeId) }) { body ->
            parseRooms(body).also { cachedRooms = it }
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
                    home = EntityRefDto(homeId),
                    metadata = buildRoomMetadata(type = type, isFavorite = false),
                )
            )
            val body = response.bodyAsText()

            if (!response.status.isSuccess()) {
                return@runCatching AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }

            val created = ApiJson.Instance.decodeFromString(RoomDto.serializer(), body).toDomain()

            val roomsInHome = fetchRoomsOrNull(homeId)

            if (roomsInHome == null) {
                return@runCatching AppResult.Success(created)
            }

            cachedRooms = roomsInHome

            roomsInHome.firstOrNull { it.id == created.id }?.let {
                return@runCatching AppResult.Success(it)
            }

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
        }.expireSessionOnUnauthorized(sessionManager)

    override suspend fun renameRoom(
        roomId: String,
        name: String,
        type: RoomType,
    ): AppResult<Room> {
        val current = resolveRoom(roomId) ?: return AppResult.Error(ApiError(ErrorKind.NotFound))
        return apiCall(
            sessionManager,
            {
                roomsApi.updateRoom(
                    roomId,
                    RoomUpdateDto(
                        name = name.trim(),
                        metadata = buildRoomMetadata(type = type, isFavorite = current.isFavorite),
                    ),
                )
            },
            ::parseRoomAndCache,
        )
    }

    override suspend fun deleteRoom(homeId: String, roomId: String): AppResult<Unit> =
        apiCallUnit(sessionManager, { roomsApi.deleteRoom(roomId) }) {
            fetchRoomsOrNull(homeId)?.let { cachedRooms = it }
        }

    override suspend fun toggleRoomFavorite(roomId: String): AppResult<Room> {
        val current = resolveRoom(roomId) ?: return AppResult.Error(ApiError(ErrorKind.NotFound))
        return apiCall(
            sessionManager,
            {
                roomsApi.updateRoom(
                    roomId,
                    RoomUpdateDto(
                        name = current.name.trim(),
                        metadata = buildRoomMetadata(type = current.type, isFavorite = !current.isFavorite),
                    ),
                )
            },
            ::parseRoomAndCache,
        )
    }

    private fun parseRoomAndCache(body: String): Room {
        val room = ApiJson.Instance.decodeFromString(RoomDto.serializer(), body).toDomain()
        cachedRooms = if (cachedRooms.any { it.id == room.id }) {
            cachedRooms.map { if (it.id == room.id) room else it }
        } else {
            cachedRooms + room
        }
        return room
    }

    private suspend fun resolveRoom(roomId: String): Room? {
        cachedRooms.firstOrNull { it.id == roomId }?.let { return it }
        return apiFetchOrNull(sessionManager, { roomsApi.getRoom(roomId) }) { body ->
            ApiJson.Instance.decodeFromString(RoomDto.serializer(), body).toDomain()
        }
    }

    private suspend fun fetchRoomsOrNull(homeId: String): List<Room>? =
        apiFetchOrNull(sessionManager, { roomsApi.getRoomsByHome(homeId) }, ::parseRooms)

    private fun parseRooms(body: String): List<Room> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(RoomDto.serializer()), body)
            .map { it.toDomain() }
}
