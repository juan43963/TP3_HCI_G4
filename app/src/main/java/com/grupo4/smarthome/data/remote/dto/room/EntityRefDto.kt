package com.grupo4.smarthome.data.remote.dto.room

import kotlinx.serialization.Serializable

// Referencia liviana a otra entidad por id (home, room, etc.) tal como la espera/devuelve la API
@Serializable
data class EntityRefDto(
    val id: String? = null,
)
