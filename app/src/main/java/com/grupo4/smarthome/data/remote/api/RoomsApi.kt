package com.grupo4.smarthome.data.remote.api

import com.grupo4.smarthome.core.network.ApiRoutes
import com.grupo4.smarthome.data.remote.dto.room.RoomCreateDto
import com.grupo4.smarthome.data.remote.dto.room.RoomUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class RoomsApi(
    private val client: HttpClient,
) {
    suspend fun getRoomsByHome(homeId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.homeRooms(homeId)))

    suspend fun getRoom(roomId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.roomById(roomId)))

    suspend fun createRoom(request: RoomCreateDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.Rooms)) {
            setBody(request)
        }

    suspend fun updateRoom(roomId: String, request: RoomUpdateDto): HttpResponse =
        client.put(ApiRoutes.resolve(ApiRoutes.roomById(roomId))) {
            setBody(request)
        }

    suspend fun deleteRoom(roomId: String): HttpResponse =
        client.delete(ApiRoutes.resolve(ApiRoutes.roomById(roomId)))

    // Asocia una habitación ya creada a un hogar (fallback si POST /rooms no la asoció)
    suspend fun addRoomToHome(homeId: String, roomId: String): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.homeRoomAssociation(homeId, roomId)))

    // Disponible para fase 2 (dispositivos por habitación)
    suspend fun getRoomDevices(roomId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.roomDevices(roomId)))
}
