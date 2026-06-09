package com.grupo4.smarthome.core.network

import kotlinx.serialization.json.Json

object ApiJson {
    val Instance = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }
}
