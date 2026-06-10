package com.grupo4.smarthome.ui.auth.change

import androidx.annotation.StringRes

data class AuthChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirm: String = "",
    val isLoading: Boolean = false,
    val passwordChanged: Boolean = false,
    @StringRes val errorMessage: Int? = null,
) {
    val showMismatch: Boolean
        get() = confirm.isNotEmpty() && confirm != newPassword

    val canSubmit: Boolean
        get() = currentPassword.isNotBlank() &&
            newPassword.length >= MIN_PASSWORD_LENGTH &&
            newPassword == confirm &&
            !isLoading
}

private const val MIN_PASSWORD_LENGTH = 8
