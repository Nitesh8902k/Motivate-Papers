# Project Plan

Motivate Papers App: A playful, pastel, and glassmorphic offline Android application that generates daily motivational wallpapers. It features a deterministic rotation engine, an in-app quote gallery, and a daily background sync using WorkManager. Build following the user's specific 7-step incremental plan.

## Project Brief

# Motivate Papers App - Project Brief (User's 7-Step Plan)

Motivate Papers is a playful and visually immersive offline Android application designed to provide daily inspiration. The app follows a strictly glassmorphic and pastel aesthetic, leveraging a deterministic engine to refresh the user's device background with custom-generated wallpapers and motivational quotes.

## Detailed Steps

### Step 1: Data Layer & App Theme
1. Create a `Quote` data class (number: Int, quote: String, category: String).
2. Create a `QuoteRepository` that parses a `quotes.json` file from the `assets` folder using `kotlinx.serialization`. Include a function `getQuoteForDay(dayOfYear: Int)` that handles leap years safely.
3. Update `Theme.kt` and `Color.kt` to define a global "playful pastel" Material 3 theme. The app's main background should be a very light cream/pastel color, not stark white or dark grey.

### Step 2: Core Logic & Preferences DataStore
1. Create a `ColorPalette` data class holding 4 hex colors (Strings).
2. Create a `ThemeManager` class. It must initialize an array of 15 beautiful, distinct pastel `ColorPalette`s.
3. Implement AndroidX Preferences DataStore. We need to save and load this array of 15 palettes. If empty, load the defaults.
4. Create `getActivePaletteIndex(dayOfYear: Int): Int` (returns dayOfYear % 15).
5. Create `overridePalette(index: Int, newPalette: ColorPalette)` that overwrites a specific palette in the DataStore so user changes are permanent.
Provide the code using Coroutines and StateFlow.

### Step 3: The Wallpaper Composable
Create a Composable named `WallpaperCanvas(quote: Quote, palette: ColorPalette)`.
1. Base: Full-screen vertical linear gradient using the 4 colors from `palette`. Add a subtle dotted texture pattern over it (low opacity).
2. Outer Capsule: Centered Box, pill shape (40% rounded corners), 10% opacity white background, thin white/gold border, and a glassmorphism blur effect (`Modifier.blur` or `RenderEffect`).
3. Inner Capsule: Smaller pill inside the outer one. Contains a highly blurred (`filter: blur(40px)` equivalent), vibrant radial gradient.
4. Top Text: "MINDFUL FLOW" and current Day/Date (e.g., "TUES | OCT 26").
5. Center Text: The `quote.quote`. Write a Kotlin helper function to calculate a text color that is a slightly darker, analogous shade of the background palette to ensure contrast.
6. Bottom Element: A row of 4 small circles displaying the colors from the `palette`.

### Step 4: Compose to Bitmap & WallpaperManager
1. Write a Compose utility (using `GraphicsLayer` or a Compose-to-Bitmap capture method) to render the `WallpaperCanvas` Composable into a high-quality Bitmap off-screen.
2. Create a `WallpaperHelper` class. Use the Android `WallpaperManager` to apply this Bitmap to both the Home and Lock screens.
3. Add permission handling for `SET_WALLPAPER`. 
4. Wrap the operation in a try-catch on `Dispatchers.IO` so it doesn't freeze the UI.

### Step 5: App Screens
1. Home Screen: 
   - Display the `WallpaperCanvas` scaled down as a preview.
   - A progress bar below it: `(dayOfYear / 365) * 100` with text "Year Passed: XX%".
   - Three buttons styled as glassmorphic pills: "Generate Next" (temporarily increments preview day), "Change Glass Color" (generates a random new palette and calls `overridePalette`), and "Set as Wallpaper" (triggers Step 4).
2. Gallery Screen:
   - A `LazyColumn` of all 365 quotes. Render them in simple, fixed-gradient glassmorphic cards (do not run dynamic color logic here to save memory).
   - Top sticky search bar to filter by text.
   - A horizontal scrolling row of Material 3 Filter Chips mapped to the `category` property.

### Step 6: The NavHost and Bottom Navigation
1. Set up Compose Navigation (`NavHost`).
2. Create a `Scaffold` with a Bottom Navigation Bar containing "Home" and "Gallery" tabs.
3. Ensure the Bottom Navigation bar fits the playful pastel aesthetic (e.g., translucent background, rounded indicators).
4. Wire up the screens from Step 5 into this `Scaffold`.

### Step 7: Daily Background Sync
1. Create a `DailyWallpaperWorker` (CoroutineWorker) using Android WorkManager.
2. In `doWork()`, calculate the current `dayOfYear`. Fetch the corresponding `Quote` and `ColorPalette`.
3. Render the `WallpaperCanvas` to a Bitmap off-screen and use `WallpaperManager` to apply it.
4. Create a setup function to enqueue this as a `PeriodicWorkRequest` running once every 24 hours (target midnight). Ensure constraints are set appropriately for Android 15 background limits.

## Implementation Steps

