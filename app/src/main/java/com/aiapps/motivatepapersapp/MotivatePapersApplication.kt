package com.aiapps.motivatepapersapp

import android.app.Application
import com.aiapps.motivatepapersapp.di.AppModule
import com.aiapps.motivatepapersapp.worker.DailyWallpaperWorker

class MotivatePapersApplication : Application() {

    lateinit var appModule: AppModule

    override fun onCreate() {
        super.onCreate()
        appModule = AppModule(this)
        
        // Setup Daily Wallpaper Sync
        DailyWallpaperWorker.enqueuePeriodicWork(this)
    }
}
