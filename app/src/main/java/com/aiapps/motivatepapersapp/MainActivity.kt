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
                            val viewModel: HomeViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return HomeViewModel(
                                            appModule.quoteRepository, 
                                            appModule.themeManager, 
                                            appModule.wallpaperHelper
                                        ) as T
                                    }
                                }
                            )
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToGallery = { navController.navigate("gallery") }
                            )
                        }
                        composable("gallery") {
                            val viewModel: GalleryViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        return GalleryViewModel(
                                            appModule.quoteRepository, 
                                            appModule.themeManager
                                        ) as T
                                    }
                                }
                            )
                            GalleryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
