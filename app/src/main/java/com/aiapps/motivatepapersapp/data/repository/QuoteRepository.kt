package com.aiapps.motivatepapersapp.data.repository

import android.content.Context
import com.aiapps.motivatepapersapp.data.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class QuoteRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getAllQuotes(): List<Quote> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<Quote>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getQuoteForDay(dayOfYear: Int): Quote {
        val quotes = getAllQuotes()
        if (quotes.isEmpty()) return Quote(0, "Keep moving!", "Mindfulness")
        
        // Handling leap years safely by using modulo with actual list size if 365 is not exact
        val index = (dayOfYear - 1) % quotes.size
        return quotes[index]
    }
}
