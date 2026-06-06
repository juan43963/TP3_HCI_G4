package com.grupo4.smarthome.core.network

import com.grupo4.smarthome.BuildConfig

object ApiConstants {
    const val ApiKeyHeader = "X-API-Key"
    const val AuthorizationHeader = "Authorization"
    val BaseUrl: String = BuildConfig.API_BASE_URL
    val ApiKey: String = BuildConfig.API_KEY
}
