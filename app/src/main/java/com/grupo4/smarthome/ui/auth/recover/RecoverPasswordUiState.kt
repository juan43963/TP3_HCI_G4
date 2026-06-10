package com.grupo4.smarthome.ui.auth.recover

import androidx.annotation.StringRes

data class RecoverPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val codeSent: Boolean = false,
    @StringRes val errorMessage: Int? = null,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && email.contains("@") && !isLoading
}
