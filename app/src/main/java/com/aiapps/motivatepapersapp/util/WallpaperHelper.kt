package com.aiapps.motivatepapersapp.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.graphics.createBitmap
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aiapps.motivatepapersapp.data.model.ColorPalette
import com.aiapps.motivatepapersapp.data.model.Quote
import com.aiapps.motivatepapersapp.ui.components.WallpaperCanvas
import com.aiapps.motivatepapersapp.ui.theme.MotivatePapersAppTheme
import kotlinx.coroutines.*

class WallpaperHelper(private val context: Context) {

    private companion object {
        const val TAG = "WallpaperHelper"
    }

    /**
     * Applies the given Bitmap to both the System and Lock screens.
     */
    suspend fun setWallpaper(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        Log.i(TAG, "setWallpaper: Starting for bitmap ${bitmap.width}x${bitmap.height}")
        val wallpaperManager = WallpaperManager.getInstance(context)
        
        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Applying wallpaper...", Toast.LENGTH_SHORT).show()
            }

            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d(TAG, "Setting wallpaper for BOTH System and Lock screens")
                val resBoth = try {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                } catch (e: Exception) {
                    Log.w(TAG, "Combined set failed: ${e.message}")
                    -1
                }

                if (resBoth <= 0) {
                    Log.w(TAG, "Trying System only as fallback")
                    val resSystem = wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    resSystem > 0
                } else {
                    true
                }
            } else {
                wallpaperManager.setBitmap(bitmap)
                true
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Wallpaper Applied!", Toast.LENGTH_SHORT).show()
            }
            Log.i(TAG, "setWallpaper: Completed successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "setWallpaper: Critical failure", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to set wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }

    /**
     * Renders the WallpaperCanvas Composable into a high-quality Bitmap off-screen.
     */
    suspend fun captureWallpaperBitmap(
        quote: Quote,
        palette: ColorPalette,
        dayOfYear: Int
    ): Bitmap = withContext(Dispatchers.Main) {
        Log.i(TAG, "captureWallpaperBitmap: Start for day $dayOfYear")
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val bounds = windowManager.currentWindowMetrics.bounds
        val width = bounds.width()
        val height = bounds.height()

        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.BLACK)
        val canvas = Canvas(bitmap)

        val composeView = ComposeView(context)
        composeView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        val frameClock = BroadcastFrameClock()
        // Recomposer must run in a context that contains the MonotonicFrameClock (frameClock)
        val recomposer = Recomposer(coroutineContext + frameClock)
        val recomposerJob = launch {
            withContext(frameClock) {
                recomposer.runRecomposeAndApplyChanges()
            }
        }
        
        composeView.setParentCompositionContext(recomposer)
        
        val lifecycleOwner = object : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
            private val lifecycleRegistry = LifecycleRegistry(this)
            private val savedStateRegistryController = SavedStateRegistryController.create(this)

            override val lifecycle: Lifecycle get() = lifecycleRegistry
            override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
            override val viewModelStore: ViewModelStore = ViewModelStore()

            init {
                savedStateRegistryController.performRestore(null)
                lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
                lifecycleRegistry.currentState = Lifecycle.State.CREATED
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }

            fun clear() {
                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                viewModelStore.clear()
            }
        }

        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(lifecycleOwner)
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner))

        composeView.layoutParams = WindowManager.LayoutParams(width, height)
        composeView.visibility = View.VISIBLE

        composeView.setContent {
            MotivatePapersAppTheme(darkTheme = false) {
                // Surface helps ensure a consistent background behavior
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    WallpaperCanvas(
                        quote = quote,
                        palette = palette,
                        dayOfYear = dayOfYear,
                        modifier = Modifier.fillMaxSize(),
                        isCapturing = true
                    )
                }
            }
        }

        try {
            Log.d(TAG, "Driving capture frames...")

            composeView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            composeView.layout(0, 0, width, height)

            // Drive frames to ensure composition and layout settle
            repeat(60) {
                frameClock.sendFrame(System.nanoTime())
                Snapshot.sendApplyNotifications()
                delay(16)
                withTimeoutOrNull(200) { recomposer.awaitIdle() }
            }

            // Re-measure and layout after composition
            composeView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            composeView.layout(0, 0, width, height)

            delay(300)

            Log.d(TAG, "Drawing to canvas...")
            composeView.draw(canvas)
            
            val cp = bitmap.getPixel(width / 2, height / 2)
            Log.d(TAG, "Center pixel result: ${Integer.toHexString(cp)}")

        } catch (e: Exception) {
            Log.e(TAG, "Error during capture", e)
        } finally {
            recomposerJob.cancel()
            recomposer.cancel()
            lifecycleOwner.clear()
        }

        Log.i(TAG, "captureWallpaperBitmap: Success")
        bitmap
    }
}
