package com.aiapps.motivatepapersapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")

class ThemeManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val PALETTES_KEY = stringPreferencesKey("palettes")

    val defaultPalettes = listOf(
        ColorPalette("#FDE2E4", "#FAD2E1", "#E2ECE9", "#BEE1E6"),
        ColorPalette("#DFE7FD", "#F0EFEB", "#FAD2E1", "#BEE1E6"),
        ColorPalette("#E2ECE9", "#DFE7FD", "#F0EFEB", "#FDE2E4"),
        ColorPalette("#FFF1E6", "#FAD2E1", "#E2ECE9", "#DFE7FD"),
        ColorPalette("#F0EFEB", "#BEE1E6", "#FDE2E4", "#FAD2E1"),
        ColorPalette("#BEE1E6", "#DFE7FD", "#FFF1E6", "#E2ECE9"),
        ColorPalette("#FDE2E4", "#FFF1E6", "#BEE1E6", "#FAD2E1"),
        ColorPalette("#FAD2E1", "#E2ECE9", "#DFE7FD", "#F0EFEB"),
        ColorPalette("#E2ECE9", "#FFF1E6", "#FDE2E4", "#BEE1E6"),
        ColorPalette("#DFE7FD", "#BEE1E6", "#FAD2E1", "#F0EFEB"),
        ColorPalette("#FFF1E6", "#FDE2E4", "#E2ECE9", "#DFE7FD"),
        ColorPalette("#F0EFEB", "#FAD2E1", "#BEE1E6", "#FFF1E6"),
        ColorPalette("#BEE1E6", "#FDE2E4", "#DFE7FD", "#E2ECE9"),
        ColorPalette("#FDE2E4", "#BEE1E6", "#F0EFEB", "#FAD2E1"),
        ColorPalette("#FAD2E1", "#FFF1E6", "#E2ECE9", "#BEE1E6")
    )

    val palettesFlow: Flow<List<ColorPalette>> = context.dataStore.data.map { preferences ->
        val palettesJson = preferences[PALETTES_KEY]
        if (palettesJson == null) {
            defaultPalettes
        } else {
            try {
                json.decodeFromString<List<ColorPalette>>(palettesJson)
            } catch (e: Exception) {
                defaultPalettes
            }
        }
    }

    suspend fun getActivePaletteIndex(dayOfYear: Int): Int {
        return dayOfYear % 15
    }

    suspend fun overridePalette(index: Int, newPalette: ColorPalette) {
        context.dataStore.edit { preferences ->
            val currentPalettes = try {
                val jsonStr = preferences[PALETTES_KEY]
                if (jsonStr != null) json.decodeFromString<List<ColorPalette>>(jsonStr).toMutableList()
                else defaultPalettes.toMutableList()
            } catch (e: Exception) {
                defaultPalettes.toMutableList()
            }
            
            if (index in currentPalettes.indices) {
                currentPalettes[index] = newPalette
                preferences[PALETTES_KEY] = json.encodeToString(currentPalettes)
            }
        }
    }
}
