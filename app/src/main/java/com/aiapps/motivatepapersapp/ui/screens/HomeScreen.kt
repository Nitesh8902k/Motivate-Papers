package com.aiapps.motivatepapersapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiapps.motivatepapersapp.R
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import com.aiapps.motivatepapersapp.ui.components.WallpaperCanvas

enum class WallpaperDesignOption {
    GRADIENT_GLASS,
    STATIC_MINIMAL
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToGallery: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Theme State
    var isDarkTheme by remember { mutableStateOf(true) }

    // Derived Colors based on Theme
    val textColor = if (isDarkTheme) Color.White else Color(0xFF2D2D2D)
    val subTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    val glassBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.3f)
    val glassBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.5f)
    val accentColor = Color(0xFFD4AF37) // Gold

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = if (isDarkTheme) R.drawable.home_night else R.drawable.home_day),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            }

            is HomeUiState.Success -> {
                var selectedDesign by remember { mutableStateOf(WallpaperDesignOption.GRADIENT_GLASS) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    // Header Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hello",
                            fontSize = 48.sp,
                            fontFamily = FontFamily.Serif,
                            color = textColor
                        )
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = accentColor,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { isDarkTheme = !isDarkTheme }
                        )
                    }

                    Text(
                        text = if (isDarkTheme) "You're one decision away from\na better day." else "Small steps, big changes.\nYou've got this.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = subTextColor,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // 1. Generate Next Button
                    ActionGlassButton(
                        title = "Generate Next",
                        subtitle = "New quote & glass pairing",
                        leftIcon = Icons.Rounded.AutoAwesome,
                        rightIcon = Icons.Rounded.Refresh,
                        onClick = viewModel::generateNext,
                        glassBgColor = glassBgColor,
                        glassBorderColor = glassBorderColor,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 2. Theme Selection & Previews
                    ThemeSelectionSection(
                        selectedDesign = selectedDesign,
                        onDesignSelected = { selectedDesign = it },
                        onChangeGlassColor = viewModel::changeGlassColor,
                        quote = state.quote,
                        palette = state.palette,
                        dayOfYear = state.dayOfYear,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        accentColor = accentColor,
                        glassBgColor = glassBgColor,
                        glassBorderColor = glassBorderColor
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val selectedFont by viewModel.selectedFont.collectAsState()

                    // 3. Font Picker & Gallery
                    FontPickerRow(
                        selectedFont = selectedFont,
                        onFontSelected = { font -> viewModel.selectFont(font) },
                        textColor = textColor,
                        glassBgColor = glassBgColor,
                        glassBorderColor = glassBorderColor,
                        accentColor = accentColor
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. Set as wallpaper Button
                    ActionGlassButton(
                        title = "Set as wallpaper",
                        subtitle = "Apply current design",
                        leftIcon = Icons.Rounded.AutoAwesome,
                        rightIcon = Icons.Rounded.ChevronRight,
                        onClick = { viewModel.setAsWallpaper(selectedDesign) },
                        glassBgColor = glassBgColor,
                        glassBorderColor = glassBorderColor,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
                // FLOATING GALLERY BUTTON
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .systemBarsPadding() // Keeps it above the bottom navigation bar
                        .padding(end = 24.dp, bottom = 60.dp) // Hovers just above the "Set as wallpaper" button
                        .size(width = 84.dp, height = 84.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFD4AF37), Color(0xFF9E8022)) // Golden gradient
                            )
                        )
                        .clickable { onNavigateToGallery() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.GridView,
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "GALLERY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelectionSection(
    selectedDesign: WallpaperDesignOption,
    onDesignSelected: (WallpaperDesignOption) -> Unit,
    onChangeGlassColor: () -> Unit,
    quote: Quote,
    palette: ColorPalette,
    dayOfYear: Int,
    textColor: Color,
    subTextColor: Color,
    accentColor: Color,
    glassBgColor: Color,
    glassBorderColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Choose Wallpaper Design",
            color = Color(0xFFD4AF37),
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Option 1: Glassmorphic Design
            ThemePreviewCard(
                title = "Glassmorphic",
                subtitle = "Modern · Bold · Clear",
                isSelected = selectedDesign == WallpaperDesignOption.GRADIENT_GLASS,
                onClick = { onDesignSelected(WallpaperDesignOption.GRADIENT_GLASS) },
                textColor = textColor,
                subTextColor = subTextColor,
                accentColor = accentColor,
                modifier = Modifier.weight(1f),
                overlayContent = {
                    // Button overlay, positioned at the TopCenter inside the preview box
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedDesign == WallpaperDesignOption.GRADIENT_GLASS,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(glassBgColor)
                                .border(1.dp, glassBorderColor, RoundedCornerShape(16.dp))
                                .clickable { onChangeGlassColor() }
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ColorLens,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Color", // Shortened slightly to fit better inside the preview
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    WallpaperCanvas(
                        quote = quote,
                        palette = palette,
                        dayOfYear = dayOfYear,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Option 2: Mindful Flow Design
            ThemePreviewCard(
                title = "Mindful Flow",
                subtitle = "Automatic Dark Mode Switch",
                isSelected = selectedDesign == WallpaperDesignOption.STATIC_MINIMAL,
                onClick = { onDesignSelected(WallpaperDesignOption.STATIC_MINIMAL) },
                textColor = textColor,
                subTextColor = subTextColor,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.day_bg),
                    contentDescription = "Mindful Flow Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
    }
}

@Composable
fun ThemePreviewCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    subTextColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    overlayContent: @Composable BoxScope.() -> Unit = {}, // Added this
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) accentColor else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // The wallpaper preview
            content()

            // The injected overlay (e.g., Change Color button)
            overlayContent()

            // The selection checkmark
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected Theme",
                    tint = accentColor,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(28.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) accentColor else textColor
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = subTextColor
        )
    }
}

@Composable
fun FontPickerRow(
    selectedFont: FontOption,
    onFontSelected: (FontOption) -> Unit,
    textColor: Color,
    glassBgColor: Color,
    glassBorderColor: Color,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Typography", // Updated to match your screenshot
            color = textColor,
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableFonts) { fontOption ->
                val isSelected = fontOption == selectedFont
                val fontFamily = FontFamily(Font(fontOption.fontResId))

                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(glassBgColor)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) accentColor else glassBorderColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onFontSelected(fontOption) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ag",
                            fontFamily = fontFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentColor else textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Focus",
                            fontFamily = fontFamily,
                            fontSize = 14.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fontOption.name,
                            fontSize = 10.sp,
                            color = textColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(18.dp)
                                .background(Color.White, CircleShape)
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
    leftIcon: ImageVector,
    rightIcon: ImageVector,
    onClick: () -> Unit,
    glassBgColor: Color,
    glassBorderColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(glassBgColor)
            .border(1.dp, glassBorderColor, RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leftIcon,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37), // Accent Gold
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
                        color = textColor
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = subTextColor,
                        lineHeight = 16.sp
                    )
                )
            }

            // Right Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = rightIcon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}