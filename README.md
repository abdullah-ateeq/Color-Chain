# Color Chain 🎨🔗

> *"Match the color. Build the chain."*

**Color Chain** is a fast-paced, offline Android puzzle/arcade game built completely with modern **Android Jetpack Compose** and **Kotlin**. Challenge your visual recognition, reflexes, and color matching under time pressure to construct massive chain multipliers and set top records!

---

## 🌟 Key Features

* 📱 **Modern Jetpack Compose UI**: Built 100% with declarative Jetpack Compose, Material 3 components, smooth animations, and custom canvas overlays.
* ⚡ **Fast & Responsive Gameplay**: Real-time tile grid generation, instant user interactions, and particle burst effects on correct color matches.
* 📈 **Progressive Level Scaling**: Dynamic grid dimensions expanding from **3×3** up to **6×6** with increasing color palettes (up to 9 distinct game colors).
* 🎯 **3 Difficulty Modes**:
  * **Easy**: Larger tile grids, generous time limits, and gradual color rollouts.
  * **Medium**: Faster pace, balanced time bonuses, and expanded grid sizes.
  * **Hard**: Rapid timer decay, complex 6×6 grids, and full color palettes.
* 🔊 **Procedural Synthesized Audio**: Built-in zero-asset `AudioTrack` sound engine generating real-time PCM sound effects with pitch scaling as your combo chain grows.
* 📳 **Custom Haptic Engine**: Tactile vibration feedback for taps, wrong selections, combo milestones, and level completion.
* ♿ **Colorblind Accessibility Mode**: Optional visual symbol overlays on color tiles ensuring high-contrast clarity for all users.
* 📊 **Persistent Statistics & Records**: Track best scores, max combo chains, highest level reached, overall accuracy percentage, and total games played.
* 🌓 **Dynamic Theme Support**: Supports System Default, Light, and Dark themes with edge-to-edge layout design.
* 🔒 **100% Offline & Private**: Zero network permissions required, no telemetry, no ads, and no external tracking.

---

## 🎮 How to Play

1. **Observe the Target Color**: Check the top target swatch indicating the required color for the turn.
2. **Tap the Matching Tile**: Locate and select the tile on the grid that matches the target color.
3. **Extend Your Chain**: Every correct tap increments your chain combo and grants extra timer bonus.
4. **Mind the Timer**: Match before the progress timer runs out to stay in the game!
5. **Avoid Wrong Taps**: Tapping an incorrect tile ends the run immediately.

---

## 🛠️ Architecture & Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/) (2.2.10)
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (`androidx.compose.material3`)
* **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) (`androidx.navigation.compose`)
* **Architecture Pattern**: MVVM (Model-View-ViewModel) with Reactive State Flow (`StateFlow` & Kotlin Coroutines)
* **Persistence**: Local `SharedPreferences` repository for user statistics and game preferences
* **Audio**: Low-latency procedural sound synthesis using Android native `AudioTrack`
* **Haptics**: Android `Vibrator` / `VibratorManager` API
* **Build System**: Gradle with Kotlin DSL & Version Catalog (`gradle/libs.versions.toml`)
* **Compatibility**: Target SDK 37 • Minimum SDK 26 (Android 8.0+)

---

## 📁 Project Structure

```text
com.example.colorchain/
├── data/
│   ├── audio/          # SoundEffectManager (AudioTrack procedural audio engine)
│   ├── haptics/        # HapticsManager (Vibration patterns & feedback)
│   ├── logic/          # GameEngine (Board generation, score calculation, difficulty levels)
│   ├── model/          # Data models (ColorTile, GameColor, Difficulty, Level config)
│   └── storage/        # GamePreferencesRepository (Local stats & settings)
├── ui/
│   ├── components/     # ParticleCanvas, ChainConnectionOverlay, Dialogs, TargetSwatch
│   ├── navigation/     # NavHost & Screen definitions
│   ├── screens/        # HomeScreen, GameScreen, ResultScreen, SettingsScreen, AboutScreen, SplashScreen
│   ├── theme/          # Material 3 Theme, Color Palette, Typography
│   └── viewmodel/      # ColorChainViewModel (Game state management & timers)
└── MainActivity.kt     # Main entry point with Edge-To-Edge enabled
```

---

## 🚀 Getting Started

### Prerequisites
* Android Studio Ladybug (or newer)
* JDK 11 or higher
* Android SDK 26+ device or emulator

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Color-Chain.git
   cd Color-Chain
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run on an Android device or emulator:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
