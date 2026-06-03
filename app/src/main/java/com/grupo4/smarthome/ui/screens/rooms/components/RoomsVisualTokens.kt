package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.grupo4.smarthome.ui.theme.AccentSoft
import com.grupo4.smarthome.ui.theme.BrandIndigo
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.BrandIndigoLight
import com.grupo4.smarthome.ui.theme.BorderDefault

val RoomsPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(BrandIndigoDark, BrandIndigo, AccentSoft),
)

val RoomsControlGradient = Brush.horizontalGradient(
    colors = listOf(BrandIndigo, AccentSoft),
)

val RoomsIconTileGradient = Brush.linearGradient(
    colors = listOf(BrandIndigoLight, Color(0xFFF7F5FF)),
)

val RoomsSelectedChipContainer = BrandIndigoLight.copy(alpha = 0.78f)
val RoomsSelectedChipBorder = AccentSoft.copy(alpha = 0.5f)
val RoomsControlTrack = Color(0xFFE8ECF7)
val RoomsSwitchInactiveTrack = Color(0xFFE5E7EF)
val RoomsSoftBorder = BorderDefault.copy(alpha = 0.78f)
