package com.example.colorchain.data.logic

import com.example.colorchain.data.model.ColorChainLevel
import com.example.colorchain.data.model.ColorTile
import com.example.colorchain.data.model.Difficulty
import com.example.colorchain.data.model.GameColor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

object GameEngine {

    const val BASE_POINTS = 100
    const val PERFECT_CHAIN_BONUS = 250
    const val LEVEL_COMPLETE_BONUS = 500

    /**
     * Deterministic score calculation:
     * basePoints = 100
     * multiplier = 1 + min(chain - 1, 20) * 0.05
     * score = round(basePoints * multiplier)
     */
    fun calculateScorePoints(chain: Int): Int {
        val cappedChainOffset = min(chain - 1, 20).coerceAtLeast(0)
        val multiplier = 1.0 + (cappedChainOffset * 0.05)
        return (BASE_POINTS * multiplier).roundToInt()
    }

    /**
     * Returns multiplier formatted for display or calculation.
     */
    fun calculateMultiplier(chain: Int): Double {
        val cappedChainOffset = min(chain - 1, 20).coerceAtLeast(0)
        return 1.0 + (cappedChainOffset * 0.05)
    }

    /**
     * Required chain to complete a level: 10 + (level - 1) * 2
     */
    fun getRequiredChain(level: Int): Int {
        return (10 + (level - 1) * 2).coerceIn(10, 50)
    }

