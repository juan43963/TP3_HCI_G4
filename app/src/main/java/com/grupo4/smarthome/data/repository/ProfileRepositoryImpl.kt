package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.model.User
import com.grupo4.smarthome.data.remote.api.ProfileApi
import com.grupo4.smarthome.data.remote.dto.auth.UserDto
import com.grupo4.smarthome.data.remote.dto.auth.UserUpdateDto
import com.grupo4.smarthome.data.remote.mapper.toDomain

class ProfileRepositoryImpl(
    private val profileApi: ProfileApi,
    private val sessionManager: SessionManager,
) : ProfileRepository {
    override suspend fun getProfile(): AppResult<User> =
        apiCall(sessionManager, { profileApi.getProfile() }, ::parseUser)

    override suspend fun updateProfile(name: String): AppResult<User> =
        apiCall(sessionManager, { profileApi.updateProfile(UserUpdateDto(name = name.trim())) }, ::parseUser)

    private suspend fun parseUser(body: String): User {
        val user = ApiJson.Instance.decodeFromString(UserDto.serializer(), body).toDomain()
        sessionManager.updateUser(user)
        return user
    }
}
