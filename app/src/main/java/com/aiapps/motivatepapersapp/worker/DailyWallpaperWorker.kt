package com.aiapps.motivatepapersapp.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aiapps.motivatepapersapp.MotivatePapersApplication
import com.aiapps.motivatepapersapp.R
import com.aiapps.motivatepapersapp.util.MindfulFlowWallpaperHelper // Import the new helper
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyWallpaperWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appModule = (applicationContext as MotivatePapersApplication).appModule
        val quoteRepository = appModule.quoteRepository
        val themeManager = appModule.themeManager
        val wallpaperHelper = appModule.wallpaperHelper // Original Helper

        return try {
            val prefs = applicationContext.getSharedPreferences("WallpaperPrefs", Context.MODE_PRIVATE)
            val savedFontId = prefs.getInt("selected_font_id", R.font.poppins)

            // Check which design the user currently has active
            val selectedDesign = prefs.getString("selected_design", "GRADIENT_GLASS")

            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

            // Fetch the daily quote (both designs need this)
            val quote = quoteRepository.getQuoteForDay(dayOfYear)

            val success = if (selectedDesign == "STATIC_MINIMAL") {
                // ==========================================
                // MINDFUL FLOW LOGIC
                // ==========================================
                val isDarkMode = currentHour >= 18 || currentHour < 6
                val backgroundResId = if (isDarkMode) R.drawable.night_bg else R.drawable.day_bg

                // Create the helper manually using the worker's context
                val mindfulHelper = MindfulFlowWallpaperHelper(applicationContext)

                val bitmap = mindfulHelper.captureWallpaperBitmap(
                    quote = quote,
                    dayOfYear = dayOfYear,
                    fontResId = savedFontId,
                    backgroundResId = backgroundResId,
                    isDarkMode = isDarkMode
                )
                mindfulHelper.setWallpaper(bitmap)

            } else {
                // ==========================================
                // ORIGINAL GLASSMORPHIC LOGIC
                // ==========================================
                val palettes = themeManager.palettesFlow.first()
                val paletteIndex = themeManager.getActivePaletteIndex(dayOfYear)
                val palette = palettes.getOrElse(paletteIndex) { themeManager.defaultPalettes[0] }

                val bitmap = wallpaperHelper.captureWallpaperBitmap(quote, palette, dayOfYear, savedFontId)
                wallpaperHelper.setWallpaper(bitmap)
            }

            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        fun enqueuePeriodicWork(context: Context) {
            // We schedule 3 separate daily triggers: Midnight, 6 AM, and 6 PM.
            scheduleUpdateForTime(context, 0, 0, "WallpaperUpdate_Midnight")
            scheduleUpdateForTime(context, 6, 0, "WallpaperUpdate_Morning")
            scheduleUpdateForTime(context, 18, 0, "WallpaperUpdate_Evening")
        }

        private fun scheduleUpdateForTime(context: Context, targetHour: Int, targetMinute: Int, workTag: String) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val currentTime = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If the target time for today has already passed, schedule it for tomorrow
                if (before(currentTime)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

            val workRequest = PeriodicWorkRequestBuilder<DailyWallpaperWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                workTag,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

            Log.i("DailyWallpaperWorker", "Scheduled $workTag to run in ${initialDelay / 1000 / 60} minutes")
        }
    }
}