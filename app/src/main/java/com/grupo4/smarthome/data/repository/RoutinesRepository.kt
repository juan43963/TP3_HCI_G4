package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory

interface RoutinesRepository {
    // Lista las rutinas del hogar activo (la API no las asocia a hogares: filtramos por metadata.homeId)
    suspend fun getRoutines(homeId: String): AppResult<List<Routine>>

    suspend fun getRoutine(routineId: String): AppResult<Routine>

    suspend fun createRoutine(
        homeId: String,
        name: String,
        category: RoutineCategory,
        time: String,
        days: List<Boolean>,
        actions: List<RoutineAction>,
    ): AppResult<Routine>

    // PUT completo: la API exige name + actions
    suspend fun updateRoutine(routine: Routine): AppResult<Routine>

    suspend fun deleteRoutine(routineId: String): AppResult<Unit>

    // PATCH /routines/{id}/execute; éxito = 2xx (ignoramos resultados por acción)
    suspend fun executeRoutine(routineId: String): AppResult<Unit>

    suspend fun toggleFavorite(routineId: String): AppResult<Routine>

    suspend fun toggleEnabled(routineId: String): AppResult<Routine>
}
