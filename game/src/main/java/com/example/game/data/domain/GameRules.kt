package com.example.game.data.domain

import com.example.game.data.model.GameState
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelConfig
import kotlin.math.abs

internal class GameRules {

    fun isPlayable(
        row: Int,
        column: Int,
        level: LevelConfig
    ): Boolean {
        val centerRow = (level.rows - 1) / 2f
        val centerColumn = (level.columns - 1) / 2f

        return when (level.shape.uppercase()) {
            "CROSS" -> {
                val isCorner =
                    (row == 0 || row == level.rows - 1) &&
                            (column == 0 || column == level.columns - 1)

                !isCorner
            }

            "CENTER_HOLE" -> {
                val isInsideCenterHole =
                    abs(row - centerRow) < 1.0f &&
                            abs(column - centerColumn) < 1.0f

                !isInsideCenterHole
            }

            else -> true
        }
    }

    fun areAdjacent(
        first: Int,
        second: Int,
        columns: Int
    ): Boolean {
        val row1 = first / columns
        val col1 = first % columns
        val row2 = second / columns
        val col2 = second % columns

        return (row1 == row2 && abs(col1 - col2) == 1) ||
                (col1 == col2 && abs(row1 - row2) == 1)
    }

    fun neighbors(
        index: Int,
        level: LevelConfig
    ): List<Int> {
        val row = index / level.columns
        val column = index % level.columns

        return listOf(
            row - 1 to column,
            row + 1 to column,
            row to column - 1,
            row to column + 1
        ).mapNotNull { (r, c) ->
            if (
                r in 0 until level.rows &&
                c in 0 until level.columns &&
                isPlayable(r, c, level)
            ) {
                r * level.columns + c
            } else {
                null
            }
        }
    }

    fun updateStatus(
        state: GameState,
        level: LevelConfig
    ): GameState {
        val won = level.goals.all { goal ->
            when (goal.type) {
                "COLLECT" ->
                    (state.collected[goal.color] ?: 0) >= goal.target

                "BREAK_FROZEN" ->
                    state.frozenBroken >= goal.target

                else -> false
            }
        }

        return state.copy(
            status = when {
                won -> GameStatus.WON
                state.movesLeft <= 0 -> GameStatus.LOST
                else -> GameStatus.PLAYING
            }
        )
    }
}