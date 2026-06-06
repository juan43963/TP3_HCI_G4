package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.model.Home
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.data.remote.api.HomesApi
import com.grupo4.smarthome.data.remote.dto.home.HomeCreateDto
import com.grupo4.smarthome.data.remote.dto.home.HomeDto
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
}
