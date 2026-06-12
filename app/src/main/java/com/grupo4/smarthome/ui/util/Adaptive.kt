package com.grupo4.smarthome.ui.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val TabletMinWidth: Dp = 600.dp

val CompactHeight: Dp = 480.dp

fun isPhoneLandscape(maxWidth: Dp, maxHeight: Dp): Boolean =
    maxWidth >= TabletMinWidth && maxHeight < CompactHeight
