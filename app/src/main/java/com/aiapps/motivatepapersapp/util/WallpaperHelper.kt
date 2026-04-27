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
                Toast.makeText(context, "Applying wallpaper...", Toast.LENGTH_SHORT).show()
            }

            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d(TAG, "Setting wallpaper for BOTH System and Lock screens")
                val resBoth = try {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                } catch (e: Exception) {
                    Log.w(TAG, "Combined set failed: ${e.message}")
                    -1
                }

                if (resBoth <= 0) {
                    Log.w(TAG, "Trying System only as fallback")
                    val resSystem = wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    resSystem > 0
                } else {
                    true
                }
            } else {
                wallpaperManager.setBitmap(bitmap)
                true
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Wallpaper Applied!", Toast.LENGTH_SHORT).show()
            }
            Log.i(TAG, "setWallpaper: Completed successfully")
            true
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
        dayOfYear: Int
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
            color = android.graphics.Color.argb(38, 128, 128, 128) // Gray with 15% alpha
            isAntiAlias = true
        }
        val spacing = 60f
        for (x in 0..width step spacing.toInt()) {
            for (y in 0..height step spacing.toInt()) {
                canvas.drawCircle(x.toFloat(), y.toFloat(), 4f, dotPaint)
            }
        }

        // 5. Calculate Capsule Dimensions
        val outerWidth = width * 0.85f
        val outerHeight = height * 0.65f
        val outerLeft = (width - outerWidth) / 2f
        val outerTop = (height - outerHeight) / 2f

        val innerWidth = outerWidth * 0.85f
        val innerHeight = outerHeight * 0.85f
        val innerLeft = outerLeft + (outerWidth - innerWidth) / 2f
        val innerTop = outerTop + (outerHeight - innerHeight) / 2f

        // 6. Draw Outer Capsule
        val outerRect = android.graphics.RectF(outerLeft, outerTop, outerLeft + outerWidth, outerTop + outerHeight)
        val outerBgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(38, 255, 255, 255) // 15% White
            isAntiAlias = true
        }
        val outerStrokePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(76, 255, 255, 255) // 30% White
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(outerRect, 90f, 90f, outerBgPaint)
        canvas.drawRoundRect(outerRect, 90f, 90f, outerStrokePaint)

        // 7. Draw Inner Capsule (Radial Gradient + Frosting)
        val innerRect = android.graphics.RectF(innerLeft, innerTop, innerLeft + innerWidth, innerTop + innerHeight)
        val innerRadialPaint = android.graphics.Paint().apply {
            shader = android.graphics.RadialGradient(
                innerRect.centerX(), innerRect.centerY(), innerWidth,
                intArrayOf(
                    android.graphics.Color.argb(204, android.graphics.Color.red(c2), android.graphics.Color.green(c2), android.graphics.Color.blue(c2)), // c2 at 80%
                    android.graphics.Color.argb(102, android.graphics.Color.red(c3), android.graphics.Color.green(c3), android.graphics.Color.blue(c3)), // c3 at 40%
                    android.graphics.Color.TRANSPARENT
                ),
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
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(innerRect, 70f, 70f, innerRadialPaint)
        canvas.drawRoundRect(innerRect, 70f, 70f, innerFrostPaint)
        canvas.drawRoundRect(innerRect, 70f, 70f, innerStrokePaint)

        // 8. Draw Text & Content
        val textColor = getNativeDarkerAnalogousColor(c1)

        // Draw "MINDFUL FLOW"
        val headerPaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(178, android.graphics.Color.red(textColor), android.graphics.Color.green(textColor), android.graphics.Color.blue(textColor)) // 70% opacity
            textSize = 36f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            letterSpacing = 0.15f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("MINDFUL FLOW", innerRect.centerX(), innerRect.top + 100f, headerPaint)

        // Draw Date String
        val dateString = java.time.LocalDate.ofYearDay(java.time.LocalDate.now().year, dayOfYear.coerceIn(1, 365))
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE | MMM dd", java.util.Locale.US)).uppercase()
        val datePaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(153, android.graphics.Color.red(textColor), android.graphics.Color.green(textColor), android.graphics.Color.blue(textColor)) // 60% opacity
            textSize = 32f
            letterSpacing = 0.1f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText(dateString, innerRect.centerX(), innerRect.top + 150f, datePaint)

        // Draw Quote using StaticLayout (handles multi-line text wrapping automatically)
        val quotePaint = android.text.TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 68f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
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
        val textY = innerRect.centerY() - (textLayout.height / 2f)
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