package com.grupo4.smarthome.ui.screens.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.R
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        sessionManager.session.value?.user?.let { user ->
            _uiState.update { it.copy(name = user.name, email = user.email) }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.getProfile()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, name = result.data.name, email = result.data.email)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun saveName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank() || _uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = profileRepository.updateProfile(trimmed)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSaving = false, name = result.data.name, nameSaved = true)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = messageFor(result.error.kind))
                }
            }
        }
    }

    fun consumeNameSaved() {
        _uiState.update { it.copy(nameSaved = false) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    @StringRes
    private fun messageFor(kind: ErrorKind): Int =
        when (kind) {
            ErrorKind.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
}

class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
