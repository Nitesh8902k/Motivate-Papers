package com.aiapps.motivatepapersapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiapps.motivatepapersapp.data.local.ThemeManager
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import com.aiapps.motivatepapersapp.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

class GalleryViewModel(
    private val quoteRepository: QuoteRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allQuotes = MutableStateFlow<List<Quote>>(emptyList())
    private val _selectedCategory = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _allQuotes.value = quoteRepository.getAllQuotes()
            
            combine(
                _allQuotes,
                _searchQuery,
                _selectedCategory,
                themeManager.palettesFlow
            ) { quotes, query, category, palettes ->
                val filteredQuotes = quotes.filter {
                    (it.quote.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)) &&
                    (category == null || it.category == category)
                }
                
                val calendar = Calendar.getInstance()
                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                val paletteIndex = themeManager.getActivePaletteIndex(dayOfYear)
                val palette = palettes.getOrElse(paletteIndex) { themeManager.defaultPalettes[0] }
                
                val categories = quotes.map { it.category }.distinct()
                
                GalleryUiState.Success(filteredQuotes, palette, categories)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }
}

sealed class GalleryUiState {
    object Loading : GalleryUiState()
    data class Success(val quotes: List<Quote>, val palette: ColorPalette, val categories: List<String>) : GalleryUiState()
}
