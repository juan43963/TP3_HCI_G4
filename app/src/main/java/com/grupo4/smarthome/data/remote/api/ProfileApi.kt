package com.grupo4.smarthome.data.remote.api

import com.grupo4.smarthome.core.network.ApiRoutes
import com.grupo4.smarthome.data.remote.dto.auth.UserUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class ProfileApi(
    private val client: HttpClient,
) {
    suspend fun getProfile(): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.UsersProfile))

    suspend fun updateProfile(request: UserUpdateDto): HttpResponse =
        client.put(ApiRoutes.resolve(ApiRoutes.UsersProfile)) {
            setBody(request)
        }
}
