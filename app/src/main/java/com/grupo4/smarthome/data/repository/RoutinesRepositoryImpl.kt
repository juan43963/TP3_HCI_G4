package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.ApiError
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.model.NotificationKind
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory
import com.grupo4.smarthome.data.remote.api.RoutinesApi
import com.grupo4.smarthome.data.remote.dto.routine.RoutineCreateDto
import com.grupo4.smarthome.data.remote.dto.routine.RoutineDto
import com.grupo4.smarthome.data.remote.dto.routine.RoutineUpdateDto
import com.grupo4.smarthome.data.remote.mapper.buildRoutineMetadata
import com.grupo4.smarthome.data.remote.mapper.readRoutineHomeId
import com.grupo4.smarthome.data.remote.mapper.toActionDtos
import com.grupo4.smarthome.data.remote.mapper.toDomain
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.builtins.ListSerializer

class RoutinesRepositoryImpl(
    private val routinesApi: RoutinesApi,
    private val sessionManager: SessionManager,
    private val localActionTracker: LocalActionTracker = LocalActionTracker(),
) : RoutinesRepository {
    private var cachedRoutines: List<Routine> = emptyList()

    override suspend fun getRoutines(homeId: String): AppResult<List<Routine>> =
        apiCall(sessionManager, { routinesApi.getRoutines() }) { body ->
            parseRoutines(body, homeId).also { cachedRoutines = it }
        }

    override suspend fun getRoutine(routineId: String): AppResult<Routine> =
        apiCall(sessionManager, { routinesApi.getRoutine(routineId) }, ::parseRoutineAndCache)

    override suspend fun createRoutine(
        homeId: String,
        name: String,
        category: RoutineCategory,
        time: String,
        days: List<Boolean>,
        actions: List<RoutineAction>,
    ): AppResult<Routine> =
        runCatching {
            val draft = Routine(
                id = "",
                name = name.trim(),
                actions = actions,
                category = category,
                time = time,
                days = days,
                favorite = false,
                enabled = true,
            )
            val response = routinesApi.createRoutine(
                RoutineCreateDto(
                    name = draft.name,
                    actions = draft.toActionDtos(),
                    metadata = buildRoutineMetadata(category, time, days, false, true, homeId),
                )
            )
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                return@runCatching AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
            val created = ApiJson.Instance.decodeFromString(RoutineDto.serializer(), body).toDomain()
            fetchRoutinesOrNull(homeId)?.let { cachedRoutines = it }
            AppResult.Success(cachedRoutines.firstOrNull { it.id == created.id } ?: created)
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }.expireSessionOnUnauthorized(sessionManager)

    override suspend fun updateRoutine(routine: Routine): AppResult<Routine> =
        apiCall(
            sessionManager,
            {
                routinesApi.updateRoutine(
                    routine.id,
                    RoutineUpdateDto(
                        name = routine.name.trim(),
                        actions = routine.toActionDtos(),
                        metadata = buildRoutineMetadata(
                            routine.category,
                            routine.time,
                            routine.days,
                            routine.favorite,
                            routine.enabled,
                            routine.homeId,
                        ),
                    ),
                )
            },
            ::parseRoutineAndCache,
        )

    override suspend fun deleteRoutine(routineId: String): AppResult<Unit> =
        apiCallUnit(sessionManager, { routinesApi.deleteRoutine(routineId) }) {
            cachedRoutines = cachedRoutines.filterNot { it.id == routineId }
        }

    override suspend fun executeRoutine(routineId: String): AppResult<Unit> {
        val affectedDeviceIds = resolveRoutine(routineId)
            ?.actions
            ?.map { it.deviceId }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        if (affectedDeviceIds.isNotEmpty()) {
            localActionTracker.recordAll(
                affectedDeviceIds,
                NotificationKind.DEVICE_ACTION,
                RoutineSuppressionWindowMillis,
            )
        }
        return apiCallUnit(sessionManager, { routinesApi.executeRoutine(routineId) })
    }

    override suspend fun toggleFavorite(routineId: String): AppResult<Routine> {
        val current = resolveRoutine(routineId) ?: return AppResult.Error(ApiError(ErrorKind.NotFound))
        return updateRoutine(current.copy(favorite = !current.favorite))
    }

    override suspend fun toggleEnabled(routineId: String): AppResult<Routine> {
        val current = resolveRoutine(routineId) ?: return AppResult.Error(ApiError(ErrorKind.NotFound))
        return updateRoutine(current.copy(enabled = !current.enabled))
    }

    private fun parseRoutineAndCache(body: String): Routine {
        val routine = ApiJson.Instance.decodeFromString(RoutineDto.serializer(), body).toDomain()
        cachedRoutines = if (cachedRoutines.any { it.id == routine.id }) {
            cachedRoutines.map { if (it.id == routine.id) routine else it }
        } else {
            cachedRoutines + routine
        }
        return routine
    }

    private suspend fun resolveRoutine(routineId: String): Routine? {
        cachedRoutines.firstOrNull { it.id == routineId }?.let { return it }
        return apiFetchOrNull(sessionManager, { routinesApi.getRoutine(routineId) }) { body ->
            ApiJson.Instance.decodeFromString(RoutineDto.serializer(), body).toDomain()
        }
    }

    private suspend fun fetchRoutinesOrNull(homeId: String): List<Routine>? =
        apiFetchOrNull(sessionManager, { routinesApi.getRoutines() }) { body ->
            parseRoutines(body, homeId)
        }

    private fun parseRoutines(body: String, homeId: String): List<Routine> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(RoutineDto.serializer()), body)
            .filter { val owner = it.metadata.readRoutineHomeId(); owner.isEmpty() || owner == homeId }
            .map { it.toDomain() }

    private companion object {
        const val RoutineSuppressionWindowMillis = 15_000L
    }
}
