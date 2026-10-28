package com.example.game.data.model

data class LevelConfig(
    val id: Int,
    val rows: Int,
    val columns: Int,
    val moves: Int,
    val shape: String,
    val goals: List<LevelGoal>,
    val frozenCells: List<Int> = emptyList()
)

data class LevelGoal(
    val type: String,
    val color: String? = null,
    val target: Int
)