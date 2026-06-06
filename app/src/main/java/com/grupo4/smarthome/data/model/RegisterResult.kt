package com.grupo4.smarthome.data.model

data class RegisterResult(
    val user: User,
    val token: String? = null,
)
