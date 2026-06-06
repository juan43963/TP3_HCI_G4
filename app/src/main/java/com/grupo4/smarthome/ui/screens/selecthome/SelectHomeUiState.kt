package com.grupo4.smarthome.ui.screens.selecthome

data class SelectHomeUiState(
    val userName: String = "",
    val homes: List<SelectableHome> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCreating: Boolean = false,
    val createError: String? = null,
    val homeJustCreated: Boolean = false,
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && homes.isEmpty()
}
