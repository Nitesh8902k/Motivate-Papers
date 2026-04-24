package com.aiapps.motivatepapersapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiapps.motivatepapersapp.data.local.ThemeManager
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import com.aiapps.motivatepapersapp.data.repository.QuoteRepository
import com.aiapps.motivatepapersapp.util.WallpaperHelper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val quoteRepository: QuoteRepository,
    private val themeManager: ThemeManager,
    private val wallpaperHelper: WallpaperHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _previewDay = MutableStateFlow<Int?>(null)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                themeManager.palettesFlow,
                _previewDay
            ) { palettes, previewDay ->
                val calendar = Calendar.getInstance()
                val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
                val dayToUse = previewDay ?: currentDay
                
                val quote = quoteRepository.getQuoteForDay(dayToUse)
                val paletteIndex = themeManager.getActivePaletteIndex(dayToUse)
                val palette = palettes.getOrElse(paletteIndex) { themeManager.defaultPalettes[0] }
                
                HomeUiState.Success(quote, palette, dayToUse)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun generateNext() {
        val currentDay = (_uiState.value as? HomeUiState.Success)?.dayOfYear ?: Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        _previewDay.value = (currentDay % 365) + 1
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

    fun setAsWallpaper() {
        Log.i("HomeViewModel", "!!! setAsWallpaper button tapped !!!")
        viewModelScope.launch {
            val successState = _uiState.value as? HomeUiState.Success ?: run {
                Log.e("HomeViewModel", "setAsWallpaper: Not in Success state")
                return@launch
            }
            
            try {
                Log.i("HomeViewModel", "Step 1: Capturing bitmap for day ${successState.dayOfYear}...")
                val bitmap = wallpaperHelper.captureWallpaperBitmap(
                    successState.quote, 
                    successState.palette,
                    successState.dayOfYear
                )
                Log.i("HomeViewModel", "Step 2: Bitmap captured: ${bitmap.width}x${bitmap.height}")
                
                Log.i("HomeViewModel", "Step 3: Calling wallpaperHelper.setWallpaper")
                val result = wallpaperHelper.setWallpaper(bitmap)
                Log.i("HomeViewModel", "Step 4: Set wallpaper result: $result")
                
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
