package com.aiapps.motivatepapersapp.di

import android.content.Context
import com.aiapps.motivatepapersapp.data.local.ThemeManager
import com.aiapps.motivatepapersapp.data.repository.QuoteRepository
import com.aiapps.motivatepapersapp.util.WallpaperHelper

class AppModule(private val context: Context) {

    val themeManager: ThemeManager by lazy {
        ThemeManager(context)
    }

    val quoteRepository: QuoteRepository by lazy {
        QuoteRepository(context)
    }

    val wallpaperHelper: WallpaperHelper by lazy {
        WallpaperHelper(context)
    }
}
