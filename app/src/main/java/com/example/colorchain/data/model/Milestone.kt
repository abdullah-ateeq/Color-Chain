package com.example.colorchain.data.model

data class Milestone(
    val chainThreshold: Int,
    val title: String,
    val bonusPoints: Int = 0
) {
    companion object {
        fun getForChain(chain: Int): Milestone? {
            return when (chain) {
                5 -> Milestone(5, "GOOD!")
                10 -> Milestone(10, "GREAT!", bonusPoints = 250)
                20 -> Milestone(20, "AMAZING!", bonusPoints = 250)
                30 -> Milestone(30, "UNSTOPPABLE!", bonusPoints = 250)
                40 -> Milestone(40, "INCREDIBLE!", bonusPoints = 250)
                50 -> Milestone(50, "COLOR MASTER!", bonusPoints = 250)
                else -> {
                    if (chain > 50 && chain % 10 == 0) {
                        Milestone(chain, "ULTRA CHAIN!", bonusPoints = 250)
                    } else {
                        null
                    }
                }
            }
        }
    }
}
