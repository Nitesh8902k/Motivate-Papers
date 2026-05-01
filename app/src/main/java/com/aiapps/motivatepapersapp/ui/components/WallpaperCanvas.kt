package com.aiapps.motivatepapersapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WallpaperCanvas(
    quote: Quote,
    palette: ColorPalette,
    dayOfYear: Int,
    modifier: Modifier = Modifier,
    isCapturing: Boolean = false
) {
    val c1 = Color(android.graphics.Color.parseColor(palette.color1))
    val c2 = Color(android.graphics.Color.parseColor(palette.color2))
    val c3 = Color(android.graphics.Color.parseColor(palette.color3))
    val c4 = Color(android.graphics.Color.parseColor(palette.color4))

    val gradientColors = listOf(c1, c2, c3, c4)
    val textColor = getDarkerAnalogousColor(c1)

    val currentDate = LocalDate.ofYearDay(LocalDate.now().year, dayOfYear.coerceIn(1, 365))
    val dateFormatter = DateTimeFormatter.ofPattern("EEE | MMM dd", Locale.US)
    val dateString = currentDate.format(dateFormatter).uppercase()


    Box(
        modifier = modifier
            .background(Color.White)
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // Dotted Background Layer (Base)
        DottedBackground(
            modifier = Modifier.fillMaxSize(),
            dotColor = Color.Gray.copy(alpha = 0.05f)
        )

        // OUTER CAPSULE
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.65f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {

            // INNER CAPSULE
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {

                // LAYER 1: The Blurred Vibrant Gradient Background
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    c2.copy(alpha = 0.8f),
                                    c3.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // LAYER 2: Light Glass Frosting
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.35f))
                )

                // LAYER 3: The Content (Text and UI elements) - NOT BLURRED
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    // Top Section
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        /*
                        Text(
                            text = "MINDFUL FLOW",
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(0.dp))
                         */
                        Text(
                            text = dateString,
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp
                        )
                    }

                    // Center Section: Quote
                    Text(
                        text = "\"${quote.quote}\"",
                        color = textColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                        maxLines = 4, // Limits the quote to 2 lines (you can adjust this number)
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis // Adds the "..." at the end
                    )

                    // Bottom Section: Palette Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        gradientColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(color)
                                    .border(0.5.dp, Color.White, RoundedCornerShape(50))
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getDarkerAnalogousColor(baseColor: Color): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(android.graphics.Color.argb(
        (baseColor.alpha * 255).toInt(),
        (baseColor.red * 255).toInt(),
        (baseColor.green * 255).toInt(),
        (baseColor.blue * 255).toInt()
    ), hsv)

    hsv[0] = (hsv[0] + 15f) % 360f
    hsv[1] = (hsv[1] + 0.3f).coerceAtMost(1f)
    hsv[2] = (hsv[2] - 0.65f).coerceAtLeast(0.15f)

    return Color(android.graphics.Color.HSVToColor(hsv))
}