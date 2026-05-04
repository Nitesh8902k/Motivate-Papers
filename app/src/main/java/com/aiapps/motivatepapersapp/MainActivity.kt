package com.aiapps.motivatepapersapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aiapps.motivatepapersapp.ui.screens.BookmarksScreen
import com.aiapps.motivatepapersapp.ui.screens.GalleryScreen
import com.aiapps.motivatepapersapp.ui.screens.GalleryViewModel
import com.aiapps.motivatepapersapp.ui.screens.HomeScreen
import com.aiapps.motivatepapersapp.ui.screens.HomeViewModel
import com.aiapps.motivatepapersapp.ui.theme.MotivatePapersAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appModule = (application as MotivatePapersApplication).appModule

        setContent {
            MotivatePapersAppTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                // 1. Initialize ViewModels HERE, outside the NavHost.
                // Because they are called directly inside setContent, they are scoped
                // to the MainActivity and will survive navigation between screens!

                val sharedHomeViewModel: HomeViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HomeViewModel(
                                appModule.quoteRepository,
                                appModule.themeManager,
                                appModule.wallpaperHelper,
                                context.applicationContext
                            ) as T
                        }
                    }
                )

                val sharedGalleryViewModel: GalleryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return GalleryViewModel(
                                appModule.quoteRepository,
                                appModule.themeManager,
                                appModule.userPreferencesRepository // Make sure you pass this here!
                            ) as T
                        }
                    }
                )

                // Permission Request Logic
                val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    emptyArray()
                }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { /* Handle results if necessary */ }

                LaunchedEffect(Unit) {
                    if (permissionsToRequest.isNotEmpty()) {
                        launcher.launch(permissionsToRequest)
                    }
                }

                Scaffold(
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            // 2. Pass the shared instance
                            HomeScreen(
                                viewModel = sharedHomeViewModel,
                                onNavigateToGallery = { navController.navigate("gallery") }
                            )
                        }

                        composable("gallery") {
                            // 3. Pass the shared instance
                            GalleryScreen(
                                viewModel = sharedGalleryViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToBookmarks = { navController.navigate("bookmarks") }
                            )
                        }

                        composable("bookmarks") {
                            // 4. Pass the EXACT SAME shared instance here
                            BookmarksScreen(
                                viewModel = sharedGalleryViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}