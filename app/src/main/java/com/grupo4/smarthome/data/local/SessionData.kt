package com.grupo4.smarthome.data.local

import com.grupo4.smarthome.data.model.User

data class SessionData(
    val token: String,
    val user: User,
)
