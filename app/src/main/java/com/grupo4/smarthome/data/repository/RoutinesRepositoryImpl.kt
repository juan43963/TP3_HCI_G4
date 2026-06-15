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
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.builtins.ListSerializer

class RoutinesRepositoryImpl(
    private val routinesApi: RoutinesApi,
    private val sessionManager: SessionManager,
    private val localActionTracker: LocalActionTracker = LocalActionTracker(),
) : RoutinesRepository {
    // Caché en memoria (ADR-0005): preserva category/time/days/favorite para reconstruir el PUT completo
    private var cachedRoutines: List<Routine> = emptyList()

    override suspend fun getRoutines(homeId: String): AppResult<List<Routine>> =
        runCatching {
            val response = routinesApi.getRoutines()
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val routines = parseRoutines(body, homeId)
                cachedRoutines = routines
                AppResult.Success(routines)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun getRoutine(routineId: String): AppResult<Routine> =
        runCatching {
            val response = routinesApi.getRoutine(routineId)
            parseRoutineResponse(response)
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

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
            // Refetch para reflejar el estado real del back-end (ADR-0008)
            fetchRoutinesOrNull(homeId)?.let { cachedRoutines = it }
            AppResult.Success(cachedRoutines.firstOrNull { it.id == created.id } ?: created)
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun updateRoutine(routine: Routine): AppResult<Routine> =
        runCatching {
            val response = routinesApi.updateRoutine(
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
                )
            )
            parseRoutineResponse(response)
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun deleteRoutine(routineId: String): AppResult<Unit> =
        runCatching {
            val response = routinesApi.deleteRoutine(routineId)
            if (response.status.isSuccess()) {
                cachedRoutines = cachedRoutines.filterNot { it.id == routineId }
                AppResult.Success(Unit)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun executeRoutine(routineId: String): AppResult<Unit> =
        runCatching {
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
            val response = routinesApi.executeRoutine(routineId)
            if (response.status.isSuccess()) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun toggleFavorite(routineId: String): AppResult<Routine> =
        runCatching {
            val current = resolveRoutine(routineId)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.NotFound)
                )
            updateRoutine(current.copy(favorite = !current.favorite))
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    override suspend fun toggleEnabled(routineId: String): AppResult<Routine> =
        runCatching {
            val current = resolveRoutine(routineId)
                ?: return@runCatching AppResult.Error(
                    ApiError(ErrorKind.NotFound)
                )
            updateRoutine(current.copy(enabled = !current.enabled))
        }.getOrElse { AppResult.Error(ErrorMapper.fromThrowable(it)) }
            .expireSessionOnUnauthorized(sessionManager)

    // --- Helpers ---

    // Parsea la respuesta de una única rutina y actualiza la caché; mapea el error si no fue 2xx
    private suspend fun parseRoutineResponse(response: HttpResponse): AppResult<Routine> {
        val body = response.bodyAsText()
        return if (response.status.isSuccess()) {
            val routine = ApiJson.Instance.decodeFromString(RoutineDto.serializer(), body).toDomain()
            cachedRoutines = if (cachedRoutines.any { it.id == routine.id }) {
                cachedRoutines.map { if (it.id == routine.id) routine else it }
            } else {
                cachedRoutines + routine
            }
            AppResult.Success(routine)
        } else {
            AppResult.Error(ErrorMapper.fromHttp(response.status, body))
        }
    }

    // Devuelve la rutina cacheada o, si falta, la trae con GET /routines/{id}
    private suspend fun resolveRoutine(routineId: String): Routine? {
        cachedRoutines.firstOrNull { it.id == routineId }?.let { return it }
        return runCatching {
            val response = routinesApi.getRoutine(routineId)
            if (response.status.isSuccess()) {
                ApiJson.Instance.decodeFromString(RoutineDto.serializer(), response.bodyAsText()).toDomain()
            } else {
                sessionManager.expireSessionIfUnauthorized(response.status)
                null
            }
        }.getOrNull()
    }

    // Refetch silencioso tras crear; devuelve null ante cualquier fallo
    private suspend fun fetchRoutinesOrNull(homeId: String): List<Routine>? =
        runCatching {
            val response = routinesApi.getRoutines()
            if (response.status.isSuccess()) {
                parseRoutines(response.bodyAsText(), homeId)
            } else {
                sessionManager.expireSessionIfUnauthorized(response.status)
                null
            }
        }.getOrNull()

    // Filtra por hogar: las que no guardan homeId se muestran en cualquier hogar (tolerante)
    private fun parseRoutines(body: String, homeId: String): List<Routine> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(RoutineDto.serializer()), body)
            .filter { val owner = it.metadata.readRoutineHomeId(); owner.isEmpty() || owner == homeId }
            .map { it.toDomain() }

    private companion object {
        const val RoutineSuppressionWindowMillis = 15_000L
    }
}
