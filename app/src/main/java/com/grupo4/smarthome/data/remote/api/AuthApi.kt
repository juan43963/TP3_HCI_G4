package com.grupo4.smarthome.data.remote.api

import com.grupo4.smarthome.core.network.ApiRoutes
import com.grupo4.smarthome.data.remote.dto.auth.ChangePasswordRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.ForgotPasswordRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.LoginRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.RegisterRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.ResetPasswordRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.SendVerificationRequestDto
import com.grupo4.smarthome.data.remote.dto.auth.VerifyAccountRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class AuthApi(
    private val client: HttpClient,
) {
    suspend fun register(request: RegisterRequestDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.UsersRegister)) {
            setBody(request)
        }

    suspend fun login(request: LoginRequestDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.UsersLogin)) {
            setBody(request)
        }

    suspend fun forgotPassword(request: ForgotPasswordRequestDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.UsersForgotPassword)) {
            setBody(request)
        }

    suspend fun resetPassword(request: ResetPasswordRequestDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.UsersResetPassword)) {
            setBody(request)
        }

    suspend fun verifyAccount(request: VerifyAccountRequestDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.UsersVerifyAccount)) {
            setBody(request)
        }

    suspend fun sendVerification(request: SendVerificationRequestDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.UsersSendVerification)) {
            setBody(request)
        }

    suspend fun changePassword(request: ChangePasswordRequestDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.UsersChangePassword)) {
            setBody(request)
        }

    suspend fun logout(): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.UsersLogout))
}
