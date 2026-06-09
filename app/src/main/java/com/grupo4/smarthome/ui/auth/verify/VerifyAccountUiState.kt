package com.grupo4.smarthome.ui.auth.verify

data class VerifyAccountUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val verified: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = code.length == VERIFY_CODE_LENGTH && !isLoading
}

const val VERIFY_CODE_LENGTH = 16
