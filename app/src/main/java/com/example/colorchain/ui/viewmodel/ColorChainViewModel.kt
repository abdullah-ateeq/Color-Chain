package com.example.colorchain.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorchain.data.audio.SoundEffectManager
import com.example.colorchain.data.haptics.HapticsManager
import com.example.colorchain.data.logic.GameEngine
import com.example.colorchain.data.model.ColorChainLevel
import com.example.colorchain.data.model.ColorChainResult
import com.example.colorchain.data.model.ColorTile
import com.example.colorchain.data.model.Difficulty
import com.example.colorchain.data.model.GameColor
import com.example.colorchain.data.model.GameOverReason
import com.example.colorchain.data.model.GameState
import com.example.colorchain.data.model.Milestone
import com.example.colorchain.data.storage.AppThemeMode
import com.example.colorchain.data.storage.GamePreferencesRepository
import com.example.colorchain.data.storage.UserGameSettings
import com.example.colorchain.data.storage.UserGameStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ConnectionAnimationData(
    val tileIndex: Int,
    val color: GameColor,
    val timestamp: Long = System.currentTimeMillis()
)

data class TapFeedbackData(
    val tileId: Int,
    val isCorrect: Boolean,
    val pointsAdded: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

class ColorChainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application)
    val soundManager = SoundEffectManager()
    val hapticsManager = HapticsManager(application)

    val stats: StateFlow<UserGameStats> = repository.statsFlow
    val settings: StateFlow<UserGameSettings> = repository.settingsFlow

    // Game state
    private val _gameState = MutableStateFlow(GameState.READY)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _difficulty = MutableStateFlow(Difficulty.EASY)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _levelConfig = MutableStateFlow(GameEngine.getLevelConfig(Difficulty.EASY, 1))
    val levelConfig: StateFlow<ColorChainLevel> = _levelConfig.asStateFlow()

    private val _tiles = MutableStateFlow<List<ColorTile>>(emptyList())
    val tiles: StateFlow<List<ColorTile>> = _tiles.asStateFlow()

    private val _targetColor = MutableStateFlow(GameColor.BLUE)
    val targetColor: StateFlow<GameColor> = _targetColor.asStateFlow()

    private val _chain = MutableStateFlow(0)
    val chain: StateFlow<Int> = _chain.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _remainingTimeMs = MutableStateFlow(10_000L)
    val remainingTimeMs: StateFlow<Long> = _remainingTimeMs.asStateFlow()

    private val _totalGameDurationMs = MutableStateFlow(0L)
    val totalGameDurationMs: StateFlow<Long> = _totalGameDurationMs.asStateFlow()

    private val _correctTaps = MutableStateFlow(0)
    val correctTaps: StateFlow<Int> = _correctTaps.asStateFlow()

    private val _wrongTaps = MutableStateFlow(0)
    val wrongTaps: StateFlow<Int> = _wrongTaps.asStateFlow()

    private val _levelMatches = MutableStateFlow(0)
    val levelMatches: StateFlow<Int> = _levelMatches.asStateFlow()

    private val _lastWrongTarget = MutableStateFlow<GameColor?>(null)
    val lastWrongTarget: StateFlow<GameColor?> = _lastWrongTarget.asStateFlow()

    private val _lastWrongTapped = MutableStateFlow<GameColor?>(null)
    val lastWrongTapped: StateFlow<GameColor?> = _lastWrongTapped.asStateFlow()

    private val _activeMilestone = MutableStateFlow<Milestone?>(null)
    val activeMilestone: StateFlow<Milestone?> = _activeMilestone.asStateFlow()

    private val _levelCompleteCelebration = MutableStateFlow<Int?>(null)
    val levelCompleteCelebration: StateFlow<Int?> = _levelCompleteCelebration.asStateFlow()

    private val _lastTapFeedback = MutableStateFlow<TapFeedbackData?>(null)
    val lastTapFeedback: StateFlow<TapFeedbackData?> = _lastTapFeedback.asStateFlow()

    private val _connectionAnimation = MutableStateFlow<ConnectionAnimationData?>(null)
    val connectionAnimation: StateFlow<ConnectionAnimationData?> = _connectionAnimation.asStateFlow()

    private val _lastResult = MutableStateFlow<ColorChainResult?>(null)
    val lastResult: StateFlow<ColorChainResult?> = _lastResult.asStateFlow()

    private var timerJob: Job? = null
    private var lastTickSecondWarned = -1
    private val random = Random(System.currentTimeMillis())

    init {
        // Sync audio/haptics with saved preferences
        viewModelScope.launch {
            settings.collect { s ->
                soundManager.isEnabled = s.soundEnabled
                hapticsManager.isEnabled = s.hapticsEnabled
            }
        }
        _difficulty.value = repository.settingsFlow.value.defaultDifficulty
    }

    fun setDifficulty(diff: Difficulty) {
        _difficulty.value = diff
        repository.setDefaultDifficulty(diff)
    }

    fun setThemeMode(mode: AppThemeMode) {
        repository.setThemeMode(mode)
    }

    fun toggleSound(enabled: Boolean) {
        repository.setSoundEnabled(enabled)
    }

    fun toggleHaptics(enabled: Boolean) {
        repository.setHapticsEnabled(enabled)
    }

    fun toggleColorAssist(enabled: Boolean) {
        repository.setColorAssistEnabled(enabled)
    }

    fun resetStatistics() {
        repository.resetStats()
    }

    /**
     * Starts a new game session.
     */
    fun startNewGame(difficulty: Difficulty = _difficulty.value) {
        stopTimer()
        _difficulty.value = difficulty
        _currentLevel.value = 1
        _chain.value = 0
        _score.value = 0
        _correctTaps.value = 0
        _wrongTaps.value = 0
        _levelMatches.value = 0
        _totalGameDurationMs.value = 0L
        _lastWrongTarget.value = null
        _lastWrongTapped.value = null
        _activeMilestone.value = null
        _levelCompleteCelebration.value = null
        _lastTapFeedback.value = null
        _connectionAnimation.value = null

        val config = GameEngine.getLevelConfig(difficulty, 1)
        _levelConfig.value = config
        _remainingTimeMs.value = config.initialTimeMs
        lastTickSecondWarned = -1

        val availableColors = GameEngine.getAvailableColors(difficulty, 1)
        val initialTarget = GameEngine.selectNextTargetColor(availableColors, null, random)
        _targetColor.value = initialTarget

        _tiles.value = GameEngine.generateBoard(
            gridSize = config.gridRows,
            availableColors = availableColors,
            targetColor = initialTarget,
            random = random
        )

        _gameState.value = GameState.PLAYING
        startTimer()
    }

    /**
     * User taps a tile.
     */
    fun onTileTapped(tile: ColorTile) {
        if (_gameState.value != GameState.PLAYING) return

        val currentTarget = _targetColor.value
        val config = _levelConfig.value
        val currentDiff = _difficulty.value

        if (tile.color == currentTarget) {
            // Correct tap!
            val newCorrect = _correctTaps.value + 1
            val newChain = _chain.value + 1
            val newLevelMatches = _levelMatches.value + 1

            _correctTaps.value = newCorrect
            _chain.value = newChain
            _levelMatches.value = newLevelMatches

            // Deterministic score calculation
            val earnedPoints = GameEngine.calculateScorePoints(newChain)
            val milestone = Milestone.getForChain(newChain)
            val bonusPoints = milestone?.bonusPoints ?: 0
            _score.value = _score.value + earnedPoints + bonusPoints

            // Add time bonus clamped to maxTimeMs
            val newTime = minOf(
                currentDiff.maxTimeMs,
                _remainingTimeMs.value + config.timeBonusMs
            )
            _remainingTimeMs.value = newTime

            // Provide sound and haptic feedback
            soundManager.playCorrectSound(newChain)
            hapticsManager.vibrateCorrect()

            // Trigger connection line animation & tap feedback
            _connectionAnimation.value = ConnectionAnimationData(
                tileIndex = tile.id,
                color = tile.color
            )
            _lastTapFeedback.value = TapFeedbackData(
                tileId = tile.id,
                isCorrect = true,
                pointsAdded = earnedPoints + bonusPoints
            )

            // Milestone celebration if achieved
            if (milestone != null) {
                _activeMilestone.value = milestone
                soundManager.playMilestoneSound()
                hapticsManager.vibrateMilestone()
                viewModelScope.launch {
                    delay(1200)
                    if (_activeMilestone.value == milestone) {
                        _activeMilestone.value = null
                    }
                }
            }

            // Check level complete
            if (newLevelMatches >= config.requiredChain) {
                handleLevelComplete()
            } else {
                // Next turn in current level
                setupNextTurn()
            }
        } else {
            // Wrong tap!
            _wrongTaps.value = _wrongTaps.value + 1
            _lastWrongTarget.value = currentTarget
            _lastWrongTapped.value = tile.color

            _lastTapFeedback.value = TapFeedbackData(
                tileId = tile.id,
                isCorrect = false
            )

            soundManager.playWrongSound()
            hapticsManager.vibrateWrong()

            // Conclude game immediately
            finishGame(GameOverReason.WRONG_COLOR)
        }
    }

    private fun setupNextTurn() {
        val config = _levelConfig.value
        val diff = _difficulty.value
        val level = _currentLevel.value
        val availableColors = GameEngine.getAvailableColors(diff, level)
        val nextTarget = GameEngine.selectNextTargetColor(
            availableColors = availableColors,
            previousTarget = _targetColor.value,
            random = random
        )
        _targetColor.value = nextTarget
        _tiles.value = GameEngine.generateBoard(
            gridSize = config.gridRows,
            availableColors = availableColors,
            targetColor = nextTarget,
            random = random
        )
    }

    private fun handleLevelComplete() {
        val completedLevel = _currentLevel.value
        _gameState.value = GameState.LEVEL_COMPLETE
        _score.value = _score.value + GameEngine.LEVEL_COMPLETE_BONUS
        _levelCompleteCelebration.value = completedLevel

        soundManager.playLevelCompleteSound()
        hapticsManager.vibrateMilestone()

        viewModelScope.launch {
            // Auto transition after brief celebration (600ms)
            delay(650)
            if (_gameState.value == GameState.LEVEL_COMPLETE) {
                val nextLevel = completedLevel + 1
                _currentLevel.value = nextLevel
                _levelMatches.value = 0
                _levelCompleteCelebration.value = null

                val newConfig = GameEngine.getLevelConfig(_difficulty.value, nextLevel)
                _levelConfig.value = newConfig

                // Reset time to full initial time for new level
                _remainingTimeMs.value = newConfig.initialTimeMs

                val availableColors = GameEngine.getAvailableColors(_difficulty.value, nextLevel)
                val nextTarget = GameEngine.selectNextTargetColor(availableColors, _targetColor.value, random)
                _targetColor.value = nextTarget

                _tiles.value = GameEngine.generateBoard(
                    gridSize = newConfig.gridRows,
                    availableColors = availableColors,
                    targetColor = nextTarget,
                    random = random
                )

                _gameState.value = GameState.PLAYING
            }
        }
    }

    /**
     * High accuracy monotonic countdown timer.
     */
    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            var lastNano = System.nanoTime()
            while (_gameState.value == GameState.PLAYING) {
                delay(30)
                val currentNano = System.nanoTime()
                val deltaMs = (currentNano - lastNano) / 1_000_000L
                lastNano = currentNano

                _totalGameDurationMs.value += deltaMs
                val updatedTime = _remainingTimeMs.value - deltaMs

                if (updatedTime <= 0L) {
                    _remainingTimeMs.value = 0L
                    finishGame(GameOverReason.TIMEOUT)
                    break
                } else {
                    _remainingTimeMs.value = updatedTime
                    // Check warning tick when remaining time <= 3s
                    if (updatedTime <= 3000L) {
                        val currentSecond = (updatedTime / 1000L).toInt()
                        if (currentSecond != lastTickSecondWarned) {
                            lastTickSecondWarned = currentSecond
                            soundManager.playWarningTick()
                        }
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun pauseGame() {
        if (_gameState.value == GameState.PLAYING) {
            _gameState.value = GameState.PAUSED
            stopTimer()
        }
    }

    fun resumeGame() {
        if (_gameState.value == GameState.PAUSED) {
            _gameState.value = GameState.PLAYING
            startTimer()
        }
    }

    fun restartGame() {
        startNewGame(_difficulty.value)
    }

    fun exitGame() {
        stopTimer()
        _gameState.value = GameState.READY
    }

    private fun finishGame(reason: GameOverReason) {
        stopTimer()
        _gameState.value = GameState.GAME_OVER

        if (reason == GameOverReason.TIMEOUT) {
            soundManager.playGameOverSound()
            hapticsManager.vibrateGameOver()
        }

        val accuracy = GameEngine.calculateAccuracy(_correctTaps.value, _wrongTaps.value)
        val rating = GameEngine.calculateRating(_chain.value, accuracy)

        val (isNewBestScore, isNewBestChain) = repository.recordGameFinished(
            score = _score.value,
            chain = _chain.value,
            elapsedTimeMs = _totalGameDurationMs.value,
            level = _currentLevel.value,
            correctTaps = _correctTaps.value,
            wrongTaps = _wrongTaps.value
        )

        _lastResult.value = ColorChainResult(
            difficulty = _difficulty.value,
            score = _score.value,
            chain = _chain.value,
            correctTaps = _correctTaps.value,
            wrongTaps = _wrongTaps.value,
            accuracy = accuracy,
            level = _currentLevel.value,
            elapsedTimeMs = _totalGameDurationMs.value,
            won = false,
            reason = reason,
            targetColor = _lastWrongTarget.value ?: _targetColor.value,
            tappedColor = _lastWrongTapped.value,
            isNewBestScore = isNewBestScore,
            isNewBestChain = isNewBestChain,
            rating = rating
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
