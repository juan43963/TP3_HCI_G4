package com.grupo4.smarthome.ui.screens.selecthome

import com.grupo4.smarthome.data.model.SharedUser

data class SelectHomeUiState(
    val userName: String = "",
    val homes: List<SelectableHome> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCreating: Boolean = false,
    val createError: String? = null,
    val homeJustCreated: Boolean = false,
    val dialog: HomeDialog? = null,
    val isProcessingAction: Boolean = false,
    val actionError: String? = null,
    val sharedUsers: List<SharedUser> = emptyList(),
    val isLoadingShared: Boolean = false,
    val activeHomeId: String? = null,
    val recentHomeIds: List<String> = emptyList(),
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && homes.isEmpty()

    // Sidebar (sin scroll): la casa activa primero y luego las últimas que se visitaron, hasta 3
    val drawerHomes: List<SelectableHome>
        get() {
            val active = homes.firstOrNull { it.id == activeHomeId }
            val rest = homes
                .filter { it.id != activeHomeId }
                .sortedBy { home ->
                    recentHomeIds.indexOf(home.id).let { if (it == -1) Int.MAX_VALUE else it }
                }
            return (listOfNotNull(active) + rest).take(3)
        }
}

// Diálogo abierto sobre una casa concreta; null significa que no hay ninguno
sealed interface HomeDialog {
    val home: SelectableHome

    data class Rename(override val home: SelectableHome) : HomeDialog
    data class Delete(override val home: SelectableHome) : HomeDialog
    data class Share(override val home: SelectableHome) : HomeDialog
}
