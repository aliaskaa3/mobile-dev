package com.example.game.data.model

enum class GameStatus {
    PLAYING,
    WON,
    LOST
}

data class GameState(
    val cells: List<GameCell?>,
    val movesLeft: Int,
    val collected: Map<String, Int> = emptyMap(),
    val frozenBroken: Int = 0,
    val status: GameStatus = GameStatus.PLAYING
)