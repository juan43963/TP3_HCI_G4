package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.Home
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.data.model.SharedUser

interface HomesRepository {
    suspend fun getHomes(): AppResult<List<Home>>

    suspend fun createHome(
        name: String,
        type: HomeType,
    ): AppResult<Home>

    suspend fun renameHome(homeId: String, name: String): AppResult<Home>

    suspend fun deleteHome(homeId: String): AppResult<Unit>

    suspend fun getSharedUsers(homeId: String): AppResult<List<SharedUser>>

    suspend fun shareHome(homeId: String, email: String): AppResult<List<SharedUser>>
}
