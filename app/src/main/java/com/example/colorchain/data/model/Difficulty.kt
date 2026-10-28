package com.example.colorchain.data.model

enum class Difficulty(
    val title: String,
    val initialTimeMs: Long,
    val timeBonusMs: Long,
    val maxTimeMs: Long
) {
    EASY(
        title = "Easy",
        initialTimeMs = 10_000L,
        timeBonusMs = 1_000L,
        maxTimeMs = 12_000L
    ),
    MEDIUM(
        title = "Medium",
        initialTimeMs = 8_000L,
        timeBonusMs = 800L,
        maxTimeMs = 10_000L
    ),
    HARD(
        title = "Hard",
        initialTimeMs = 6_000L,
        timeBonusMs = 600L,
        maxTimeMs = 8_000L
    )
}
