package com.aiapps.motivatepapersapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiapps.motivatepapersapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit
) {
    // We reuse GalleryViewModel since it acts as the single source of truth for quotes and bookmarks
    val uiState by viewModel.uiState.collectAsState()

    // A local search state just for the bookmarks screen
    var bookmarkSearchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.day_bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        when (val state = uiState) {
            is GalleryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD4AF37))
                }
            }
            is GalleryUiState.Success -> {
                // 1. Filter the main quote list to ONLY show bookmarked quotes
                val bookmarkedQuotes = state.quotes.filter { state.bookmarkedIds.contains(it.number) }

                // 2. Apply local search filter on top of the bookmarked quotes
                val displayQuotes = if (bookmarkSearchQuery.isNotBlank()) {
                    bookmarkedQuotes.filter {
                        it.quote.contains(bookmarkSearchQuery, ignoreCase = true) ||
                                it.category.contains(bookmarkSearchQuery, ignoreCase = true)
                    }
                } else {
                    bookmarkedQuotes
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    // Top Row: Back Button + Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF2C2C30))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Local Search Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            TextField(
                                value = bookmarkSearchQuery,
                                onValueChange = { bookmarkSearchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("Search saved...", color = Color.DarkGray, fontSize = 14.sp)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search",
                                        tint = Color.DarkGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }

                    // Title Header
                    Text(
                        text = "Your Collection",
                        fontFamily = FontFamily.Serif,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    // Empty State or List
                    if (displayQuotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (bookmarkSearchQuery.isNotBlank())
                                    "No saved quotes match your search."
                                else
                                    "Your collection is empty.\nTap the bookmark icon on any quote to save it here.",
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Quotes List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(displayQuotes) { quote ->
                                // Reusing the exact same card from GalleryScreen.kt
                                QuoteDarkGlassCard(
                                    quote = quote,
                                    isBookmarked = true, // It's always true here because we filtered for them
                                    onBookmarkClick = { viewModel.toggleBookmark(quote) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}