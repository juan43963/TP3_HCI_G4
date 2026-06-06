package com.grupo4.smarthome.ui.screens.selecthome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.model.Home
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.data.repository.HomesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SelectHomeViewModel(
    private val homesRepository: HomesRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SelectHomeUiState(userName = sessionManager.session.value?.user?.name.orEmpty())
    )
    val uiState: StateFlow<SelectHomeUiState> = _uiState.asStateFlow()

    init {
        loadHomes()
    }

    fun loadHomes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = homesRepository.getHomes()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, homes = result.data.map(Home::toSelectable))
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = messageFor(result.error.kind, result.error.description))
                }
            }
        }
    }

    fun createHome(name: String, typeIndex: Int, photoIndex: Int?) {
        if (_uiState.value.isCreating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }
            val type = HomeType.entries.getOrElse(typeIndex) { HomeType.OTRO }
            when (val result = homesRepository.createHome(name, type, photoIndex)) {
                is AppResult.Success -> {
                    // Refetch para reflejar el estado real del back-end (ADR-0008)
                    val refreshed = (homesRepository.getHomes() as? AppResult.Success)?.data
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            homeJustCreated = true,
                            homes = refreshed?.map(Home::toSelectable) ?: it.homes,
                        )
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isCreating = false, createError = messageFor(result.error.kind, result.error.description))
                }
            }
        }
    }

    fun consumeHomeCreated() {
        _uiState.update { it.copy(homeJustCreated = false) }
    }

    fun dismissCreateError() {
        _uiState.update { it.copy(createError = null) }
    }

    private fun messageFor(kind: ErrorKind, fallback: String): String =
        when (kind) {
            ErrorKind.Unauthorized -> "Tu sesión expiró. Volvé a iniciar sesión."
            ErrorKind.Network -> "No se pudo conectar con el servidor."
            ErrorKind.Validation -> "Revisá los datos ingresados."
            ErrorKind.Server -> "El servidor no pudo completar la operación."
            else -> fallback
        }
}

private fun Home.toSelectable(): SelectableHome =
    SelectableHome(
        id = id,
        name = name,
        roomsCount = 0,
        devicesCount = 0,
        photoIndex = photoIndex,
    )

class SelectHomeViewModelFactory(
    private val homesRepository: HomesRepository,
    private val sessionManager: SessionManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectHomeViewModel::class.java)) {
            return SelectHomeViewModel(homesRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
