package com.example.colorchain.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.colorchain.data.model.GameState
import com.example.colorchain.ui.screens.about.AboutScreen
import com.example.colorchain.ui.screens.game.GameScreen
import com.example.colorchain.ui.screens.home.HomeScreen
import com.example.colorchain.ui.screens.result.ResultScreen
import com.example.colorchain.ui.screens.settings.SettingsScreen
import com.example.colorchain.ui.screens.splash.SplashScreen
import com.example.colorchain.ui.viewmodel.ColorChainViewModel

@Composable
fun ColorChainNavHost(
    navController: NavHostController,
    viewModel: ColorChainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val targetColor by viewModel.targetColor.collectAsState()
    val tiles by viewModel.tiles.collectAsState()
    val levelConfig by viewModel.levelConfig.collectAsState()
    val currentLevel by viewModel.currentLevel.collectAsState()
    val chain by viewModel.chain.collectAsState()
    val score by viewModel.score.collectAsState()
    val remainingTimeMs by viewModel.remainingTimeMs.collectAsState()
    val levelMatches by viewModel.levelMatches.collectAsState()
    val activeMilestone by viewModel.activeMilestone.collectAsState()
    val levelCompletedEvent by viewModel.levelCompleteCelebration.collectAsState()
    val lastTapFeedback by viewModel.lastTapFeedback.collectAsState()
    val connectionAnimation by viewModel.connectionAnimation.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()

    // Automatically navigate to Result screen when Game Over happens
    LaunchedEffect(gameState) {
        if (gameState == GameState.GAME_OVER) {
            navController.navigate(Screen.Result.route) {
                popUpTo(Screen.Game.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // 1. SPLASH
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. HOME
        composable(Screen.Home.route) {
            HomeScreen(
                stats = stats,
                selectedDifficulty = difficulty,
                onDifficultySelected = { viewModel.setDifficulty(it) },
                onPlayClicked = {
                    viewModel.startNewGame(difficulty)
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClicked = { navController.navigate(Screen.Settings.route) },
                onAboutClicked = { navController.navigate(Screen.About.route) }
            )
        }

        // 3. GAME
        composable(Screen.Game.route) {
            GameScreen(
                gameState = gameState,
                targetColor = targetColor,
                tiles = tiles,
                levelConfig = levelConfig,
                currentLevel = currentLevel,
                chain = chain,
                score = score,
                remainingTimeMs = remainingTimeMs,
                levelMatches = levelMatches,
                colorAssistEnabled = settings.colorAssistEnabled,
                activeMilestone = activeMilestone,
                levelCompletedEvent = levelCompletedEvent,
                lastTapFeedback = lastTapFeedback,
                connectionAnimation = connectionAnimation,
                onTileTapped = { viewModel.onTileTapped(it) },
                onPauseClicked = { viewModel.pauseGame() },
                onResumeClicked = { viewModel.resumeGame() },
                onRestartConfirmed = { viewModel.restartGame() },
                onExitConfirmed = {
                    viewModel.exitGame()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. RESULT
        composable(Screen.Result.route) {
            ResultScreen(
                result = lastResult,
                stats = stats,
                onPlayAgainClicked = {
                    viewModel.startNewGame(difficulty)
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onHomeClicked = {
                    viewModel.exitGame()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. SETTINGS
        composable(Screen.Settings.route) {
            SettingsScreen(
                settings = settings,
                onThemeSelected = { viewModel.setThemeMode(it) },
                onSoundToggled = { viewModel.toggleSound(it) },
                onHapticsToggled = { viewModel.toggleHaptics(it) },
                onColorAssistToggled = { viewModel.toggleColorAssist(it) },
                onResetStatsConfirmed = { viewModel.resetStatistics() },
                onBackClicked = { navController.popBackStack() }
            )
        }

        // 6. ABOUT
        composable(Screen.About.route) {
            AboutScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
