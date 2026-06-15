package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.core.result.AppResult
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
import kotlinx.serialization.builtins.ListSerializer

class HomesRepositoryImpl(
    private val homesApi: HomesApi,
    private val sessionManager: SessionManager,
) : HomesRepository {
    private var cachedHomes: List<Home> = emptyList()

    override suspend fun getHomes(): AppResult<List<Home>> =
        apiCall(sessionManager, { homesApi.getHomes() }) { body ->
            ApiJson.Instance.decodeFromString(ListSerializer(HomeDto.serializer()), body)
                .map { it.toDomain() }
                .also { cachedHomes = it }
        }

    override suspend fun createHome(
        name: String,
        type: HomeType,
    ): AppResult<Home> =
        apiCall(
            sessionManager,
            { homesApi.createHome(HomeCreateDto(name = name.trim(), metadata = buildHomeMetadata(type))) },
            ::parseHome,
        )

    override suspend fun renameHome(homeId: String, name: String): AppResult<Home> {
        val current = cachedHomes.firstOrNull { it.id == homeId }
        return apiCall(
            sessionManager,
            {
                homesApi.updateHome(
                    homeId,
                    HomeUpdateDto(
                        name = name.trim(),
                        metadata = buildHomeMetadata(type = current?.type ?: HomeType.OTRO),
                    ),
                )
            },
            ::parseHome,
        )
    }

    override suspend fun deleteHome(homeId: String): AppResult<Unit> =
        apiCallUnit(sessionManager, { homesApi.deleteHome(homeId) })

    override suspend fun getSharedUsers(homeId: String): AppResult<List<SharedUser>> =
        apiCall(sessionManager, { homesApi.getSharedUsers(homeId) }, ::parseSharedUsers)

    override suspend fun shareHome(homeId: String, email: String): AppResult<List<SharedUser>> =
        apiCall(
            sessionManager,
            { homesApi.shareHome(homeId, ShareHomeRequestDto(listOf(email.trim()))) },
            ::parseSharedUsers,
        )

    private fun parseHome(body: String): Home =
        ApiJson.Instance.decodeFromString(HomeDto.serializer(), body).toDomain()

    private fun parseSharedUsers(body: String): List<SharedUser> =
        ApiJson.Instance
            .decodeFromString(ListSerializer(SharedUserDto.serializer()), body)
            .mapNotNull { it.toDomain() }
}
