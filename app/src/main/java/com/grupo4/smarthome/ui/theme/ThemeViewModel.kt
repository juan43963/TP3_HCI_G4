package com.grupo4.smarthome.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grupo4.smarthome.data.local.PreferencesDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val preferences: PreferencesDataStore,
) : ViewModel() {
    val themePreference: StateFlow<ThemePreference> =
        preferences.themePreference
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreference.SYSTEM)

    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch { preferences.setThemePreference(theme) }
    }
}

class ThemeViewModelFactory(
    private val preferences: PreferencesDataStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            return ThemeViewModel(preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
