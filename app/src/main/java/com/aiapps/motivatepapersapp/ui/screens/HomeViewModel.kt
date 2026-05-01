package com.aiapps.motivatepapersapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.FontRes
import com.aiapps.motivatepapersapp.R
import com.aiapps.motivatepapersapp.data.local.ThemeManager
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import com.aiapps.motivatepapersapp.data.repository.QuoteRepository
import com.aiapps.motivatepapersapp.util.WallpaperHelper
import com.aiapps.motivatepapersapp.util.MindfulFlowWallpaperHelper // Added import for the new helper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

data class FontOption(
    val name: String,
    @FontRes val fontResId: Int
)

val availableFonts = listOf(
    FontOption("Anton", R.font.anton),
    FontOption("Arapey", R.font.arapey),
    FontOption("Caveat", R.font.caveat),
    FontOption("Cedarville Cursive", R.font.cedarville_cursive),
    FontOption("Cinzel", R.font.cinzel),
    FontOption("Cormorant Garamond", R.font.cormorant_garamond),
    FontOption("Dancing Script", R.font.dancing_script),
    FontOption("Frank Ruhl Libre", R.font.frank_ruhl_libre),
    FontOption("Instrument Serif", R.font.instrumentserif),
    FontOption("Lora", R.font.lora),
    FontOption("Manrope", R.font.manrope),
    FontOption("Montserrat", R.font.montserrat),
    FontOption("Oswald", R.font.oswald),
    FontOption("Pacifico", R.font.pacifico_regular),
    FontOption("Playfair Display", R.font.playfair_display),
    FontOption("Poppins", R.font.poppins),
    FontOption("Raleway", R.font.raleway),
    FontOption("Shippori Mincho", R.font.shippori_mincho),
    FontOption("Teko", R.font.teko),
)

class HomeViewModel(
    private val quoteRepository: QuoteRepository,
    private val themeManager: ThemeManager,
    private val wallpaperHelper: WallpaperHelper, // Original Glassmorphic helper
    private val context: Context
) : ViewModel() {

    // Instantiate the new helper right here using the context that is already injected
    private val mindfulFlowWallpaperHelper = MindfulFlowWallpaperHelper(context)

    private val prefs: SharedPreferences = context.getSharedPreferences("WallpaperPrefs", Context.MODE_PRIVATE)
    private val savedFontId = prefs.getInt("selected_font_id", availableFonts[0].fontResId)
    private val initialFont = availableFonts.find { it.fontResId == savedFontId } ?: availableFonts[0]
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _selectedFont = MutableStateFlow(initialFont)
    val selectedFont: StateFlow<FontOption> = _selectedFont.asStateFlow()

    private val _quoteFetchDay = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))

    init {
        loadData()
    }

    fun selectFont(font: FontOption) {
        _selectedFont.value = font
        prefs.edit().putInt("selected_font_id", font.fontResId).apply()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                themeManager.palettesFlow,
                _quoteFetchDay
            ) { palettes, quoteDay ->
                val calendar = Calendar.getInstance()
                val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

                // 1. Fetch the quote using the changing quoteDay
                val quote = quoteRepository.getQuoteForDay(quoteDay)

                // 2. Fetch the palette using the static currentDay
                val paletteIndex = themeManager.getActivePaletteIndex(currentDay)
                val palette = palettes.getOrElse(paletteIndex) { themeManager.defaultPalettes[0] }

                // 3. Pass currentDay to the UI so the date/progress bar never change
                HomeUiState.Success(quote, palette, currentDay)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }


    fun generateNext() {
        // Generates a random quote index between 1 and 365
        _quoteFetchDay.value = Random.nextInt(1, 366)
    }

    fun changeGlassColor() {
        viewModelScope.launch {
            val successState = _uiState.value as? HomeUiState.Success ?: return@launch
            val currentDay = successState.dayOfYear
            val paletteIndex = themeManager.getActivePaletteIndex(currentDay)

            val newPalette = ColorPalette(
                color1 = getRandomPastelHex(),
                color2 = getRandomPastelHex(),
                color3 = getRandomPastelHex(),
                color4 = getRandomPastelHex()
            )
            themeManager.overridePalette(paletteIndex, newPalette)
        }
    }

    // Updated to require the selected design as a parameter
    fun setAsWallpaper(selectedDesign: WallpaperDesignOption) {
        Log.i("HomeViewModel", "!!! setAsWallpaper button tapped !!! Design: $selectedDesign")

        prefs.edit().putString("selected_design", selectedDesign.name).apply()

        viewModelScope.launch {
            val successState = _uiState.value as? HomeUiState.Success ?: run {
                Log.e("HomeViewModel", "setAsWallpaper: Not in Success state")
                return@launch
            }

            try {
                val result = when (selectedDesign) {
                    WallpaperDesignOption.GRADIENT_GLASS -> {
                        Log.i("HomeViewModel", "Step 1: Capturing Glassmorphic bitmap...")
                        val bitmap = wallpaperHelper.captureWallpaperBitmap(
                            successState.quote,
                            successState.palette,
                            successState.dayOfYear,
                            _selectedFont.value.fontResId
                        )
                        wallpaperHelper.setWallpaper(bitmap)
                    }

                    WallpaperDesignOption.STATIC_MINIMAL -> {
                        Log.i("HomeViewModel", "Step 1: Capturing Mindful Flow bitmap...")

                        // Automatically switch to dark mode between 6 PM and 6 AM
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        val isDarkMode = hour >= 18 || hour < 6

                        val currentBackground = if (isDarkMode) {
                            R.drawable.night_bg  // Your dark image name
                        } else {
                            R.drawable.day_bg // Your light image name
                        }

                        val bitmap = mindfulFlowWallpaperHelper.captureWallpaperBitmap(
                            quote = successState.quote,
                            dayOfYear = successState.dayOfYear,
                            fontResId = _selectedFont.value.fontResId,
                            backgroundResId = currentBackground, // Your static image
                            isDarkMode = isDarkMode
                        )
                        mindfulFlowWallpaperHelper.setWallpaper(bitmap)
                    }
                }

                Log.i("HomeViewModel", "Step 2: Set wallpaper result: $result")
                if (result) {
                    Log.i("HomeViewModel", "Wallpaper set successfully!")
                } else {
                    Log.e("HomeViewModel", "Wallpaper set returned false")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "CRITICAL: Failed in setAsWallpaper flow", e)
            }
        }
    }

    private fun getRandomPastelHex(): String {
        val base = 200
        val r = (base + (Math.random() * (255 - base)).toInt())
        val g = (base + (Math.random() * (255 - base)).toInt())
        val b = (base + (Math.random() * (255 - base)).toInt())
        return String.format("#%02X%02X%02X", r, g, b)
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val quote: Quote, val palette: ColorPalette, val dayOfYear: Int) : HomeUiState()
}