package com.aiapps.motivatepapersapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiapps.motivatepapersapp.ui.components.DottedBackground
import com.aiapps.motivatepapersapp.ui.components.WallpaperCanvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.aiapps.motivatepapersapp.R

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToGallery: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD4AF37))
                }
            }
            /*is HomeUiState.Success -> {
                // Wrap the Success UI in a Box that draws the image background using the paint modifier
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .paint(
                            painter = painterResource(id = R.drawable.app_background),
                            contentScale = ContentScale.Crop
                        )
                ) {
                   */
                    is HomeUiState.Success -> {
                    // Wrap the Success UI in a Box with the specific solid color
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFAF5F1)) // Hex color #FAF5F1
                    ) {
                    // 3. Apply the Dotted Background over the gradient
                    DottedBackground(
                        modifier = Modifier.fillMaxSize(),
                        dotColor = Color.Gray.copy(alpha = 0.15f)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(0.dp))

                        // 1. Generate Next Button
                        ActionGlassButton(
                            title = "Generate Next",
                            subtitle = "New quote & glass pairing",
                            icon = Icons.Rounded.Refresh,
                            onClick = viewModel::generateNext,
                            glassColor = Color(0xFFE2ECE9).copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Change Glass Color Button
                        ActionGlassButton(
                            title = "Change Glass Color",
                            subtitle = "change the color scheme of the wallpaper",
                            icon = Icons.Rounded.ColorLens,
                            onClick = viewModel::changeGlassColor,
                            glassColor = Color(0xFFE2ECE9).copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.weight(0.5f))

                        @Composable
                        fun FontPickerRow(
                            selectedFont: FontOption,
                            onFontSelected: (FontOption) -> Unit
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                                Text(
                                    text = "Choose Font",
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(availableFonts) { fontOption ->
                                        val isSelected = fontOption == selectedFont
                                        val fontFamily = FontFamily(Font(fontOption.fontResId)) // Compose loads the font here!

                                        Card(
                                            modifier = Modifier
                                                .width(120.dp)
                                                .height(80.dp)
                                                .clickable { onFontSelected(fontOption) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) Color.Black.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                // The Preview Quote
                                                Text(
                                                    text = "Focus", // A common preview text, or you can use "Focus"
                                                    fontFamily = fontFamily, // Applies the actual font!
                                                    fontSize = 24.sp,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                // The Font Name
                                                Text(
                                                    text = fontOption.name,
                                                    fontSize = 12.sp,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Wallpaper Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .aspectRatio(0.6f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.White)
                                .border(2.dp, Color.White, RoundedCornerShape(32.dp))
                                .scale(1f)
                        ) {
                            WallpaperCanvas(
                                quote = state.quote,
                                palette = state.palette,
                                dayOfYear = state.dayOfYear,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.weight(0.5f))

                        val selectedFont by viewModel.selectedFont.collectAsState()

                        FontPickerRow(
                            selectedFont = selectedFont,
                            onFontSelected = { font -> viewModel.selectFont(font) }
                        )

                        // 4. Set as wallpaper Button
                        ActionGlassButton(
                            title = "Set as wallpaper",
                            subtitle = "Apply current design",
                            icon = Icons.Rounded.DoneAll,
                            onClick = viewModel::setAsWallpaper,
                            glassColor = Color(0xFFE2ECE9).copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 5. Year Passed Progress (Design specific placement)
                        val progress = state.dayOfYear / 365f
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.05f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFD4AF37), Color(0xFFD4AF37).copy(alpha = 0.3f))
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Year Passed: ${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // Bottom Navigation Mockup (Pills)
                    // Note: Actual logic handled in NavHost/Scaffold in Step 6
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // Aligns the box to the bottom right
                            .padding(bottom = 32.dp, end = 24.dp)
                    ) {
                        NavPill(
                            text = "GALLERY",
                            icon = Icons.Rounded.GridView,
                            isActive = false,
                            onClick = onNavigateToGallery,
                            modifier = Modifier.width(80.dp) // Gives the single button a clean, fixed width
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionGlassButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    glassColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(42.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.2f))),
                shape = RoundedCornerShape(42.dp)
            )
    ) {
        // Blur background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(glassColor)
                .blur(20.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NavPill(
    text: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) Color(0xFF1B261E).copy(alpha = 0.7f) else Color(0x00000000).copy(alpha = 0.7f)
    val contentColor = if (isActive) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = contentColor
                )
            )
        }
    }
}