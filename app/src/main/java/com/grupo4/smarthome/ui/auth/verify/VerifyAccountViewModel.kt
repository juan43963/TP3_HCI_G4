package com.grupo4.smarthome.ui.auth.verify

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.R
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.toMessageRes
import com.grupo4.smarthome.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerifyAccountViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VerifyAccountUiState())
    val uiState: StateFlow<VerifyAccountUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        val trimmed = value.trim()
        if (trimmed.length > VERIFY_CODE_LENGTH) return
        _uiState.update { it.copy(code = trimmed, errorMessage = null) }
    }

    fun verify(email: String, password: String) {
        val current = _uiState.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.verifyAccount(current.code)) {
                is AppResult.Success -> {
                    when (val login = authRepository.login(email, password)) {
                        is AppResult.Success -> _uiState.update { it.copy(isLoading = false, verified = true) }
                        is AppResult.Error -> _uiState.update {
                            it.copy(isLoading = false, errorMessage = login.error.kind.toMessageRes())
                        }
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun resend(email: String) {
        if (email.isBlank()) return
        if (_uiState.value.resendCooldownSeconds > 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(code = "", errorMessage = null, hasSent = true) }
            authRepository.sendVerification(email)
        }
        startResendCooldown()
    }

    private fun startResendCooldown() {
        viewModelScope.launch {
            var remaining = RESEND_COOLDOWN_SECONDS
            while (remaining > 0) {
                _uiState.update { it.copy(resendCooldownSeconds = remaining) }
                delay(1000)
                remaining--
            }
            _uiState.update { it.copy(resendCooldownSeconds = 0) }
        }
    }

    fun consumeVerified() {
        _uiState.update { it.copy(verified = false) }
    }

    @StringRes
    private fun messageFor(kind: ErrorKind): Int =
        when (kind) {
            ErrorKind.Conflict -> R.string.error_account_already_verified
            ErrorKind.Validation, ErrorKind.NotFound -> R.string.error_invalid_or_expired_code
            else -> kind.toMessageRes()
        }
}

class VerifyAccountViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VerifyAccountViewModel::class.java)) {
            return VerifyAccountViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
