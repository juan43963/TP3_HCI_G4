package com.grupo4.smarthome.ui.auth.recover

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

class RecoverPasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecoverPasswordUiState())
    val uiState: StateFlow<RecoverPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun sendCode() {
        val current = _uiState.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.forgotPassword(current.email)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, codeSent = true) }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(result.error.kind, result.error.description))
                }
            }
        }
    }

    fun onResend() {
        _uiState.update { it.copy(codeSent = false, errorMessage = null) }
    }

    private fun messageFor(kind: ErrorKind, fallback: String): String =
        when (kind) {
            ErrorKind.NotFound -> "No encontramos una cuenta con ese email."
            ErrorKind.Network -> "No se pudo conectar con el servidor."
            ErrorKind.Validation -> "Ingresá un email válido."
            else -> fallback
        }
}

class RecoverPasswordViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecoverPasswordViewModel::class.java)) {
            return RecoverPasswordViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
