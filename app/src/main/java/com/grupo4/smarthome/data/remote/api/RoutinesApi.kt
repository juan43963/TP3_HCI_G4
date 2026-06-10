package com.grupo4.smarthome.data.remote.api

import com.grupo4.smarthome.core.network.ApiRoutes
import com.grupo4.smarthome.data.remote.dto.routine.RoutineCreateDto
import com.grupo4.smarthome.data.remote.dto.routine.RoutineUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class RoutinesApi(
    private val client: HttpClient,
) {
    suspend fun getRoutines(): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.Routines))

    suspend fun getRoutine(routineId: String): HttpResponse =
        client.get(ApiRoutes.resolve(ApiRoutes.routineById(routineId)))

    suspend fun createRoutine(request: RoutineCreateDto): HttpResponse =
        client.post(ApiRoutes.resolve(ApiRoutes.Routines)) {
            setBody(request)
        }

    suspend fun updateRoutine(routineId: String, request: RoutineUpdateDto): HttpResponse =
        client.put(ApiRoutes.resolve(ApiRoutes.routineById(routineId))) {
            setBody(request)
        }

    suspend fun deleteRoutine(routineId: String): HttpResponse =
        client.delete(ApiRoutes.resolve(ApiRoutes.routineById(routineId)))

    // Ejecuta todas las acciones de la rutina en el back-end
    suspend fun executeRoutine(routineId: String): HttpResponse =
        client.patch(ApiRoutes.resolve(ApiRoutes.routineExecute(routineId)))
}
