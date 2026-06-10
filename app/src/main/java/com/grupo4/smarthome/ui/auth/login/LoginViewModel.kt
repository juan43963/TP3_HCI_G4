package com.grupo4.smarthome.ui.auth.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.R
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.toMessageRes
import com.grupo4.smarthome.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val current = _uiState.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.login(current.email, current.password)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, loggedIn = true) }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun consumeLoggedIn() {
        _uiState.update { it.copy(loggedIn = false) }
    }

    @StringRes
    private fun messageFor(kind: ErrorKind): Int =
        when (kind) {
            ErrorKind.Unauthorized, ErrorKind.Validation -> R.string.error_login_invalid_credentials
            ErrorKind.Server -> R.string.error_login_server
            else -> kind.toMessageRes()
        }
}

class LoginViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
