package com.aiapps.motivatepapersapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WallpaperCanvas(
    quote: Quote,
    palette: ColorPalette,
    modifier: Modifier = Modifier,
    dayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR),
    isCapturing: Boolean = false
) {
    val colors = listOf(
        safeParseColor(palette.color1),
        safeParseColor(palette.color2),
        safeParseColor(palette.color3),
        safeParseColor(palette.color4)
    )

    val textColor = getDarkerAnalogous(colors[0])
    val accentColor = Color(0xFFD4AF37).copy(alpha = 0.6f) // Rose Gold/Metallic vibe

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) {
        // 1. Subtle Dotted Texture
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDottedGrid(Color.Black.copy(alpha = 0.05f))
        }

        // 2. Outer Capsule (Glass Card)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.65f)
                .clip(RoundedCornerShape(60.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), accentColor)),
                    shape = RoundedCornerShape(60.dp)
                )
        ) {
            // Background Blur layer - Reduce or disable blur during capture if it causes black screen issues
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.15f))
                    .then(if (isCapturing) Modifier else Modifier.blur(30.dp))
            )

            // Content inside Outer Capsule
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 3. Inner Capsule (Quote Container)
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(colors[1].copy(alpha = 0.4f), Color.Transparent),
                                center = Offset.Unspecified,
                                radius = 800f
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 4. Top Text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MINDFUL FLOW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 4.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = getCurrentFormattedDate(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    color = textColor.copy(alpha = 0.4f)
                                )
                            )
                        }

                        // 5. Center Text (Quote)
                        Text(
                            text = "\"${quote.quote}\"",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 34.sp,
                                color = textColor
                            ),
                            textAlign = TextAlign.Center
                        )

                        // 6. Bottom Elements (Pagination Dots)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(4) { index ->
                                val isSelected = index == (dayOfYear % 4)
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 10.dp else 6.dp)
                                        .background(
                                            if (isSelected) Color(0xFFFFD700) else Color.LightGray.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                        .then(
                                            if (isSelected) Modifier.blur(2.dp) else Modifier
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Bottom Progress Section (Optional: as per mockup)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val progress = dayOfYear / 365f
            
            // Year Passed Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(accentColor, colors[0].copy(alpha = 0.8f))
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Year Passed: ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = textColor.copy(alpha = 0.6f)
                )
            )
        }
    }
}

private fun DrawScope.drawDottedGrid(color: Color) {
    val dotSpacing = 16.dp.toPx()
    val dotRadius = 1.dp.toPx()
    
    for (x in 0..size.width.toInt() step dotSpacing.toInt()) {
        for (y in 0..size.height.toInt() step dotSpacing.toInt()) {
            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
    }
}

private fun safeParseColor(hex: String): Color {
    return try {
        val colorStr = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(colorStr))
    } catch (e: Exception) {
        android.util.Log.e("WallpaperCanvas", "Failed to parse color: " + hex)
        Color.Gray
    }
}

private fun getDarkerAnalogous(baseColor: Color): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (baseColor.red * 255).toInt(),
        (baseColor.green * 255).toInt(),
        (baseColor.blue * 255).toInt(),
        hsv
    )
    hsv[2] *= 0.4f // Significantly darken for readability
    hsv[1] *= 1.1f // Slightly more saturated
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun getCurrentFormattedDate(): String {
    val sdf = SimpleDateFormat("EEEE | MMM dd", Locale.getDefault())
    return sdf.format(Date()).uppercase()
}
