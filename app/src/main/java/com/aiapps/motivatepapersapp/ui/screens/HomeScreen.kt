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
            is HomeUiState.Success -> {
                // 1. Create the gradient brush from the current palette
                val backgroundBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(android.graphics.Color.parseColor(state.palette.color1)),
                        Color(android.graphics.Color.parseColor(state.palette.color2)),
                        Color(android.graphics.Color.parseColor(state.palette.color3)),
                        Color(android.graphics.Color.parseColor(state.palette.color4))
                    )
                )

                // 2. Wrap the Success UI in a Box that draws the gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
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
                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. Generate Next Button
                        ActionGlassButton(
                            title = "Generate Next",
                            subtitle = "New quote & glass pairing",
                            icon = Icons.Rounded.Refresh,
                            onClick = viewModel::generateNext,
                            glassColor = Color(0xFFB2E2F2).copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Change Glass Color Button
                        ActionGlassButton(
                            title = "Change Glass Color",
                            subtitle = "change the color scheme of the wallpaper",
                            icon = Icons.Rounded.ColorLens,
                            onClick = viewModel::changeGlassColor,
                            glassColor = Color(0xFFFAD2E1).copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.weight(0.5f))

                        // 3. Wallpaper Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .aspectRatio(0.6f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.White)
                                .border(6.dp, Color.White, RoundedCornerShape(32.dp))
                                .scale(0.95f)
                        ) {
                            WallpaperCanvas(
                                quote = state.quote,
                                palette = state.palette,
                                dayOfYear = state.dayOfYear,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.weight(0.5f))

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
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        NavPill(
                            text = "GALLERY",
                            icon = Icons.Rounded.GridView,
                            isActive = false,
                            onClick = onNavigateToGallery,
                            modifier = Modifier.weight(1f)
                        )
                        NavPill(
                            text = "HOME",
                            icon = Icons.Rounded.Home,
                            isActive = true,
                            onClick = {},
                            modifier = Modifier.weight(1f)
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
    val backgroundColor = if (isActive) Color(0xFFFAD2E1).copy(alpha = 0.4f) else Color(0xFF1B261E).copy(alpha = 0.9f)
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