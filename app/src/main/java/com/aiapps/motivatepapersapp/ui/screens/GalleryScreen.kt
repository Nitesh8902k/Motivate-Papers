package com.aiapps.motivatepapersapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiapps.motivatepapersapp.ui.components.DottedBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is GalleryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is GalleryUiState.Success -> {
                val primaryColor = Color(android.graphics.Color.parseColor(state.palette.color1))
                
                // Light grey/cream base as per step 1
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)))
                
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1B261E).copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        Icons.Rounded.ArrowBackIosNew,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // 1. Search Bar (Exact mockup look)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE9ECEF))
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = { 
                                    Text("Search quotes...", color = Color.Gray) 
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }

                        // 2. Category Chips
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            items(state.categories) { category ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.onCategorySelect(category) },
                                    label = { Text(category, fontSize = 12.sp) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.White,
                                        labelColor = Color.Black.copy(alpha = 0.7f)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = Color.Transparent,
                                        enabled = true,
                                        selected = false
                                    )
                                )
                            }
                        }

                        // 3. Quotes List (Gradient Glass Cards)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(state.quotes) { quote ->
                                QuoteGlassCard(
                                    quote = quote,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteGlassCard(
    quote: com.aiapps.motivatepapersapp.data.model.Quote,
    primaryColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(40.dp))
            .background(
                Brush.linearGradient(
                    listOf(primaryColor.copy(alpha = 0.2f), Color(0xFFB2E2F2).copy(alpha = 0.2f))
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.5f),
                RoundedCornerShape(40.dp)
            )
            .padding(32.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = quote.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wrap the quotes and text in a Box so they perfectly layer/overlap
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Big opening quotation mark (Background layer)
                Text(
                    text = "“",
                    fontSize = 120.sp,
                    fontFamily = FontFamily.Serif,
                    color = primaryColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-8).dp, y = (-24).dp)
                )

                // Actual Quote (Foreground layer)
                Text(
                    text = quote.quote,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    ),
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 16.dp, vertical = 12.dp) // Generous padding to give the text breathing room
                )

                // Big closing quotation mark (Background layer)
                Text(
                    text = "”",
                    fontSize = 120.sp,
                    fontFamily = FontFamily.Serif,
                    color = primaryColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 74.dp)
                )
            }
        }
    }
}
