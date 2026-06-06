package com.grupo4.smarthome.ui.auth.register

data class RegisterUiState(
    val step: Int = 1,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationCompleted: Boolean = false,
    val registeredEmail: String? = null,
) {
    val step1Valid: Boolean
        get() = fullName.isNotBlank() && email.isNotBlank() && !isLoading

    val step2Valid: Boolean
        get() = password.length >= MIN_PASSWORD_LENGTH &&
            password == confirmPassword &&
            !isLoading
}

private const val MIN_PASSWORD_LENGTH = 6
