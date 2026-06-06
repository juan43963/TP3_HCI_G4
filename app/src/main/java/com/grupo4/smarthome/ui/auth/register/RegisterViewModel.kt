package com.grupo4.smarthome.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        updateForm { it.copy(fullName = value) }
    }

    fun onEmailChange(value: String) {
        updateForm { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        updateForm { it.copy(password = value) }
    }

    fun onConfirmPasswordChange(value: String) {
        updateForm { it.copy(confirmPassword = value) }
    }

    fun onNextStep() {
        val error = validateStepOne(_uiState.value)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        _uiState.update { it.copy(step = 2, errorMessage = null) }
    }

    fun onPreviousStep() {
        _uiState.update { it.copy(step = 1, errorMessage = null) }
    }

    fun register() {
        val current = _uiState.value
        if (current.isLoading) return

        val error = validateForm(current)
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    registrationCompleted = false,
                    registeredEmail = null,
                )
            }

            when (val result = authRepository.register(current.fullName, current.email, current.password)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registrationCompleted = true,
                            registeredEmail = result.data.user.email,
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = messageFor(result.error.kind, result.error.description),
                        )
                    }
                }
            }
        }
    }

    fun consumeRegistrationCompleted() {
        _uiState.update { it.copy(registrationCompleted = false) }
    }

    private fun updateForm(block: (RegisterUiState) -> RegisterUiState) {
        _uiState.update {
            block(it).copy(
                errorMessage = null,
                registrationCompleted = false,
                registeredEmail = null,
            )
        }
    }

    private fun validateStepOne(state: RegisterUiState): String? =
        when {
            state.fullName.isBlank() -> "Ingresá tu nombre."
            state.email.isBlank() -> "Ingresá tu email."
            !state.email.trim().matches(EMAIL_REGEX) -> "Ingresá un email válido."
            else -> null
        }

    private fun validateForm(state: RegisterUiState): String? =
        validateStepOne(state) ?: when {
            state.password.length < MIN_PASSWORD_LENGTH -> "La contraseña debe tener al menos 6 caracteres."
            state.password != state.confirmPassword -> "Las contraseñas no coinciden."
            else -> null
        }

    private fun messageFor(kind: ErrorKind, fallback: String): String =
        when (kind) {
            ErrorKind.Conflict -> "Ese email ya está registrado."
            ErrorKind.Network -> "No se pudo conectar con el servidor."
            ErrorKind.Server -> "El servidor no pudo crear la cuenta."
            ErrorKind.Validation -> "Revisá los datos ingresados."
            else -> fallback
        }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}

class RegisterViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
