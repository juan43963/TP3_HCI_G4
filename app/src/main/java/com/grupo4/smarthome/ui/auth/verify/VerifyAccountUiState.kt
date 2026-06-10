package com.grupo4.smarthome.ui.auth.verify

import androidx.annotation.StringRes

data class VerifyAccountUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val verified: Boolean = false,
    @StringRes val errorMessage: Int? = null,
) {
    val canSubmit: Boolean
        get() = code.length == VERIFY_CODE_LENGTH && !isLoading
}

const val VERIFY_CODE_LENGTH = 16
