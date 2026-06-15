package com.grupo4.smarthome.data.remote.mapper

import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory
import com.grupo4.smarthome.data.remote.dto.routine.RoutineActionDto
import com.grupo4.smarthome.data.remote.dto.routine.RoutineDeviceRefDto
import com.grupo4.smarthome.data.remote.dto.routine.RoutineDto
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

private const val KEY_CATEGORY = "category"
private const val KEY_TIME = "time"
private const val KEY_DAYS = "days"
private const val KEY_FAVORITE = "favorite"
private const val KEY_ENABLED = "enabled"
private const val KEY_HOME_ID = "homeId"

private const val DEFAULT_TIME = "08:00"

fun RoutineDto.toDomain(): Routine =
    Routine(
        id = id.orEmpty(),
        name = name,
        actions = actions.mapNotNull { it.toDomain() },
        category = metadata.readCategory(),
        time = metadata.readTime(),
        days = metadata.readDays(),
        favorite = metadata.readBoolean(KEY_FAVORITE, default = false),
        enabled = metadata.readBoolean(KEY_ENABLED, default = true),
        homeId = metadata.readRoutineHomeId(),
    )

private fun RoutineActionDto.toDomain(): RoutineAction? {
    val action = actionName ?: return null
    return RoutineAction(
        deviceId = device?.id.orEmpty(),
        deviceName = device?.name.orEmpty(),
        actionName = action,
        params = params?.map { (it as? JsonPrimitive)?.content.orEmpty() }.orEmpty(),
    )
}

fun Routine.toActionDtos(): List<RoutineActionDto> =
    actions.map { action ->
        RoutineActionDto(
            device = RoutineDeviceRefDto(id = action.deviceId),
            actionName = action.actionName,
            params = buildJsonArray {
                action.params.forEach { p -> p.toIntOrNull()?.let { add(it) } ?: add(p) }
            },
        )
    }

fun buildRoutineMetadata(
    category: RoutineCategory,
    time: String,
    days: List<Boolean>,
    favorite: Boolean,
    enabled: Boolean,
    homeId: String,
    existing: JsonObject? = null,
): JsonObject = buildJsonObject {
    val ownKeys = setOf(KEY_CATEGORY, KEY_TIME, KEY_DAYS, KEY_FAVORITE, KEY_ENABLED, KEY_HOME_ID)
    existing?.forEach { (key, value) -> if (key !in ownKeys) put(key, value) }
    put(KEY_CATEGORY, category.name.lowercase())
    put(KEY_TIME, time)
    putJsonArray(KEY_DAYS) { days.forEach { add(it) } }
    put(KEY_FAVORITE, favorite)
    put(KEY_ENABLED, enabled)
    put(KEY_HOME_ID, homeId)
}

fun JsonObject?.readRoutineHomeId(): String =
    (this?.get(KEY_HOME_ID) as? JsonPrimitive)?.content.orEmpty()

private fun JsonObject?.readCategory(): RoutineCategory {
    val raw = (this?.get(KEY_CATEGORY) as? JsonPrimitive)?.content?.uppercase()
    return RoutineCategory.entries.firstOrNull { it.name == raw } ?: RoutineCategory.CUSTOM
}

private fun JsonObject?.readTime(): String =
    (this?.get(KEY_TIME) as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() } ?: DEFAULT_TIME

private fun JsonObject?.readDays(): List<Boolean> {
    val array = this?.get(KEY_DAYS) as? JsonArray
    val parsed = array?.map { (it as? JsonPrimitive)?.booleanOrNull ?: false }
    return if (parsed != null && parsed.size == 7) parsed else List(7) { false }
}

private fun JsonObject?.readBoolean(key: String, default: Boolean): Boolean =
    (this?.get(key) as? JsonPrimitive)?.booleanOrNull ?: default
