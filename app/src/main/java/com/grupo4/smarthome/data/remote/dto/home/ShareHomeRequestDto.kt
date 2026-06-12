package com.grupo4.smarthome.data.remote.dto.home

import kotlinx.serialization.Serializable

@Serializable
data class ShareHomeRequestDto(
    val emails: List<String>,
)
