package com.example.colorchain.data.model

data class ColorChainLevel(
    val level: Int,
    val gridRows: Int,
    val gridColumns: Int,
    val colorCount: Int,
    val initialTimeMs: Long,
    val timeBonusMs: Long,
    val requiredChain: Int
)
