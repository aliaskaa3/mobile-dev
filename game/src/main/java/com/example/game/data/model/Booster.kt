package com.example.game.data.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class BoosterType {
    SWAP,
    CROSS_CLEAR,
    HAMMER
}

data class Booster(
    val type: BoosterType,
    val icon: ImageVector,
    val contentDescription: String
)