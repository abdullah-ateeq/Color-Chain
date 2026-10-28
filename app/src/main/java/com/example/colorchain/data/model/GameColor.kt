package com.example.colorchain.data.model

import androidx.compose.ui.graphics.Color

/**
 * The 9 carefully chosen distinguishable base colors for Color Chain.
 * Each color contains high-contrast accessible text colors and a distinct geometric symbol
 * for optional color accessibility / color-blindness support.
 */
enum class GameColor(
    val displayName: String,
    val color: Color,
    val onColor: Color,
    val symbolChar: String
) {
    RED(
        displayName = "RED",
        color = Color(0xFFE53935),
        onColor = Color.White,
        symbolChar = "▲"
    ),
    ORANGE(
        displayName = "ORANGE",
        color = Color(0xFFFB8C00),
        onColor = Color.White,
        symbolChar = "◆"
    ),
    YELLOW(
        displayName = "YELLOW",
        color = Color(0xFFFDD835),
        onColor = Color(0xFF212121),
        symbolChar = "★"
    ),
    GREEN(
        displayName = "GREEN",
        color = Color(0xFF43A047),
        onColor = Color.White,
        symbolChar = "●"
    ),
    TEAL(
        displayName = "TEAL",
        color = Color(0xFF00897B),
        onColor = Color.White,
        symbolChar = "✚"
    ),
    CYAN(
        displayName = "CYAN",
        color = Color(0xFF00ACC1),
        onColor = Color.White,
        symbolChar = "■"
    ),
    BLUE(
        displayName = "BLUE",
        color = Color(0xFF1E88E5),
        onColor = Color.White,
        symbolChar = "▼"
    ),
    PURPLE(
        displayName = "PURPLE",
        color = Color(0xFF8E24AA),
        onColor = Color.White,
        symbolChar = "✦"
    ),
    PINK(
        displayName = "PINK",
        color = Color(0xFFD81B60),
        onColor = Color.White,
        symbolChar = "♥"
    );

    companion object {
        fun easyInitialColors(): List<GameColor> = listOf(RED, BLUE, GREEN, YELLOW)
        fun mediumInitialColors(): List<GameColor> = listOf(RED, BLUE, GREEN, YELLOW, ORANGE, PURPLE)
        fun hardInitialColors(): List<GameColor> = listOf(RED, ORANGE, YELLOW, GREEN, TEAL, CYAN, BLUE)
    }
}
