package com.grupo4.smarthome.ui.screens.selecthome

import androidx.annotation.DrawableRes
import com.grupo4.smarthome.R

// Fotos preset empaquetadas en la app (la API no sube imágenes; se elige una y se guarda el índice).
@DrawableRes
val homePresetPhotos: List<Int> = listOf(
    R.drawable.casa_principal,
    R.drawable.casa_finde,
    R.drawable.depto,
    R.drawable.oficina,
)

@DrawableRes
fun homePhotoRes(index: Int?): Int? = index?.let { homePresetPhotos.getOrNull(it) }
