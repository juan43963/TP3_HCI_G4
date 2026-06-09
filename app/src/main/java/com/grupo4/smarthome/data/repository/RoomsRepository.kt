package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType

interface RoomsRepository {
    suspend fun getRooms(homeId: String): AppResult<List<Room>>

    suspend fun createRoom(homeId: String, name: String, type: RoomType): AppResult<Room>

    suspend fun renameRoom(roomId: String, name: String, type: RoomType): AppResult<Room>

    suspend fun deleteRoom(homeId: String, roomId: String): AppResult<Unit>

    suspend fun toggleRoomFavorite(roomId: String): AppResult<Room>
}
