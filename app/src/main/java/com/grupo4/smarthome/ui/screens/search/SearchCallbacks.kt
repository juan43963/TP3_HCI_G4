package com.grupo4.smarthome.ui.screens.search

import com.grupo4.smarthome.data.model.ActionArg

data class SearchCallbacks(
    val onQueryChange: (String) -> Unit = {},
    val onBack: () -> Unit = {},
    val onRoomClick: (String) -> Unit = {},
    val onToggleRoomFavorite: (String) -> Unit = {},
    val onToggleDevice: (String) -> Unit = {},
    val onDeviceValueChange: (String, Int) -> Unit = { _, _ -> },
    val onDeviceAction: (String, String, List<ActionArg>) -> Unit = { _, _, _ -> },
    val onRunRoutine: (String) -> Unit = {},
    val onToggleRoutineFavorite: (String) -> Unit = {},
    val onDismissRoomError: () -> Unit = {},
    val onConsumeDeviceActionResult: () -> Unit = {},
    val onDismissRoutineError: () -> Unit = {},
    val onDismissRoutineInfo: () -> Unit = {},
)
