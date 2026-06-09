package com.grupo4.smarthome.data.model

data class User(
    val id: String? = null,
    val email: String,
    val name: String,
    val isVerified: Boolean = false,
    val createdAt: String? = null,
)