    /**
     * Colors available at given level and difficulty according to progression specs.
     */
    fun getAvailableColors(difficulty: Difficulty, level: Int): List<GameColor> {
        return when (difficulty) {
            Difficulty.EASY -> when {
                level <= 1 -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW)
                level == 2 -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW, GameColor.ORANGE)
                level == 3 -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW, GameColor.ORANGE)
                level == 4 -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW, GameColor.ORANGE, GameColor.PURPLE)
                else -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW, GameColor.ORANGE, GameColor.PURPLE, GameColor.PINK)
            }
            Difficulty.MEDIUM -> when {
                level <= 1 -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW, GameColor.ORANGE, GameColor.PURPLE)
                level == 2 -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW, GameColor.ORANGE, GameColor.PURPLE, GameColor.CYAN)
                level == 3 -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW, GameColor.ORANGE, GameColor.PURPLE, GameColor.CYAN)
                else -> listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN, GameColor.YELLOW, GameColor.ORANGE, GameColor.PURPLE, GameColor.CYAN, GameColor.PINK, GameColor.TEAL)
            }
            Difficulty.HARD -> when {
                level <= 1 -> listOf(GameColor.RED, GameColor.ORANGE, GameColor.YELLOW, GameColor.GREEN, GameColor.TEAL, GameColor.CYAN, GameColor.BLUE)
                level == 2 -> listOf(GameColor.RED, GameColor.ORANGE, GameColor.YELLOW, GameColor.GREEN, GameColor.TEAL, GameColor.CYAN, GameColor.BLUE, GameColor.PURPLE)
                else -> GameColor.values().toList()
            }
        }
    }

    /**
     * Level configuration: grid dimensions, color counts, timer properties.
     */
    fun getLevelConfig(difficulty: Difficulty, level: Int): ColorChainLevel {
        val colors = getAvailableColors(difficulty, level)
        val requiredChain = getRequiredChain(level)

        return when (difficulty) {
            Difficulty.EASY -> {
                val (dimension, initialTimeMs) = when {
                    level <= 2 -> Pair(3, 10_000L)
                    level == 3 -> Pair(4, 10_000L)
                    else -> Pair(4, 9_500L)
                }
                ColorChainLevel(
                    level = level,
                    gridRows = dimension,
                    gridColumns = dimension,
                    colorCount = colors.size,
                    initialTimeMs = initialTimeMs,
                    timeBonusMs = 1_000L,
                    requiredChain = requiredChain
                )
            }
            Difficulty.MEDIUM -> {
                val (dimension, initialTimeMs) = when {
                    level <= 2 -> Pair(4, 8_000L)
                    else -> Pair(5, 7_500L)
                }
                ColorChainLevel(
                    level = level,
                    gridRows = dimension,
                    gridColumns = dimension,
                    colorCount = colors.size,
                    initialTimeMs = initialTimeMs,
                    timeBonusMs = 800L,
                    requiredChain = requiredChain
                )
            }
            Difficulty.HARD -> {
                val (dimension, initialTimeMs) = when {
                    level <= 2 -> Pair(5, 6_000L)
                    else -> Pair(6, 5_500L)
                }
                ColorChainLevel(
                    level = level,
                    gridRows = dimension,
                    gridColumns = dimension,
                    colorCount = colors.size,
                    initialTimeMs = initialTimeMs,
                    timeBonusMs = 600L,
                    requiredChain = requiredChain
                )
            }
        }
    }

    /**
     * Board generation meeting all strict criteria:
     * 1. Target color is guaranteed to appear at least once.
     * 2. Number of target matches is controlled (1 to 3 depending on grid size).
     * 3. At least one non-matching tile (never all target).
     * 4. Tile positions are randomized.
     * 5. Colors used come from availableColors.
     */
    fun generateBoard(
        gridSize: Int,
        availableColors: List<GameColor>,
        targetColor: GameColor,
        random: Random = Random.Default
    ): List<ColorTile> {
        val totalTiles = gridSize * gridSize
        require(totalTiles >= 4) { "Grid must have at least 4 tiles" }
        require(availableColors.contains(targetColor)) { "Target color must be in available colors" }

        val decoyColors = availableColors.filter { it != targetColor }
        require(decoyColors.isNotEmpty()) { "Decoy colors must not be empty" }

        // Decide how many matching tiles: 1 to 3 (or at most totalTiles / 3, capped at 3)
        val maxMatches = minOf(3, (totalTiles / 3).coerceAtLeast(1))
        val targetCount = if (maxMatches > 1) {
            random.nextInt(1, maxMatches + 1)
        } else {
            1
        }

        val tileColors = ArrayList<GameColor>(totalTiles)
        // Add target colors
        repeat(targetCount) {
            tileColors.add(targetColor)
        }

        // Fill the rest with decoys
        val remainingCount = totalTiles - targetCount
        repeat(remainingCount) {
            val decoy = decoyColors[random.nextInt(decoyColors.size)]
            tileColors.add(decoy)
        }

        // Shuffle tile positions
        tileColors.shuffle(random)

        return tileColors.mapIndexed { index, color ->
            ColorTile(
                id = index,
                color = color,
                selected = false
            )
        }
    }

    /**
     * Selects a target color that exists in available colors, preferring
     * a color different from previousTarget if possible.
     */
    fun selectNextTargetColor(
        availableColors: List<GameColor>,
        previousTarget: GameColor? = null,
        random: Random = Random.Default
    ): GameColor {
        require(availableColors.isNotEmpty())
        if (availableColors.size == 1) return availableColors.first()

        val candidates = if (previousTarget != null && availableColors.size > 1) {
            availableColors.filter { it != previousTarget }
        } else {
            availableColors
        }
        return candidates[random.nextInt(candidates.size)]
    }

    fun calculateAccuracy(correctTaps: Int, wrongTaps: Int): Double {
        val total = correctTaps + wrongTaps
        if (total == 0) return 100.0
        return ((correctTaps.toDouble() / total) * 1000.0).roundToInt() / 10.0
    }

    fun calculateRating(chain: Int, accuracy: Double): String {
        return when {
            accuracy >= 98.0 && chain >= 20 -> "PERFECT"
            accuracy >= 92.0 && chain >= 15 -> "EXCELLENT"
            accuracy >= 85.0 && chain >= 10 -> "GREAT"
            chain >= 5 -> "GOOD"
            else -> "KEEP GOING"
        }
    }
}
