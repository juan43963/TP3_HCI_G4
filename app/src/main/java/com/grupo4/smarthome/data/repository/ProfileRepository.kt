package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.User

interface ProfileRepository {
    suspend fun getProfile(): AppResult<User>

    suspend fun updateProfile(name: String): AppResult<User>
}
