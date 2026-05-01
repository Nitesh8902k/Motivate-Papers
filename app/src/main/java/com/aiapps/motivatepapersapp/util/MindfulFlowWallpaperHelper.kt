package com.aiapps.motivatepapersapp.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.aiapps.motivatepapersapp.data.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MindfulFlowWallpaperHelper(private val context: Context) {

    private companion object {
        const val TAG = "MindfulFlowHelper"
    }

    /**
     * Applies the given Bitmap to the Lock screen (and potentially Home screen depending on OS).
     */
    suspend fun setWallpaper(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        Log.i(TAG, "setWallpaper: Starting for bitmap ${bitmap.width}x${bitmap.height}")
        val wallpaperManager = WallpaperManager.getInstance(context)

        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Applying lock screen wallpaper...", Toast.LENGTH_SHORT).show()
            }

            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val resLock = try {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                } catch (e: Exception) {
                    Log.w(TAG, "Lock screen set failed: ${e.message}")
                    -1
                }
                resLock > 0
            } else {
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
     * Renders the Mindful Flow design into a high-quality Bitmap off-screen.
     */
    suspend fun captureWallpaperBitmap(
        quote: Quote,
        dayOfYear: Int,
        fontResId: Int,
        backgroundResId: Int,
        isDarkMode: Boolean
    ): Bitmap = withContext(Dispatchers.Default) {
        Log.i(TAG, "captureWallpaperBitmap (MindfulFlow): Start for day $dayOfYear")

        // 1. Setup Canvas dimensions based on screen size
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val bounds = windowManager.currentWindowMetrics.bounds
        val width = bounds.width()
        val height = bounds.height()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 2. Define Hardcoded Theme Colors
        val accentColor = if (isDarkMode) android.graphics.Color.parseColor("#D4AF37") else android.graphics.Color.parseColor("#9F7E5C")
        val primaryTextColor = if (isDarkMode) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#9F7E5C")
        val trackColor = if (isDarkMode) android.graphics.Color.argb(76, 255, 255, 255) else android.graphics.Color.argb(50, 0, 0, 0)

        // 3. Draw Static Background Image (Center Crop scaling)
        val options = BitmapFactory.Options().apply { inMutable = true }
        val bgBitmap = BitmapFactory.decodeResource(context.resources, backgroundResId, options)

        if (bgBitmap != null) {
            val scale = maxOf(width.toFloat() / bgBitmap.width, height.toFloat() / bgBitmap.height)
            val scaledWidth = bgBitmap.width * scale
            val scaledHeight = bgBitmap.height * scale
            val left = (width - scaledWidth) / 2f
            val top = (height - scaledHeight) / 2f
            val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
            canvas.drawBitmap(bgBitmap, null, destRect, null)
            bgBitmap.recycle() // Free memory
        } else {
            // Fallback to solid color if image fails to load
            canvas.drawColor(if (isDarkMode) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }

        // 4. Setup Text Paints
        val mindfulFlowPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            letterSpacing = 0.3f
            textAlign = Paint.Align.CENTER
        }

        val quoteMarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 100f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val customTypeface = ResourcesCompat.getFont(context, fontResId)
        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryTextColor
            textSize = 72f
            typeface = customTypeface ?: Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val dateLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 32f
            letterSpacing = 0.25f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val dateValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryTextColor
            textSize = 42f
            letterSpacing = 0.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        // 5. Draw "MINDFUL FLOW" at the top
        canvas.drawText("MINDFUL FLOW", width / 2f, height * 0.15f, mindfulFlowPaint)

        // Draw a small icon/sparkle below it (Optional based on mockup)
        val sparklePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 100f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("☼", width / 2f, height * 0.15f + 80f, sparklePaint) // You can replace this with a drawable icon if preferred

        // 6. Setup Quote Layout (handles multi-line text wrapping automatically)
        val quoteMargin = 140
        val textLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(quote.quote, 0, quote.quote.length, quotePaint, width - (quoteMargin * 2))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(quote.quote, quotePaint, width - (quoteMargin * 2), Layout.Alignment.ALIGN_NORMAL, 1.2f, 0f, false)
        }

        // Calculate dynamic vertical positions based on quote height
        val centerY = height * 0.45f
        val textTopY = centerY - (textLayout.height / 2f)
        val textBottomY = centerY + (textLayout.height / 2f)

        // Draw Opening Quote Mark
        canvas.drawText("“", width / 2f, textTopY - 60f, quoteMarkPaint)

        // Draw Quote
        canvas.save()
        canvas.translate(width / 2f, textTopY)
        textLayout.draw(canvas)
        canvas.restore()

        // Draw Closing Quote Mark
        canvas.drawText("”", width / 2f, textBottomY + 110f, quoteMarkPaint)

        // Draw Divider Line
        val dividerY = textBottomY + 180f
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(width / 2f - 60f, dividerY, width / 2f + 60f, dividerY, dividerPaint)

        // 7. Draw Date Strings
        val localDate = LocalDate.ofYearDay(LocalDate.now().year, dayOfYear.coerceIn(1, 365))
        val dayOfWeekStr = localDate.format(DateTimeFormatter.ofPattern("EEEE", Locale.US)).uppercase()
        val monthDayStr = localDate.format(DateTimeFormatter.ofPattern("MMMM dd", Locale.US)).uppercase()

        val dateStartY = dividerY + 80f
        canvas.drawText(dayOfWeekStr, width / 2f, dateStartY, dateLabelPaint)
        canvas.drawText(monthDayStr, width / 2f, dateStartY + 60f, dateValuePaint)

        // 8. Draw Year Passed Percentage Bar at the bottom
        val year = LocalDate.now().year
        val isLeapYear = java.time.Year.of(year).isLeap
        val totalDays = if (isLeapYear) 366f else 365f
        val progress = dayOfYear / totalDays

        val barWidth = width * 0.65f
        val barHeight = 8f
        val barLeft = (width - barWidth) / 2f
        val barTop = height * 0.82f

        // Empty Track
        val trackLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = trackColor
            style = Paint.Style.STROKE
            strokeWidth = barHeight
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(barLeft, barTop, barLeft + barWidth, barTop, trackLinePaint)

        // Filled Progress
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = barHeight
            strokeCap = Paint.Cap.ROUND
        }
        val currentProgressWidth = barWidth * progress
        canvas.drawLine(barLeft, barTop, barLeft + currentProgressWidth, barTop, fillPaint)

        // Glow/Dot at the end of the progress line
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDarkMode) android.graphics.Color.parseColor("#FFF2C8") else accentColor
            style = Paint.Style.FILL
            setShadowLayer(15f, 0f, 0f, accentColor) // Creates a slight glow effect
        }
        canvas.drawCircle(barLeft + currentProgressWidth, barTop, barHeight * 1.5f, dotPaint)

        // Progress Text ("YEAR PASSED: 82%")
        val progressText = "YEAR PASSED: ${(progress * 100).toInt()}%"
        val progressTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 32f
            letterSpacing = 0.25f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(progressText, width / 2f, barTop + 60f, progressTextPaint)

        Log.i(TAG, "captureWallpaperBitmap (MindfulFlow): Success")
        bitmap
    }
}