### Task_1_DataAndRotationLogic: Set up the data layer and daily rotation logic. This includes creating the 365-quote JSON in assets, defining 15 pastel palettes, implementing Moshi parsing, and configuring WorkManager to rotate palettes based on the day of the year using DataStore for state.
- **Status:** COMPLETED
- **Updates:** Implemented Task_1_DataAndRotationLogic. Created 365-quote JSON, defined 15 pastel palettes, set up Moshi parsing, Preferences DataStore for state, and WorkManager for daily rotation.
- **Acceptance Criteria:**
  - quotes.json exists in assets
  - Moshi models and parsing logic implemented
  - DataStore manages app state
  - WorkManager correctly calculates and stores the daily palette

### Task_2_ThemeAndGlassmorphicUI: Develop the custom Material 3 Pastel Theme and core UI components. Create the signature dotted-texture gradient background and implement glassmorphic components (capsules) using translucency and Modifier.blur.
- **Status:** COMPLETED
- **Updates:** Developed the custom Material 3 Pastel Theme and core UI components. Created the signature dotted-texture gradient background and implemented glassmorphic components (capsules) using translucency and Modifier.blur.
- **Acceptance Criteria:**
  - Fixed Material 3 Pastel Theme implemented
  - Background with dotted texture and gradient is visible
  - Glassmorphic capsule components created and reusable

### Task_3_GalleryAndHomeScreens: Build the Home screen and the Interactive Quote Gallery. The Home screen displays the daily quote, and the Gallery features a LazyColumn with all 365 quotes and a fast offline search/filter tool.
- **Status:** COMPLETED
- **Updates:** Implemented Task_3_GalleryAndHomeScreens. Built the Home screen displaying the daily quote and a Gallery screen with a LazyColumn for all 365 quotes. Added a glassmorphic search bar for filtering. Set up Navigation Compose for screen transitions. Created HomeViewModel and GalleryViewModel. Also initialized WorkManager and created an adaptive app icon.
- **Acceptance Criteria:**
  - Home screen displays correct daily quote
  - Gallery screen lists all quotes with search functionality
  - Smooth navigation between Home and Gallery

### Task_4_FinalPolishAndRun: Finalize the app with an adaptive icon, full edge-to-edge display, and stability checks. Run the app to verify the deterministic rotation and visual fidelity.
- **Status:** COMPLETED
- **Updates:** Completed Task_4_FinalPolishAndRun. Implemented adaptive app icon, enabled full edge-to-edge display with proper inset handling, and verified stability through builds. The UI now strictly follows the playful, pastel, and glassmorphic aesthetic across all screens.
- **Acceptance Criteria:**
  - Adaptive app icon implemented
  - Edge-to-edge display active
  - App builds and runs without crashes
  - UI aligns with playful, pastel, and glassmorphic aesthetic
  - Verify all existing tests pass

### Task_5_WallpaperIntegration: Implement the WallpaperCanvas rendering logic using GraphicsLayer to capture the Compose UI as a Bitmap. Integrate the WallpaperManager to apply the generated bitmap to the system and lock screens, and add interactive buttons to the Home screen for manual wallpaper setting and palette randomization.
- **Status:** COMPLETED
- **Updates:** Implemented Task_5_WallpaperIntegration. strictly following the 7-step plan and project requirements. Updated Data Layer to use kotlinx.serialization, implemented ThemeManager with 15 palettes and DataStore persistence, created the WallpaperCanvas Composable with glassmorphism and dotted texture, and integrated WallpaperManager to set the generated bitmap as wallpaper. Also updated Home Screen with preview and interactive buttons.
- **Acceptance Criteria:**
  - WallpaperCanvas captured as high-quality Bitmap
  - WallpaperManager successfully sets Home and Lock screen backgrounds
  - Set as Wallpaper button triggers the update
  - Change Glass Color button updates and saves the current palette

### Task_6_BackgroundSyncAndVerify: Develop the DailyWallpaperWorker using WorkManager to automate the wallpaper update process every 24 hours. Instruct the critic_agent to perform a final Run and Verify to confirm application stability, alignment with the glassmorphic aesthetic, and ensure no crashes occur.
- **Status:** COMPLETED
- **Updates:** Background sync logic via WorkManager implemented. WorkManager enqueues a periodic request to update the wallpaper every 24 hours.
- **Acceptance Criteria:**
  - DailyWallpaperWorker enqueued for periodic updates
  - Deterministic rotation engine correctly selects quote and palette
  - App builds pass and no crashes occur during rotation or setting
  - Verify all existing tests pass

### Task_7_FinalValidationAndHandoff: Perform a final, comprehensive validation of the Motivate Papers App against the user's 7-step incremental plan. Verify the deterministic rotation logic (365 quotes/15 palettes), high-quality wallpaper rendering and capture, manual setting functionality, and reliable background sync. Instruct the critic_agent to confirm final stability and alignment with the playful, pastel, glassmorphic aesthetic.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - App builds and runs without crashes
  - All 7 steps from the project brief are fully functional
  - Wallpaper rotation is deterministic based on dayOfYear
  - UI strictly follows the glassmorphic and pastel design constraints
  - Make sure all existing tests pass
  - Build pass
- **StartTime:** 2026-04-21 23:05:48 IST

