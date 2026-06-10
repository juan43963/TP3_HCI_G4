package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.model.User
import com.grupo4.smarthome.data.remote.api.ProfileApi
import com.grupo4.smarthome.data.remote.dto.auth.UserDto
import com.grupo4.smarthome.data.remote.dto.auth.UserUpdateDto
import com.grupo4.smarthome.data.remote.mapper.toDomain
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class ProfileRepositoryImpl(
    private val profileApi: ProfileApi,
    private val sessionManager: SessionManager,
) : ProfileRepository {
    override suspend fun getProfile(): AppResult<User> =
        request { profileApi.getProfile() }

    override suspend fun updateProfile(name: String): AppResult<User> =
        request { profileApi.updateProfile(UserUpdateDto(name = name.trim())) }

    private suspend fun request(call: suspend () -> HttpResponse): AppResult<User> =
        runCatching {
            val response = call()
            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                val user = ApiJson.Instance.decodeFromString(UserDto.serializer(), body).toDomain()
                sessionManager.updateUser(user)
                AppResult.Success(user)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }
}
