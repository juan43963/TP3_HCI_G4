package com.grupo4.smarthome.data.remote.mapper

import com.grupo4.smarthome.data.model.User
import com.grupo4.smarthome.data.remote.dto.auth.UserDto

fun UserDto.toDomain(): User =
    User(
        id = id,
        email = email,
        name = name,
        isVerified = isVerified,
        createdAt = createdAt,
    )
