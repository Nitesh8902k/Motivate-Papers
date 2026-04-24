package com.aiapps.motivatepapersapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ColorPalette(
    val color1: String,
    val color2: String,
    val color3: String,
    val color4: String
)
