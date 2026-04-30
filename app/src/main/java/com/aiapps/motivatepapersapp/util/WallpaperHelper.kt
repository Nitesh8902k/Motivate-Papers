package com.aiapps.motivatepapersapp.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.graphics.createBitmap
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import com.aiapps.motivatepapersapp.ui.components.WallpaperCanvas
import com.aiapps.motivatepapersapp.ui.theme.MotivatePapersAppTheme
import kotlinx.coroutines.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size

class WallpaperHelper(private val context: Context) {

    private companion object {
        const val TAG = "WallpaperHelper"
    }

    /**
     * Applies the given Bitmap to both the System and Lock screens.
     */
    suspend fun setWallpaper(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        Log.i(TAG, "setWallpaper: Starting for bitmap ${bitmap.width}x${bitmap.height}")
        val wallpaperManager = WallpaperManager.getInstance(context)

        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Applying lock screen wallpaper...", Toast.LENGTH_SHORT).show()
            }

            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d(TAG, "Setting wallpaper for Lock screen ONLY")
                val resLock = try {
                    // Only use FLAG_LOCK here
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                } catch (e: Exception) {
                    Log.w(TAG, "Lock screen set failed: ${e.message}")
                    -1
                }
                resLock > 0
            } else {
                // Android versions before Nougat (API 24) don't natively support separating
                // lock screen and home screen wallpapers through this standard API.
                Log.w(TAG, "Specific lock screen wallpaper not supported below Android N")
                false
            }

            if (result) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lock Screen Wallpaper Applied!", Toast.LENGTH_SHORT).show()
                }
                Log.i(TAG, "setWallpaper: Completed successfully")
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to apply lock screen wallpaper.", Toast.LENGTH_SHORT).show()
                }
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "setWallpaper: Critical failure", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to set wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }

    /**
     * Renders the WallpaperCanvas Composable into a high-quality Bitmap off-screen.
     */
    suspend fun captureWallpaperBitmap(
        quote: Quote,
        palette: ColorPalette,
        dayOfYear: Int,
        fontResId: Int
    ): Bitmap = withContext(Dispatchers.Default) {
        Log.i(TAG, "captureWallpaperBitmap (Native): Start for day $dayOfYear")

        // 1. Setup Canvas dimensions based on screen size
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val bounds = windowManager.currentWindowMetrics.bounds
        val width = bounds.width()
        val height = bounds.height()

        // Create a standard software bitmap (100% safe for background workers)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 2. Parse Colors
        val c1 = android.graphics.Color.parseColor(palette.color1)
        val c2 = android.graphics.Color.parseColor(palette.color2)
        val c3 = android.graphics.Color.parseColor(palette.color3)
        val c4 = android.graphics.Color.parseColor(palette.color4)

        // 3. Draw Background Vertical Gradient
        val bgPaint = android.graphics.Paint().apply {
            shader = android.graphics.LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(c1, c2, c3, c4),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 4. Draw Dotted Background
        val dotPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(35, 128, 128, 128) // Gray with 15% alpha
            isAntiAlias = true
        }
        val spacing = 30f
        for (x in 0..width step spacing.toInt()) {
            for (y in 0..height step spacing.toInt()) {
                canvas.drawCircle(x.toFloat(), y.toFloat(), 4f, dotPaint)
            }
        }

        // 5. Calculate Capsule Dimensions
        val outerWidth = width * 0.8f
        val outerHeight = height * 0.55f
        val outerLeft = (width - outerWidth) / 2f
        val outerTop = (height - outerHeight) / 2.2f

        val innerWidth = outerWidth * 0.8f
        val innerHeight = outerHeight * 0.6f
        val innerLeft = outerLeft + (outerWidth - innerWidth) / 2f
        val innerTop = outerTop + (outerHeight - innerHeight) / 2f

        // 6. Draw Outer Capsule
        val outerRect = android.graphics.RectF(outerLeft, outerTop, outerLeft + outerWidth, outerTop + outerHeight)
        val outerBgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(255, 249, 246, 241)
            isAntiAlias = true
            // Add soft outer drop shadow: radius (blur size), dx (horizontal offset), dy (vertical offset), shadowColor
            setShadowLayer(65f, 0f, 75f, android.graphics.Color.argb(50, 0, 0, 0))
        }
        val edgeCenterColor = android.graphics.Color.parseColor("#EDD8C5")
        val cornerColor = android.graphics.Color.parseColor("#A1836B")

        val outerStrokePaint = android.graphics.Paint().apply {
            // SweepGradient goes clockwise starting from 3 o'clock (0 degrees).
            // We alternate edge colors (0, 90, 180, 270) and corner colors (45, 135, 225, 315).
            shader = android.graphics.SweepGradient(
                outerRect.centerX(), outerRect.centerY(),
                intArrayOf(
                    edgeCenterColor, cornerColor, edgeCenterColor, cornerColor,
                    edgeCenterColor, cornerColor, edgeCenterColor, cornerColor, edgeCenterColor
                ),
                floatArrayOf(
                    0f,      // 0° (Right Edge)
                    0.125f,  // 45° (Bottom-Right Corner)
                    0.25f,   // 90° (Bottom Edge)
                    0.375f,  // 135° (Bottom-Left Corner)
                    0.5f,    // 180° (Left Edge)
                    0.625f,  // 225° (Top-Left Corner)
                    0.75f,   // 270° (Top Edge)
                    0.875f,  // 315° (Top-Right Corner)
                    1f       // 360° (Back to Right Edge)
                )
            )
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 16f
            isAntiAlias = true
        }
        canvas.drawRoundRect(outerRect, 300f, 300f, outerBgPaint)
        canvas.drawRoundRect(outerRect, 300f, 300f, outerStrokePaint)

        // 7. Draw Inner Capsule (Radial Gradient + Frosting)
        val innerRect = android.graphics.RectF(innerLeft, innerTop, innerLeft + innerWidth, innerTop + innerHeight)
        val innerShadowPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE // Solid color ensures shadow color works
            setShadowLayer(95f, 0f, 90f, android.graphics.Color.argb(30, 0, 0, 0))
            isAntiAlias = true
        }
        canvas.drawRoundRect(innerRect, 230f, 230f, innerShadowPaint)
        val innerBgPaint = android.graphics.Paint().apply {
            // Reusing the c1, c2, c3, c4 palette as a linear gradient for the inner capsule
            shader = android.graphics.LinearGradient(
                innerRect.left, innerRect.top, innerRect.left, innerRect.bottom,
                intArrayOf(c1, c2, c3, c4),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
            isAntiAlias = true
        }
        val innerFrostPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(64, 255, 255, 255) // 25% White
            isAntiAlias = true
        }
        val innerStrokePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(128, 255, 255, 255) // 50% White
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(innerRect, 230f, 230f, innerBgPaint)
        canvas.drawRoundRect(innerRect, 230f, 230f, innerFrostPaint)
        canvas.drawRoundRect(innerRect, 230f, 230f, innerStrokePaint)

        // 8. Draw Text & Content
        val textColor = getNativeDarkerAnalogousColor(c1)

        // Draw "MINDFUL FLOW"
        val headerPaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(178, android.graphics.Color.red(textColor), android.graphics.Color.green(textColor), android.graphics.Color.blue(textColor)) // 70% opacity
            textSize = 45f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            letterSpacing = 0.25f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("MINDFUL FLOW", innerRect.centerX(), innerRect.top + 130f, headerPaint)

        // Draw Date String
        val dateString = java.time.LocalDate.ofYearDay(java.time.LocalDate.now().year, dayOfYear.coerceIn(1, 365))
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE | MMM dd", java.util.Locale.US)).uppercase()
        val datePaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(153, android.graphics.Color.red(textColor), android.graphics.Color.green(textColor), android.graphics.Color.blue(textColor)) // 60% opacity
            textSize = 40f
            letterSpacing = 0.2f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText(dateString, innerRect.centerX(), innerRect.top + 180f, datePaint)

        val customTypeface = androidx.core.content.res.ResourcesCompat.getFont(context, fontResId)

        // Draw Quote using StaticLayout (handles multi-line text wrapping automatically)
        val quotePaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(200, android.graphics.Color.red(textColor), android.graphics.Color.green(textColor), android.graphics.Color.blue(textColor)) // 70% opacity
            textSize = 68f
            typeface = customTypeface ?: android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.NORMAL)
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val textLayout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.text.StaticLayout.Builder.obtain("\"${quote.quote}\"", 0, quote.quote.length + 2, quotePaint, (innerWidth - 100).toInt())
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .build()
        } else {
            @Suppress("DEPRECATION")
            android.text.StaticLayout("\"${quote.quote}\"", quotePaint, (innerWidth - 100).toInt(), android.text.Layout.Alignment.ALIGN_NORMAL, 1.2f, 0f, false)
        }

        // Center the multi-line quote vertically inside the capsule
        val textY = innerRect.centerY() - (textLayout.height / 2f) + 50
        canvas.save()
        canvas.translate(innerRect.centerX(), textY)
        textLayout.draw(canvas)
        canvas.restore()

        // 9. Draw Palette Dots at the bottom
        val dotRadius = 16f
        val dotSpacing = 24f
        val totalDotsWidth = (4 * (dotRadius * 2)) + (3 * dotSpacing)
        var startX = innerRect.centerX() - (totalDotsWidth / 2f) + dotRadius
        val startY = innerRect.bottom - 80f

        val colors = intArrayOf(c1, c2, c3, c4)
        val dotStrokePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        for (color in colors) {
            val pPaint = android.graphics.Paint().apply {
                this.color = color
                isAntiAlias = true
            }
            canvas.drawCircle(startX, startY, dotRadius, pPaint)
            canvas.drawCircle(startX, startY, dotRadius, dotStrokePaint)
            startX += (dotRadius * 2) + dotSpacing
        }
        // 10. Draw Year Passed Percentage Bar (Outside capsules, with specified custom colors)
        val year = java.time.LocalDate.now().year
        val isLeapYear = java.time.Year.of(year).isLeap
        val totalDays = if (isLeapYear) 366f else 365f
        val progress = dayOfYear / totalDays

        // Positioned based on the full screen dimensions, not the capsule
        val barWidth = width * 0.65f
        val barHeight = 25f
        val barLeft = (width - barWidth) / 2f
        val barTop = height - 380f // Positioned safely at the bottom of the screen

        // Track (Semi-transparent white line with rounded caps)
        val trackPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(60, 255, 255, 255) // ~24% White
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = barHeight
            strokeCap = android.graphics.Paint.Cap.ROUND
            isAntiAlias = true
        }
        canvas.drawLine(barLeft, barTop, barLeft + barWidth, barTop, trackPaint)

        // Custom Gradient Colors for the bar
        val barGradientColor1 = android.graphics.Color.parseColor("#9F7E5C")
        val barGradientColor2 = android.graphics.Color.parseColor("#F1D8C2")
        val barGradientColor3 = android.graphics.Color.parseColor("#9F7E5C")

        // Fill (Foreground progress line matching the custom gradient, with rounded caps)
        val fillPaint = android.graphics.Paint().apply {
            shader = android.graphics.LinearGradient(
                barLeft, barTop, barLeft + barWidth, barTop,
                intArrayOf(barGradientColor1, barGradientColor2, barGradientColor3),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = barHeight
            strokeCap = android.graphics.Paint.Cap.ROUND
            isAntiAlias = true
        }
        val currentProgressWidth = barWidth * progress
        canvas.drawLine(barLeft, barTop, barLeft + currentProgressWidth, barTop, fillPaint)

        // Labels (Left: "YEAR PASSED", Right: Percentage)
        val labelTextPaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#9F7E5C") // Applied specific custom color
            textSize = 42f
            letterSpacing = 0.25f // Wide spacing as seen in the image
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("YEAR PASSED", width / 2f, barTop - 36f, labelTextPaint)

        val progressText = "${(progress * 100).toInt()}%"
        val progressTextPaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#9F7E5C")
            textSize = 42f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        canvas.drawText(progressText, barLeft + barWidth, barTop - 36f, progressTextPaint)

        Log.i(TAG, "captureWallpaperBitmap (Native): Success")
        bitmap
    }
}
private fun getNativeDarkerAnalogousColor(baseColorInt: Int): Int {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(baseColorInt, hsv)

    hsv[0] = (hsv[0] + 15f) % 360f
    hsv[1] = (hsv[1] + 0.3f).coerceAtMost(1f)
    hsv[2] = (hsv[2] - 0.65f).coerceAtLeast(0.15f)

    return android.graphics.Color.HSVToColor(hsv)
}