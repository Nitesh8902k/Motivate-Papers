package com.aiapps.motivatepapersapp.data.model

import androidx.compose.ui.graphics.Color

data class Palette(
    val id: Int,
    val name: String,
    val primary: Color,
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val onPrimary: Color = Color.White
)

object Palettes {
    val all = listOf(
        Palette(0, "Soft Lavender", Color(0xFFB39DDB), Color(0xFFEDE7F6), Color(0xFFD1C4E9)),
        Palette(1, "Minty Fresh", Color(0xFF80CBC4), Color(0xFFE0F2F1), Color(0xFFB2DFDB)),
        Palette(2, "Peach Fuzz", Color(0xFFFFCCBC), Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
        Palette(3, "Sky Blue", Color(0xFF90CAF9), Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
        Palette(4, "Rose Quartz", Color(0xFFF48FB1), Color(0xFFFCE4EC), Color(0xFFF8BBD0)),
        Palette(5, "Sage Green", Color(0xFFA5D6A7), Color(0xFFE8F5E9), Color(0xFFC8E6C9)),
        Palette(6, "Lemon Chiffon", Color(0xFFFFF59D), Color(0xFFFFFDE7), Color(0xFFFFF9C4)),
        Palette(7, "Lilac Mist", Color(0xFFCE93D8), Color(0xFFF3E5F5), Color(0xFFE1BEE7)),
        Palette(8, "Apricot Dream", Color(0xFFFFAB91), Color(0xFFFBE9E7), Color(0xFFFFCCBC)),
        Palette(9, "Ocean Breeze", Color(0xFF81D4FA), Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
        Palette(10, "Periwinkle", Color(0xFF9FA8DA), Color(0xFFE8EAF6), Color(0xFFC5CAE9)),
        Palette(11, "Pale Gold", Color(0xFFFFE082), Color(0xFFFFF8E1), Color(0xFFFFECB3)),
        Palette(12, "Coral Pink", Color(0xFFEF9A9A), Color(0xFFFFEBEE), Color(0xFFFFCDD2)),
        Palette(13, "Teal Glow", Color(0xFF4DB6AC), Color(0xFFE0F2F1), Color(0xFFB2DFDB)),
        Palette(14, "Creamy Berry", Color(0xFFCE93D8), Color(0xFFF3E5F5), Color(0xFFE1BEE7))
    )
}
