package com.example.colorchain

import com.example.colorchain.data.logic.GameEngine
import com.example.colorchain.data.model.Difficulty
import com.example.colorchain.data.model.GameColor
import com.example.colorchain.data.model.Milestone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ColorChainGameEngineTest {

    @Test
    fun testDeterministicScoringFormula() {
        // Chain 1: multiplier 1.0, base 100 -> 100
        assertEquals(1.0, GameEngine.calculateMultiplier(1), 0.001)
        assertEquals(100, GameEngine.calculateScorePoints(1))

        // Chain 5: multiplier 1 + (4 * 0.05) = 1.20, base 100 -> 120
        assertEquals(1.20, GameEngine.calculateMultiplier(5), 0.001)
        assertEquals(120, GameEngine.calculateScorePoints(5))

        // Chain 10: multiplier 1 + (9 * 0.05) = 1.45, base 100 -> 145
        assertEquals(1.45, GameEngine.calculateMultiplier(10), 0.001)
        assertEquals(145, GameEngine.calculateScorePoints(10))

        // Chain 20: multiplier 1 + (19 * 0.05) = 1.95, base 100 -> 195
        assertEquals(1.95, GameEngine.calculateMultiplier(20), 0.001)
        assertEquals(195, GameEngine.calculateScorePoints(20))

        // Chain 25 (capped at offset 20): multiplier 1 + 1.0 = 2.0 -> 200
        assertEquals(2.0, GameEngine.calculateMultiplier(25), 0.001)
        assertEquals(200, GameEngine.calculateScorePoints(25))
    }

    @Test
    fun testTargetExistenceIn100Boards() {
        val availableColors = GameColor.values().toList()
        for (i in 0 until 100) {
            val targetColor = availableColors[i % availableColors.size]
            val board = GameEngine.generateBoard(
                gridSize = 4,
                availableColors = availableColors,
                targetColor = targetColor,
                random = Random(i)
            )

            // Must contain at least one matching tile
            val matchCount = board.count { it.color == targetColor }
            assertTrue("Board #$i must contain target color $targetColor", matchCount >= 1)

            // Must contain at least one non-matching tile
            val nonMatchCount = board.count { it.color != targetColor }
            assertTrue("Board #$i must contain non-matching tiles", nonMatchCount >= 1)

            // Never all tiles identical
            assertNotEquals("Board #$i must not be entirely target color", board.size, matchCount)
        }
    }

    @Test
    fun testColorRandomizationAndNonIdenticalBoards() {
        val available = GameColor.easyInitialColors()
        val target = GameColor.BLUE

        val boards = (1..10).map { seed ->
            GameEngine.generateBoard(
                gridSize = 3,
                availableColors = available,
                targetColor = target,
                random = Random(seed * 100)
            ).map { it.color }
        }

        val distinctBoards = boards.distinct()
        assertTrue("Generated boards should vary in tile placements", distinctBoards.size > 1)
    }

    @Test
    fun testLevelProgressionConfigurations() {
        // Easy mode progression
        val easyL1 = GameEngine.getLevelConfig(Difficulty.EASY, 1)
        assertEquals(3, easyL1.gridRows)
        assertEquals(3, easyL1.gridColumns)
        assertEquals(4, easyL1.colorCount)
        assertEquals(10_000L, easyL1.initialTimeMs)
        assertEquals(1_000L, easyL1.timeBonusMs)
        assertEquals(10, easyL1.requiredChain)

        val easyL3 = GameEngine.getLevelConfig(Difficulty.EASY, 3)
        assertEquals(4, easyL3.gridRows)
        assertEquals(4, easyL3.gridColumns)
        assertEquals(5, easyL3.colorCount)
        assertEquals(14, easyL3.requiredChain)

        // Medium mode progression
        val medL1 = GameEngine.getLevelConfig(Difficulty.MEDIUM, 1)
        assertEquals(4, medL1.gridRows)
        assertEquals(6, medL1.colorCount)
        assertEquals(8_000L, medL1.initialTimeMs)
        assertEquals(800L, medL1.timeBonusMs)

        val medL3 = GameEngine.getLevelConfig(Difficulty.MEDIUM, 3)
        assertEquals(5, medL3.gridRows)
        assertEquals(7, medL3.colorCount)

        // Hard mode progression
        val hardL1 = GameEngine.getLevelConfig(Difficulty.HARD, 1)
        assertEquals(5, hardL1.gridRows)
        assertEquals(7, hardL1.colorCount)
        assertEquals(6_000L, hardL1.initialTimeMs)
        assertEquals(600L, hardL1.timeBonusMs)

        val hardL3 = GameEngine.getLevelConfig(Difficulty.HARD, 3)
        assertEquals(6, hardL3.gridRows)
        assertEquals(9, hardL3.colorCount)
        assertTrue(hardL3.gridRows <= 6) // Max 6x6 constraint
    }

    @Test
    fun testMilestones() {
        assertEquals("GOOD!", Milestone.getForChain(5)?.title)
        assertEquals(0, Milestone.getForChain(5)?.bonusPoints)

        val m10 = Milestone.getForChain(10)
        assertNotNull(m10)
        assertEquals("GREAT!", m10?.title)
        assertEquals(250, m10?.bonusPoints)

        val m20 = Milestone.getForChain(20)
        assertNotNull(m20)
        assertEquals("AMAZING!", m20?.title)
        assertEquals(250, m20?.bonusPoints)

        val m30 = Milestone.getForChain(30)
        assertNotNull(m30)
        assertEquals("UNSTOPPABLE!", m30?.title)
        assertEquals(250, m30?.bonusPoints)

        val m50 = Milestone.getForChain(50)
        assertNotNull(m50)
        assertEquals("COLOR MASTER!", m50?.title)
        assertEquals(250, m50?.bonusPoints)
    }

    @Test
    fun testAccuracyAndRatings() {
        val acc1 = GameEngine.calculateAccuracy(24, 1)
        assertEquals(96.0, acc1, 0.1)

        val acc2 = GameEngine.calculateAccuracy(20, 0)
        assertEquals(100.0, acc2, 0.001)

        val ratingPerfect = GameEngine.calculateRating(24, 99.0)
        assertEquals("PERFECT", ratingPerfect)

        val ratingExcellent = GameEngine.calculateRating(16, 95.0)
        assertEquals("EXCELLENT", ratingExcellent)

        val ratingGreat = GameEngine.calculateRating(12, 88.0)
        assertEquals("GREAT", ratingGreat)

        val ratingGood = GameEngine.calculateRating(6, 60.0)
        assertEquals("GOOD", ratingGood)

        val ratingKeepGoing = GameEngine.calculateRating(2, 50.0)
        assertEquals("KEEP GOING", ratingKeepGoing)
    }

    @Test
    fun testNextTargetSelection() {
        val colors = listOf(GameColor.RED, GameColor.BLUE, GameColor.GREEN)
        val selected = GameEngine.selectNextTargetColor(colors, GameColor.RED, Random(42))
        assertTrue(colors.contains(selected))
        assertNotEquals(GameColor.RED, selected)
    }
}
