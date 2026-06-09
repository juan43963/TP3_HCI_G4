package com.grupo4.smarthome.data.remote.api

import com.grupo4.smarthome.core.network.ApiRoutes
import com.grupo4.smarthome.data.remote.dto.home.HomeCreateDto
import com.grupo4.smarthome.data.remote.dto.home.HomeUpdateDto
import com.grupo4.smarthome.data.remote.dto.home.ShareHomeRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
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

    suspend fun updateHome(homeId: String, request: HomeUpdateDto): HttpResponse =
        client.put(ApiRoutes.resolve(ApiRoutes.homeById(homeId))) {
            setBody(request)
        }

    suspend fun deleteHome(homeId: String): HttpResponse =
        client.delete(ApiRoutes.resolve(ApiRoutes.homeById(homeId)))

    suspend fun getSharedUsers(homeId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.homeShare(homeId)))

    suspend fun shareHome(homeId: String, request: ShareHomeRequestDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.homeShare(homeId))) {
            setBody(request)
        }
}
