package com.grupo4.smarthome.ui.auth.change

data class ChangePasswordUiState(
    val code: String = "",
    val password: String = "",
    val confirm: String = "",
    val isLoading: Boolean = false,
    val passwordChanged: Boolean = false,
    val errorMessage: String? = null,
) {
    val showMismatch: Boolean
        get() = confirm.isNotEmpty() && confirm != password

    val canSubmit: Boolean
        get() = code.isNotBlank() &&
            password.length >= MIN_PASSWORD_LENGTH &&
            password == confirm &&
            !isLoading
}

private const val MIN_PASSWORD_LENGTH = 8
