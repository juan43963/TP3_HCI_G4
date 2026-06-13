package com.grupo4.smarthome.ui.screens.selecthome

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.R
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.ErrorKind
import com.grupo4.smarthome.core.result.toMessageRes
import com.grupo4.smarthome.data.local.PreferencesDataStore
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.model.Home
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.data.repository.HomesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SelectHomeViewModel(
    private val homesRepository: HomesRepository,
    private val sessionManager: SessionManager,
    private val preferencesDataStore: PreferencesDataStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelectHomeUiState())
    val uiState: StateFlow<SelectHomeUiState> = _uiState.asStateFlow()

    // Casa activa persistida; al cambiar reconciliamos contra la lista cargada
    private val selectedHomeId: StateFlow<String?> =
        preferencesDataStore.selectedHomeId.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // Reaccionamos a la sesión: al loguearse (aparece el token) recargamos las casas solas
        viewModelScope.launch {
            sessionManager.session
                .distinctUntilChanged { old, new -> old?.token == new?.token }
                .collect { session ->
                    if (session != null) {
                        _uiState.update { it.copy(userName = session.user.name) }
                        loadHomes()
                    }
                }
        }
        viewModelScope.launch {
            selectedHomeId.collect { reconcileActiveHome() }
        }
    }

    fun loadHomes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = homesRepository.getHomes()) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            homesLoaded = true,
                            homes = result.data.map(Home::toSelectable),
                        )
                    }
                    reconcileActiveHome()
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, homesLoaded = true, errorMessage = result.error.kind.toMessageRes())
                }
            }
        }
    }

    fun selectHome(homeId: String) {
        _uiState.update {
            it.copy(activeHomeId = homeId, recentHomeIds = it.recentHomeIds.moveToFront(homeId))
        }
        viewModelScope.launch { preferencesDataStore.setSelectedHomeId(homeId) }
    }

    // Si la casa guardada ya no existe (o no hay), caemos a la primera y la persistimos
    private fun reconcileActiveHome() {
        val homes = _uiState.value.homes
        if (homes.isEmpty()) return
        val stored = selectedHomeId.value
        val active = homes.firstOrNull { it.id == stored } ?: homes.first()
        _uiState.update {
            it.copy(activeHomeId = active.id, recentHomeIds = it.recentHomeIds.moveToFront(active.id))
        }
        if (active.id != stored) {
            viewModelScope.launch { preferencesDataStore.setSelectedHomeId(active.id) }
        }
    }

    // Mueve el id al frente de la lista de recientes (sin duplicados)
    private fun List<String>.moveToFront(id: String): List<String> =
        listOf(id) + this.filter { it != id }

    fun createHome(name: String, typeIndex: Int, photoIndex: Int?, shareEmail: String? = null) {
        if (_uiState.value.isCreating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }
            val type = HomeType.entries.getOrElse(typeIndex) { HomeType.OTRO }
            when (val result = homesRepository.createHome(name, type, photoIndex)) {
                is AppResult.Success -> {
                    // Si se ingresó un email, compartimos la casa recién creada (opcional)
                    if (!shareEmail.isNullOrBlank() && result.data.id.isNotBlank()) {
                        homesRepository.shareHome(result.data.id, shareEmail)
                    }
                    // Refetch para reflejar el estado real del back-end (ADR-0008)
                    val refreshed = (homesRepository.getHomes() as? AppResult.Success)?.data
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            homeJustCreated = true,
                            homes = refreshed?.map(Home::toSelectable) ?: it.homes,
                        )
                    }
                    reconcileActiveHome()
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isCreating = false, createError = result.error.kind.toMessageRes())
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

    // --- Menú por casa: renombrar / eliminar / compartir ---

    fun openRenameDialog(home: SelectableHome) {
        _uiState.update { it.copy(dialog = HomeDialog.Rename(home), actionError = null) }
    }

    fun openDeleteDialog(home: SelectableHome) {
        _uiState.update { it.copy(dialog = HomeDialog.Delete(home), actionError = null) }
    }

    fun openShareDialog(home: SelectableHome) {
        _uiState.update {
            it.copy(dialog = HomeDialog.Share(home), actionError = null, sharedUsers = emptyList())
        }
        loadSharedUsers(home.id)
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialog = null, actionError = null, sharedUsers = emptyList()) }
    }

    fun renameHome(newName: String) {
        val home = (_uiState.value.dialog as? HomeDialog.Rename)?.home ?: return
        if (_uiState.value.isProcessingAction) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true, actionError = null) }
            when (val result = homesRepository.renameHome(home.id, newName)) {
                is AppResult.Success -> {
                    refreshHomes()
                    _uiState.update { it.copy(isProcessingAction = false, dialog = null) }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isProcessingAction = false,
                        actionError = result.error.kind.toMessageRes(),
                    )
                }
            }
        }
    }

    fun deleteHome() {
        val home = (_uiState.value.dialog as? HomeDialog.Delete)?.home ?: return
        if (_uiState.value.isProcessingAction) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true, actionError = null) }
            when (val result = homesRepository.deleteHome(home.id)) {
                is AppResult.Success -> {
                    refreshHomes()
                    _uiState.update { it.copy(isProcessingAction = false, dialog = null) }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isProcessingAction = false,
                        actionError = result.error.kind.toMessageRes(),
                    )
                }
            }
        }
    }

    fun shareHome(email: String) {
        val home = (_uiState.value.dialog as? HomeDialog.Share)?.home ?: return
        if (_uiState.value.isProcessingAction) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true, actionError = null) }
            when (val result = homesRepository.shareHome(home.id, email)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isProcessingAction = false, sharedUsers = result.data)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isProcessingAction = false, actionError = shareMessageFor(result.error.kind))
                }
            }
        }
    }

    private fun loadSharedUsers(homeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingShared = true) }
            val users = (homesRepository.getSharedUsers(homeId) as? AppResult.Success)?.data.orEmpty()
            _uiState.update { it.copy(isLoadingShared = false, sharedUsers = users) }
        }
    }

    private suspend fun refreshHomes() {
        val refreshed = (homesRepository.getHomes() as? AppResult.Success)?.data
        if (refreshed != null) {
            _uiState.update { it.copy(homes = refreshed.map(Home::toSelectable)) }
            reconcileActiveHome()
        }
    }

    @StringRes
    private fun shareMessageFor(kind: ErrorKind): Int =
        when (kind) {
            ErrorKind.Conflict -> R.string.error_share_already_shared
            ErrorKind.NotFound -> R.string.error_share_user_not_found
            ErrorKind.Validation -> R.string.error_email_invalid
            ErrorKind.Network -> R.string.error_network
            ErrorKind.Unauthorized -> R.string.error_session_expired
            else -> R.string.error_share_generic
        }
}

private fun Home.toSelectable(): SelectableHome =
    SelectableHome(
        id = id,
        name = name,
        roomsCount = 0,
        devicesCount = 0,
        photoIndex = photoIndex,
        type = type,
        ownerName = ownerName,
        ownerEmail = ownerEmail,
    )

class SelectHomeViewModelFactory(
    private val homesRepository: HomesRepository,
    private val sessionManager: SessionManager,
    private val preferencesDataStore: PreferencesDataStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectHomeViewModel::class.java)) {
            return SelectHomeViewModel(homesRepository, sessionManager, preferencesDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
