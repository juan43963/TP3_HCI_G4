package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorMapper
import com.grupo4.smarthome.core.network.ApiJson
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.model.RegisterResult
import com.grupo4.smarthome.data.model.User
import com.grupo4.smarthome.data.remote.api.AuthApi
import com.grupo4.smarthome.data.remote.dto.auth.ChangePasswordRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.ForgotPasswordRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.LoginRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.LoginResponseDto
import com.grupo4.smarthome.data.remote.dto.auth.RegisterRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.RegisterResponseDto
import com.grupo4.smarthome.data.remote.dto.auth.ResetPasswordRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.SendVerificationRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.UserDto
import com.grupo4.smarthome.data.remote.dto.auth.VerifyAccountRequestDto
import com.grupo4.smarthome.data.remote.mapper.toDomain
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
) : AuthRepository {
    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): AppResult<RegisterResult> =
        runCatching {
            val response = authApi.register(
                RegisterRequestDto(
                    email = email.trim(),
                    password = password,
                    name = name.trim(),
                )
            )

            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                // register no devuelve token: la sesión se crea recién en login
                AppResult.Success(parseRegisterSuccess(body))
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun login(email: String, password: String): AppResult<User> =
        runCatching {
            val response = authApi.login(LoginRequestDto(email.trim(), password))
            val body = response.bodyAsText()

            if (response.status.isSuccess()) {
                val result = ApiJson.Instance.decodeFromString(LoginResponseDto.serializer(), body)
                val user = result.user.toDomain()
                sessionManager.saveSession(result.token, user)
                AppResult.Success(user)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, body))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    override suspend fun forgotPassword(email: String): AppResult<Unit> =
        requestUnit { authApi.forgotPassword(ForgotPasswordRequestDto(email.trim())) }

    override suspend fun resetPassword(code: String, password: String): AppResult<Unit> =
        requestUnit { authApi.resetPassword(ResetPasswordRequestDto(code.trim(), password)) }

    override suspend fun verifyAccount(code: String): AppResult<Unit> =
        requestUnit { authApi.verifyAccount(VerifyAccountRequestDto(code.trim())) }

    override suspend fun sendVerification(email: String): AppResult<Unit> =
        requestUnit { authApi.sendVerification(SendVerificationRequestDto(email.trim())) }

    override suspend fun changePassword(oldPassword: String, newPassword: String): AppResult<Unit> =
        requestUnit { authApi.changePassword(ChangePasswordRequestDto(oldPassword, newPassword)) }

    // Best-effort: avisamos al backend pero siempre limpiamos la sesión local
    override suspend fun logout() {
        runCatching { authApi.logout() }
        sessionManager.clearSession()
    }

    // Llamadas que no necesitan parsear cuerpo: solo importa éxito o el error mapeado
    private suspend fun requestUnit(call: suspend () -> HttpResponse): AppResult<Unit> =
        runCatching {
            val response = call()
            if (response.status.isSuccess()) {
                AppResult.Success(Unit)
            } else {
                AppResult.Error(ErrorMapper.fromHttp(response.status, response.bodyAsText()))
            }
        }.getOrElse { throwable ->
            AppResult.Error(ErrorMapper.fromThrowable(throwable))
        }

    private fun parseRegisterSuccess(body: String): RegisterResult {
        val response = ApiJson.Instance.decodeFromString(RegisterResponseDto.serializer(), body)
        val wrappedUser = response.user
        if (wrappedUser != null) {
            return RegisterResult(
                user = wrappedUser.toDomain(),
                token = response.token,
            )
        }

        return RegisterResult(
            user = ApiJson.Instance.decodeFromString(UserDto.serializer(), body).toDomain(),
        )
    }
}
