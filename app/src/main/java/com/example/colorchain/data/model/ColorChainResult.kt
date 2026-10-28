package com.example.colorchain.data.model

enum class GameOverReason {
    TIMEOUT,
    WRONG_COLOR,
    QUIT
}

data class ColorChainResult(
    val difficulty: Difficulty,
    val score: Int,
    val chain: Int,
    val correctTaps: Int,
    val wrongTaps: Int,
    val accuracy: Double,
    val level: Int,
    val elapsedTimeMs: Long,
    val won: Boolean = false,
    val reason: GameOverReason = GameOverReason.WRONG_COLOR,
    val targetColor: GameColor? = null,
    val tappedColor: GameColor? = null,
    val isNewBestScore: Boolean = false,
    val isNewBestChain: Boolean = false,
    val rating: String = "GOOD"
)
