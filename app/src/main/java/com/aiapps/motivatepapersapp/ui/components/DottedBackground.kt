package com.aiapps.motivatepapersapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aiapps.motivatepapersapp.data.model.ColorPalette

@Composable
fun DottedBackground(
    palette: ColorPalette,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(android.graphics.Color.parseColor(palette.color1)),
        Color(android.graphics.Color.parseColor(palette.color2)),
        Color(android.graphics.Color.parseColor(palette.color3)),
        Color(android.graphics.Color.parseColor(palette.color4))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dotSpacing = 20.dp.toPx()
            val dotSize = 2.dp.toPx()
            val color = Color.White.copy(alpha = 0.15f)

            for (x in 0..size.width.toInt() step dotSpacing.toInt()) {
                for (y in 0..size.height.toInt() step dotSpacing.toInt()) {
                    drawCircle(
                        color = color,
                        radius = dotSize / 2,
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }
    }
}
