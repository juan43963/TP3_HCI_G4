package com.grupo4.smarthome.ui.screens.profile

import androidx.annotation.StringRes

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val nameSaved: Boolean = false,
    @StringRes val errorMessage: Int? = null,
)
