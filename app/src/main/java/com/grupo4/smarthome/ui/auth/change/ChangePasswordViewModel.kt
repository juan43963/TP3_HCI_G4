package com.grupo4.smarthome.ui.auth.change

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

class ChangePasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        _uiState.update { it.copy(code = value.trim(), errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmChange(value: String) {
        _uiState.update { it.copy(confirm = value, errorMessage = null) }
    }

    fun submit() {
        val current = _uiState.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.resetPassword(current.code, current.password)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, passwordChanged = true) }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun consumePasswordChanged() {
        _uiState.update { it.copy(passwordChanged = false) }
    }

    @StringRes
    private fun messageFor(kind: ErrorKind): Int =
        when (kind) {
            ErrorKind.NotFound, ErrorKind.Validation -> R.string.error_invalid_or_expired_code
            else -> kind.toMessageRes()
        }
}

class ChangePasswordViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {
            return ChangePasswordViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
