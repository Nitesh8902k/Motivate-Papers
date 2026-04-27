package com.aiapps.motivatepapersapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DottedBackground(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White.copy(alpha = 0.15f)
) {
    // We remove the Box and the Brush.verticalGradient here.
    // This component should ONLY draw the dots, acting as a transparent overlay.
    Canvas(modifier = modifier.fillMaxSize()) {
        val dotSpacing = 20.dp.toPx()
        val dotSize = 2.dp.toPx()

        for (x in 0..size.width.toInt() step dotSpacing.toInt()) {
            for (y in 0..size.height.toInt() step dotSpacing.toInt()) {
                drawCircle(
                    color = dotColor,
                    radius = dotSize / 2,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
        }
    }
}