package com.grupo4.smarthome.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.core.result.AppResult
import com.grupo4.smarthome.core.result.toMessageRes
import com.grupo4.smarthome.data.repository.DevicesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val devicesRepository: DevicesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

   
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = devicesRepository.getLogs(limit = PAGE_SIZE, offset = 0)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        logs = result.data,
                        canLoadMore = result.data.size == PAGE_SIZE,
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.kind.toMessageRes())
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.canLoadMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            when (val result = devicesRepository.getLogs(limit = PAGE_SIZE, offset = state.logs.size)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        logs = it.logs + result.data,
                        canLoadMore = result.data.size == PAGE_SIZE,
                    )
                }
                is AppResult.Error -> _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 30
    }
}

class HistoryViewModelFactory(
    private val devicesRepository: DevicesRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(devicesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
