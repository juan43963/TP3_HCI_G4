package com.grupo4.smarthome.data.remote.api

import com.grupo4.smarthome.core.network.ApiRoutes
import com.grupo4.smarthome.data.remote.dto.home.HomeCreateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class HomesApi(
    private val client: HttpClient,
) {
    suspend fun getHomes(): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.Homes))

    suspend fun createHome(request: HomeCreateDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.Homes)) {
            setBody(request)
        }
}
