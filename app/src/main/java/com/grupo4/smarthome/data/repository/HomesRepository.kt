package com.grupo4.smarthome.data.repository

import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.data.model.Home
import com.grupo4.smarthome.data.model.HomeType

interface HomesRepository {
    suspend fun getHomes(): AppResult<List<Home>>

    suspend fun createHome(
        name: String,
        type: HomeType,
        photoIndex: Int?,
    ): AppResult<Home>
}
