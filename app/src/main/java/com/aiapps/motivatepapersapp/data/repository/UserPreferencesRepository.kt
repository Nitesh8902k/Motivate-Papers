package com.aiapps.motivatepapersapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    // 1. Define the key to store our set of bookmark IDs (stored as Strings)
    private val BOOKMARKED_QUOTES_KEY = stringSetPreferencesKey("bookmarked_quotes")

    // 2. Read the bookmarks (Returns a Flow so the UI updates automatically when it changes)
    val bookmarkedQuotesFlow: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[BOOKMARKED_QUOTES_KEY] ?: emptySet()
        }

    // 3. Save the bookmarks
    suspend fun saveBookmarks(bookmarks: Set<String>) {
        dataStore.edit { preferences ->
            preferences[BOOKMARKED_QUOTES_KEY] = bookmarks
        }
    }
}