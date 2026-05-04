package com.aiapps.motivatepapersapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiapps.motivatepapersapp.R
import com.aiapps.motivatepapersapp.data.model.Quote
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBookmarks: () -> Unit // NEW navigation callback
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState() // Tracks the scroll position
    val coroutineScope = rememberCoroutineScope() // Needed for smooth scrolling

    Box(modifier = Modifier.fillMaxSize()) {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                ) {
                    // 1. Top Row: Back Button + Search Bar + Bookmarks Button
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

                        // Search Bar (Filter icon removed)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp) // Slightly less tall to match icons
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("Search quotes...", color = Color.DarkGray, fontSize = 14.sp)
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

                        Spacer(modifier = Modifier.width(12.dp))

                        // NEW: Bookmarks Directory Button
                        IconButton(
                            onClick = onNavigateToBookmarks,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = "Saved Bookmarks",
                                tint = Color(0xFFD4AF37), // Gold accent
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // 2. Category Chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        val categoryList = listOf(
                            "Work" to Icons.Rounded.WorkOutline,
                            "Career" to Icons.Rounded.TrendingUp,
                            "Health" to Icons.Rounded.FavoriteBorder,
                            "Relationships" to Icons.Rounded.PeopleOutline,
                            "Life" to Icons.Rounded.WbSunny,
                            "Travel" to Icons.Rounded.FlightTakeoff
                        )

                        items(categoryList) { (category, icon) ->
                            val isSelected = category == selectedCategory

                            val bgColor = if (isSelected) Color(0xFF3B3B40) else Color.White.copy(alpha = 0.4f)
                            val contentColor = if (isSelected) Color.White else Color.DarkGray
                            val iconTint = if (isSelected) Color(0xFFD4AF37) else Color.DarkGray
                            val borderColor = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.6f)

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                    .clickable {
                                        // If it's already selected, deselect it. Otherwise, select the new one.
                                        selectedCategory = if (selectedCategory == category) null else category
                                        // Tell the ViewModel to filter the quotes based on the new selection
                                        viewModel.onCategorySelect(selectedCategory)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = contentColor
                                )
                            }
                        }
                    }

                    // 3. Quotes List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp, start = 24.dp, end = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(state.quotes) { quote ->
                            val isBookmarked = state.bookmarkedIds.contains(quote.number)
                            QuoteDarkGlassCard(
                                quote = quote,
                                isBookmarked = isBookmarked,
                                onBookmarkClick = { viewModel.toggleBookmark(quote) }
                            )
                        }
                    }
                }

                // 4. Floating "Surprise Me" Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .systemBarsPadding()
                        .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
                ) {
                    ActionGlassPill(
                        title = "Surprise Me",
                        subtitle = "Randomise the quotes",
                        leftIcon = Icons.Rounded.CardGiftcard,
                        rightIcon = Icons.Rounded.Refresh, // Swapped to Refresh icon to match the action
                        onClick = {
                            viewModel.randomizeQuotes() // Shuffle the list
                            coroutineScope.launch {
                                listState.animateScrollToItem(0) // Smoothly scroll to top
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuoteDarkGlassCard(
    quote: Quote,
    isBookmarked: Boolean, // NEW parameter
    onBookmarkClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF6B6E7A).copy(alpha = 0.9f),
                        Color(0xFF3B3A40).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(28.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(150.dp)
                .offset(x = 40.dp, y = 40.dp)
                .clip(CircleShape)
                .border(2.dp, Color(0xFFD4AF37).copy(alpha = 0.2f), CircleShape)
        )

        Column(modifier = Modifier.padding(24.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = quote.category.uppercase(),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Bookmark Button with Dynamic Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onBookmarkClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color(0xFFD4AF37) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "“",
                fontSize = 64.sp,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF8B90A0),
                modifier = Modifier.height(40.dp)
            )

            Text(
                text = quote.quote,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 28.sp,
                    fontSize = 20.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "”",
                fontSize = 64.sp,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF8B90A0),
                modifier = Modifier
                    .align(Alignment.End)
                    .height(50.dp)
                    .offset(y = (-16).dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color(0xFF908CBA))
                )
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
fun ActionGlassPill(
    title: String,
    subtitle: String,
    leftIcon: ImageVector,
    rightIcon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.4f))
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon Box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2C2C30)), // Dark box
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37), // Gold
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Texts
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.DarkGray,
                        lineHeight = 16.sp
                    )
                )
            }

            // Right Chevron
            Icon(
                imageVector = rightIcon,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}