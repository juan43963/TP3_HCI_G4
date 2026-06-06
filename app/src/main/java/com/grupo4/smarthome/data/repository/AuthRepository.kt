package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.RegisterResult
import com.grupo4.smarthome.data.model.User

interface AuthRepository {
    suspend fun register(
        name: String,
        email: String,
        password: String,
    ): AppResult<RegisterResult>

    suspend fun login(email: String, password: String): AppResult<User>

    suspend fun forgotPassword(email: String): AppResult<Unit>

    suspend fun resetPassword(code: String, password: String): AppResult<Unit>

    suspend fun verifyAccount(code: String): AppResult<Unit>

    suspend fun sendVerification(email: String): AppResult<Unit>
}
