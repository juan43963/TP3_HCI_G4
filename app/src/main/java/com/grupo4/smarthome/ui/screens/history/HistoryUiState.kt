package com.grupo4.smarthome.ui.screens.history

import androidx.annotation.StringRes
import com.grupo4.smarthome.data.model.DeviceLog

data class HistoryUiState(
    val logs: List<DeviceLog> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    @StringRes val errorMessage: Int? = null,
)
