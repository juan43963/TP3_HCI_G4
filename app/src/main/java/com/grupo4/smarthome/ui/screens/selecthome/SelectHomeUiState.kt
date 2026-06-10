package com.grupo4.smarthome.ui.screens.selecthome

import androidx.annotation.StringRes
import com.grupo4.smarthome.data.model.SharedUser

data class SelectHomeUiState(
    val userName: String = "",
    val homes: List<SelectableHome> = emptyList(),
    val isLoading: Boolean = false,
    @StringRes val errorMessage: Int? = null,
    val isCreating: Boolean = false,
    @StringRes val createError: Int? = null,
    val homeJustCreated: Boolean = false,
    val dialog: HomeDialog? = null,
    val isProcessingAction: Boolean = false,
    @StringRes val actionError: Int? = null,
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
