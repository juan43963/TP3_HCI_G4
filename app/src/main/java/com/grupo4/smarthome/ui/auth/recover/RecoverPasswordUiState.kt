package com.grupo4.smarthome.ui.auth.recover

data class RecoverPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val codeSent: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && email.contains("@") && !isLoading
}
