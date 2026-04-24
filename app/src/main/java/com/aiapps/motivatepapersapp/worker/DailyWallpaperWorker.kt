package com.aiapps.motivatepapersapp.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aiapps.motivatepapersapp.MotivatePapersApplication
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
        val wallpaperHelper = appModule.wallpaperHelper

        return try {
            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

            // Fetch Quote
            val quote = quoteRepository.getQuoteForDay(dayOfYear)

            // Fetch Palette
            val palettes = themeManager.palettesFlow.first()
            val paletteIndex = themeManager.getActivePaletteIndex(dayOfYear)
            val palette = palettes.getOrElse(paletteIndex) { themeManager.defaultPalettes[0] }

            // Render to Bitmap
            val bitmap = wallpaperHelper.captureWallpaperBitmap(quote, palette, dayOfYear)

            // Set Wallpaper
            val success = wallpaperHelper.setWallpaper(bitmap)

            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "DailyWallpaperUpdate"

        fun enqueuePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            // Calculate initial delay to target midnight
            val currentTime = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
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
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
