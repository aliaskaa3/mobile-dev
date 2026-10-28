package com.example.game.data.model

enum class CellColor {
    RED,
    BLUE,
    YELLOW,
    GREEN,
    PINK
}

enum class SpecialType {
    NONE,
    AIRPLANE,
    BOMB,
    DISCO_BALL
}

data class GameCell(
    val color: CellColor,
    val isFrozen: Boolean = false,
    val specialType: SpecialType = SpecialType.NONE
)