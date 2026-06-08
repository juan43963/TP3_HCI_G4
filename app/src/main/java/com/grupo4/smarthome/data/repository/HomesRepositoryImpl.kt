package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.model.Home
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.data.model.SharedUser
import com.grupo4.smarthome.data.remote.api.HomesApi
import com.grupo4.smarthome.data.remote.dto.home.HomeCreateDto
import com.grupo4.smarthome.data.remote.dto.home.HomeDto
import com.grupo4.smarthome.data.remote.dto.home.HomeUpdateDto
import com.grupo4.smarthome.data.remote.dto.home.ShareHomeRequestDto
import com.grupo4.smarthome.data.remote.dto.home.SharedUserDto
import com.grupo4.smarthome.data.remote.mapper.buildHomeMetadata
import com.grupo4.smarthome.data.remote.mapper.toDomain
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.builtins.ListSerializer

class HomesRepositoryImpl(
    private val homesApi: HomesApi,
) : HomesRepository {
    // Caché en memoria (ADR-0005): vive con el proceso, no se persiste en DataStore
    private var cachedHomes: List<Home> = emptyList()

    override suspend fun getHomes(): AppResult<List<Home>> =
        runCatching {
            val response = homesApi.getHomes()
            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                val dtos = ApiJson.Instance.decodeFromString(ListSerializer(HomeDto.serializer()), body)
                val homes = dtos.map { it.toDomain() }
                cachedHomes = homes
                AppResult.Success(homes)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun createHome(
        name: String,
        type: HomeType,
        photoIndex: Int?,
    ): AppResult<Home> =
        runCatching {
            val response = homesApi.createHome(
                HomeCreateDto(
                    name = name.trim(),
                    metadata = buildHomeMetadata(type, photoIndex),
                )
            )
            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                AppResult.Success(
                    ApiJson.Instance.decodeFromString(HomeDto.serializer(), body).toDomain()
                )
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun renameHome(homeId: String, name: String): AppResult<Home> =
        runCatching {
            // Preservamos el tipo y la foto guardados en metadata; solo cambia el nombre
            val current = cachedHomes.firstOrNull { it.id == homeId }
            val response = homesApi.updateHome(
                homeId,
                HomeUpdateDto(
                    name = name.trim(),
                    metadata = buildHomeMetadata(
                        type = current?.type ?: HomeType.OTRO,
                        photoIndex = current?.photoIndex,
                    ),
                )
            )
            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                AppResult.Success(
                    ApiJson.Instance.decodeFromString(HomeDto.serializer(), body).toDomain()
                )
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun deleteHome(homeId: String): AppResult<Unit> =
        runCatching {
            val response = homesApi.deleteHome(homeId)
            if (response.status.isSuccess()) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun getSharedUsers(homeId: String): AppResult<List<SharedUser>> =
        runCatching {
            val response = homesApi.getSharedUsers(homeId)
            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                AppResult.Success(parseSharedUsers(body))
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun shareHome(homeId: String, email: String): AppResult<List<SharedUser>> =
        runCatching {
            val response = homesApi.shareHome(homeId, ShareHomeRequestDto(email.trim()))
            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                // El POST devuelve la lista actualizada de usuarios con acceso
                AppResult.Success(parseSharedUsers(body))
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    private fun parseSharedUsers(body: String): List<SharedUser> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(SharedUserDto.serializer()), body)
            .mapNotNull { it.toDomain() }
}
