package com.grupo4.smarthome.data.remote.mapper

import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.data.remote.dto.room.RoomDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val KEY_ROOM_TYPE = "roomType"
private const val KEY_FAVORITE = "favorite"

fun RoomDto.toDomain(): Room =
    Room(
        id = id.orEmpty(),
        name = name,
        type = metadata.readRoomType(),
        isFavorite = metadata.readFavorite(),
        devices = emptyList(),
    )

fun buildRoomMetadata(
    type: RoomType,
    isFavorite: Boolean,
    existing: JsonObject? = null,
): JsonObject = buildJsonObject {
    existing?.forEach { (key, value) ->
        if (key != KEY_ROOM_TYPE && key != KEY_FAVORITE) put(key, value)
    }
    put(KEY_ROOM_TYPE, type.name.lowercase())
    put(KEY_FAVORITE, isFavorite)
}

private fun JsonObject?.readRoomType(): RoomType {
    val raw = (this?.get(KEY_ROOM_TYPE) as? JsonPrimitive)?.content?.uppercase()
    return RoomType.entries.firstOrNull { it.name == raw } ?: RoomType.OTHER
}

private fun JsonObject?.readFavorite(): Boolean =
    (this?.get(KEY_FAVORITE) as? JsonPrimitive)?.booleanOrNull ?: false
