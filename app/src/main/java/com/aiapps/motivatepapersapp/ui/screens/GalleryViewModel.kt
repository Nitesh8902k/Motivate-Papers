package com.aiapps.motivatepapersapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiapps.motivatepapersapp.data.local.ThemeManager
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import com.aiapps.motivatepapersapp.data.repository.QuoteRepository
import com.aiapps.motivatepapersapp.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

class GalleryViewModel(
    private val quoteRepository: QuoteRepository,
    private val themeManager: ThemeManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allQuotes = MutableStateFlow<List<Quote>>(emptyList())
    private val _selectedCategory = MutableStateFlow<String?>(null)

    // NEW: State to hold the random seed. 0 means normal sorting.
    private val _randomSeed = MutableStateFlow(0L)

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
                userPreferencesRepository.bookmarkedQuotesFlow,
                // We combine the last two flows into a Pair to avoid the 5-flow limit
                combine(themeManager.palettesFlow, _randomSeed) { palettes, seed -> Pair(palettes, seed) }
            ) { quotes, query, category, bookmarkedIdsString, paletteAndSeed ->

                val palettes = paletteAndSeed.first
                val seed = paletteAndSeed.second

                val bookmarkedIds = bookmarkedIdsString.mapNotNull { it.toIntOrNull() }.toSet()

                val calendar = Calendar.getInstance()
                val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                // 1. Filter the quotes based on search and category
                val filteredQuotes = quotes.filter {
                    (it.quote.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)) &&
                            (category == null || it.category == category)
                }

                // 2. Sort normally OR Randomly based on the seed
                val finalQuotes = if (seed == 0L) {
                    filteredQuotes.sortedWith(
                        compareBy(
                            { it.number < currentDayOfYear },
                            { it.number }
                        )
                    )
                } else {
                    // Shuffles the list randomly using the unique seed
                    filteredQuotes.shuffled(kotlin.random.Random(seed))
                }

                val paletteIndex = themeManager.getActivePaletteIndex(currentDayOfYear)
                val palette = palettes.getOrElse(paletteIndex) { themeManager.defaultPalettes[0] }

                val categories = quotes.map { it.category }.distinct()

                GalleryUiState.Success(
                    quotes = finalQuotes, // Pass the final sorted/shuffled list
                    palette = palette,
                    categories = categories,
                    bookmarkedIds = bookmarkedIds
                )
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

    fun toggleBookmark(quote: Quote) {
        val currentState = _uiState.value
        if (currentState is GalleryUiState.Success) {
            val currentBookmarks = currentState.bookmarkedIds.toMutableSet()

            if (currentBookmarks.contains(quote.number)) {
                currentBookmarks.remove(quote.number)
            } else {
                currentBookmarks.add(quote.number)
            }

            viewModelScope.launch {
                val stringSet = currentBookmarks.map { it.toString() }.toSet()
                userPreferencesRepository.saveBookmarks(stringSet)
            }
        }
    }

    // NEW: Function to trigger randomization
    fun randomizeQuotes() {
        _randomSeed.value = System.currentTimeMillis()
    }
}

sealed class GalleryUiState {
    object Loading : GalleryUiState()
    data class Success(
        val quotes: List<Quote>,
        val palette: ColorPalette,
        val categories: List<String>,
        val bookmarkedIds: Set<Int>
    ) : GalleryUiState()
}