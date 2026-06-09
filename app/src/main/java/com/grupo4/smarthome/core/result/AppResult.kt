package com.grupo4.smarthome.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: ApiError) : AppResult<Nothing>
}
