package com.grupo4.smarthome.core.result

data class ApiError(
    val kind: ErrorKind,
    val description: String,
    val httpStatus: Int? = null,
    val apiCode: Int? = null,
)

enum class ErrorKind {
    Validation,
    Conflict,
    Unauthorized,
    Forbidden,
    NotFound,
    Network,
    Server,
    Unknown,
}
