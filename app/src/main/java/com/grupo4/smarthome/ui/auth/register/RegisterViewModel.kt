package com.grupo4.smarthome.ui.auth.register

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.R
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
                            errorMessage = messageFor(result.error.kind),
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

    @StringRes
    private fun validateStepOne(state: RegisterUiState): Int? =
        when {
            state.fullName.isBlank() -> R.string.error_name_required
            state.email.isBlank() -> R.string.error_email_required
            !state.email.trim().matches(EMAIL_REGEX) -> R.string.error_email_invalid
            else -> null
        }

    @StringRes
    private fun validateForm(state: RegisterUiState): Int? =
        validateStepOne(state) ?: when {
            state.password.length < MIN_PASSWORD_LENGTH -> R.string.error_password_too_short_6
            state.password != state.confirmPassword -> R.string.error_passwords_mismatch
            else -> null
        }

    @StringRes
    private fun messageFor(kind: ErrorKind): Int =
        when (kind) {
            ErrorKind.Conflict -> R.string.error_register_email_taken
            ErrorKind.Network -> R.string.error_network
            ErrorKind.Server -> R.string.error_register_server
            ErrorKind.Validation -> R.string.error_validation
            else -> R.string.error_unknown
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
