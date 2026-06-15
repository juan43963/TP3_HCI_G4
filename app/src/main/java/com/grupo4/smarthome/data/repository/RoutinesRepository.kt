package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.data.model.RoutineAction
import com.grupo4.smarthome.data.model.RoutineCategory

interface RoutinesRepository {
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

    suspend fun updateRoutine(routine: Routine): AppResult<Routine>

    suspend fun deleteRoutine(routineId: String): AppResult<Unit>

    suspend fun executeRoutine(routineId: String): AppResult<Unit>

    suspend fun toggleFavorite(routineId: String): AppResult<Routine>

    suspend fun toggleEnabled(routineId: String): AppResult<Routine>
}
