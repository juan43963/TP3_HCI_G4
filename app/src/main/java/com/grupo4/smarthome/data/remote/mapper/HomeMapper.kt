package com.grupo4.smarthome.data.remote.mapper

import com.grupo4.smarthome.data.model.Home
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.data.model.SharedUser
import com.grupo4.smarthome.data.remote.dto.home.HomeDto
import com.grupo4.smarthome.data.remote.dto.home.SharedUserDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put

private const val KEY_TYPE = "type"
private const val KEY_PHOTO = "photo"
private const val KEY_OWNER_NAME = "ownerName"
private const val KEY_OWNER_EMAIL = "ownerEmail"

fun HomeDto.toDomain(): Home =
    Home(
        id = id.orEmpty(),
        name = name,
        rooms = emptyList(),
        type = metadata.readHomeType(),
        photoIndex = metadata.readPhotoIndex(),
        ownerName = metadata.readString(KEY_OWNER_NAME),
        ownerEmail = metadata.readString(KEY_OWNER_EMAIL),
    )

fun SharedUserDto.toDomain(): SharedUser? {
    val safeEmail = email?.takeIf { it.isNotBlank() } ?: return null
    return SharedUser(
        id = id.orEmpty(),
        email = safeEmail,
        name = name.orEmpty(),
    )
}


fun buildHomeMetadata(
    type: HomeType,
    photoIndex: Int?,
    existing: JsonObject? = null,
): JsonObject = buildJsonObject {
    existing?.forEach { (key, value) ->
        if (key != KEY_TYPE && key != KEY_PHOTO) put(key, value)
    }
    put(KEY_TYPE, type.name.lowercase())
    photoIndex?.let { put(KEY_PHOTO, it) }
}

private fun JsonObject?.readHomeType(): HomeType {
    val raw = (this?.get(KEY_TYPE) as? JsonPrimitive)?.content?.uppercase()
    return HomeType.entries.firstOrNull { it.name == raw } ?: HomeType.OTRO
}

private fun JsonObject?.readPhotoIndex(): Int? =
    (this?.get(KEY_PHOTO) as? JsonPrimitive)?.intOrNull

private fun JsonObject?.readString(key: String): String? =
    (this?.get(key) as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
