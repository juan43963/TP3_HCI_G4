package com.grupo4.smarthome.ui.screens.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var homeId: String? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onHomeChanged(newHomeId: String?) {
        if (newHomeId == homeId) return
        homeId = newHomeId
        _uiState.update { it.copy(searchQuery = "") }
    }
}
