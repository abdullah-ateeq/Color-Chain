package com.example.colorchain.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.colorchain.data.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserGameStats(
    val gamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val bestChain: Int = 0,
    val bestTimeMs: Long = 0L,
    val highestLevel: Int = 1,
    val totalCorrectTaps: Int = 0,
    val totalWrongTaps: Int = 0
) {
    val overallAccuracy: Double
        get() {
            val total = totalCorrectTaps + totalWrongTaps
            if (total == 0) return 100.0
            return ((totalCorrectTaps.toDouble() / total) * 1000.0).toLong() / 10.0
        }
}

data class UserGameSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val colorAssistEnabled: Boolean = false,
    val defaultDifficulty: Difficulty = Difficulty.EASY
)

class GamePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _statsFlow = MutableStateFlow(loadStats())
    val statsFlow: StateFlow<UserGameStats> = _statsFlow.asStateFlow()

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<UserGameSettings> = _settingsFlow.asStateFlow()

    private fun loadStats(): UserGameStats {
        return UserGameStats(
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            bestChain = prefs.getInt(KEY_BEST_CHAIN, 0),
            bestTimeMs = prefs.getLong(KEY_BEST_TIME_MS, 0L),
            highestLevel = prefs.getInt(KEY_HIGHEST_LEVEL, 1),
            totalCorrectTaps = prefs.getInt(KEY_TOTAL_CORRECT_TAPS, 0),
            totalWrongTaps = prefs.getInt(KEY_TOTAL_WRONG_TAPS, 0)
        )
    }

    private fun loadSettings(): UserGameSettings {
        val themeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM)

        val diffStr = prefs.getString(KEY_DEFAULT_DIFFICULTY, Difficulty.EASY.name) ?: Difficulty.EASY.name
        val difficulty = runCatching { Difficulty.valueOf(diffStr) }.getOrDefault(Difficulty.EASY)

        return UserGameSettings(
            themeMode = themeMode,
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
            colorAssistEnabled = prefs.getBoolean(KEY_COLOR_ASSIST_ENABLED, false),
            defaultDifficulty = difficulty
        )
    }

    fun recordGameFinished(
        score: Int,
        chain: Int,
        elapsedTimeMs: Long,
        level: Int,
        correctTaps: Int,
        wrongTaps: Int
    ): Pair<Boolean, Boolean> {
        val currentStats = _statsFlow.value
        val isNewBestScore = score > currentStats.bestScore
        val isNewBestChain = chain > currentStats.bestChain

        val newBestScore = if (isNewBestScore) score else currentStats.bestScore
        val newBestChain = if (isNewBestChain) chain else currentStats.bestChain
        val newBestTime = if (elapsedTimeMs > currentStats.bestTimeMs) elapsedTimeMs else currentStats.bestTimeMs
        val newHighestLevel = if (level > currentStats.highestLevel) level else currentStats.highestLevel

        val updatedStats = currentStats.copy(
            gamesPlayed = currentStats.gamesPlayed + 1,
            bestScore = newBestScore,
            bestChain = newBestChain,
            bestTimeMs = newBestTime,
            highestLevel = newHighestLevel,
            totalCorrectTaps = currentStats.totalCorrectTaps + correctTaps,
            totalWrongTaps = currentStats.totalWrongTaps + wrongTaps
        )

        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, updatedStats.gamesPlayed)
            .putInt(KEY_BEST_SCORE, updatedStats.bestScore)
            .putInt(KEY_BEST_CHAIN, updatedStats.bestChain)
            .putLong(KEY_BEST_TIME_MS, updatedStats.bestTimeMs)
            .putInt(KEY_HIGHEST_LEVEL, updatedStats.highestLevel)
            .putInt(KEY_TOTAL_CORRECT_TAPS, updatedStats.totalCorrectTaps)
            .putInt(KEY_TOTAL_WRONG_TAPS, updatedStats.totalWrongTaps)
            .apply()

        _statsFlow.value = updatedStats
        return Pair(isNewBestScore, isNewBestChain)
    }

    fun setThemeMode(themeMode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(themeMode = themeMode)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(soundEnabled = enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(hapticsEnabled = enabled)
    }

    fun setColorAssistEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_COLOR_ASSIST_ENABLED, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(colorAssistEnabled = enabled)
    }

    fun setDefaultDifficulty(difficulty: Difficulty) {
        prefs.edit().putString(KEY_DEFAULT_DIFFICULTY, difficulty.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(defaultDifficulty = difficulty)
    }

    fun resetStats() {
        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, 0)
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_BEST_CHAIN, 0)
            .putLong(KEY_BEST_TIME_MS, 0L)
            .putInt(KEY_HIGHEST_LEVEL, 1)
            .putInt(KEY_TOTAL_CORRECT_TAPS, 0)
            .putInt(KEY_TOTAL_WRONG_TAPS, 0)
            .apply()

        _statsFlow.value = UserGameStats(highestLevel = 1)
    }

    companion object {
        private const val PREFS_NAME = "color_chain_prefs"
        private const val KEY_GAMES_PLAYED = "key_games_played"
        private const val KEY_BEST_SCORE = "key_best_score"
        private const val KEY_BEST_CHAIN = "key_best_chain"
        private const val KEY_BEST_TIME_MS = "key_best_time_ms"
        private const val KEY_HIGHEST_LEVEL = "key_highest_level"
        private const val KEY_TOTAL_CORRECT_TAPS = "key_total_correct_taps"
        private const val KEY_TOTAL_WRONG_TAPS = "key_total_wrong_taps"

        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
        private const val KEY_COLOR_ASSIST_ENABLED = "key_color_assist_enabled"
        private const val KEY_DEFAULT_DIFFICULTY = "key_default_difficulty"
    }
}
