package com.example.game.data.domain

import com.example.game.data.model.CellColor
import com.example.game.data.model.GameCell
import com.example.game.data.model.GameState
import com.example.game.data.model.LevelConfig

internal class BoardGenerator(
    private val rules: GameRules
) {

    fun createInitialState(level: LevelConfig): GameState {
        require(level.rows > 0 && level.columns > 0) {
            "Размеры игрового поля должны быть положительными"
        }

        val cells = MutableList<GameCell?>(
            level.rows * level.columns
        ) { null }

        for (row in 0 until level.rows) {
            for (column in 0 until level.columns) {
                if (!rules.isPlayable(row, column, level)) continue

                val index = row * level.columns + column

                val available = CellColor.entries.filter { color ->
                    val horizontal =
                        column >= 2 &&
                                cells[index - 1]?.let {
                                    !it.isFrozen && it.color == color
                                } == true &&
                                cells[index - 2]?.let {
                                    !it.isFrozen && it.color == color
                                } == true

                    val vertical =
                        row >= 2 &&
                                cells[index - level.columns]?.let {
                                    !it.isFrozen && it.color == color
                                } == true &&
                                cells[index - level.columns * 2]?.let {
                                    !it.isFrozen && it.color == color
                                } == true

                    !horizontal && !vertical
                }

                cells[index] = GameCell(
                    color = available.random(),
                    isFrozen = index in level.frozenCells
                )
            }
        }

        return GameState(
            cells = cells,
            movesLeft = level.moves
        )
    }
}