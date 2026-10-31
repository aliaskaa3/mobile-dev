package com.example.game.data.domain

import com.example.game.data.model.CellColor
import com.example.game.data.model.GameCell
import com.example.game.data.model.GameState
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelConfig
import kotlin.math.abs

class GameEngine {

    fun createInitialState(level: LevelConfig): GameState {
        val cells = MutableList<GameCell?>(level.rows * level.columns) { null }

        for (row in 0 until level.rows) {
            for (column in 0 until level.columns) {
                val index = row * level.columns + column

                val available = CellColor.entries.filter { color ->
                    val horizontal = column >= 2 &&
                            cells[index - 1]?.color == color &&
                            cells[index - 2]?.color == color

                    val vertical = row >= 2 &&
                            cells[index - level.columns]?.color == color &&
                            cells[index - level.columns * 2]?.color == color

                    !horizontal && !vertical
                }

                cells[index] = GameCell(available.random())
            }
        }

        return GameState(cells = cells, movesLeft = level.moves)
    }

    fun swapCells(
        state: GameState,
        first: Int,
        second: Int,
        level: LevelConfig
    ): GameState {
        if (state.status != GameStatus.PLAYING) return state
        if (first !in state.cells.indices || second !in state.cells.indices) return state
        if (!areAdjacent(first, second, level.columns)) return state

        val swapped = state.cells.toMutableList()
        val temp = swapped[first]
        swapped[first] = swapped[second]
        swapped[second] = temp

        if (findMatches(swapped, level.rows, level.columns).isEmpty()) return state

        return resolve(
            state.copy(cells = swapped, movesLeft = state.movesLeft - 1),
            level
        )
    }

    private fun resolve(
        initial: GameState,
        level: LevelConfig
    ): GameState {
        var cells = initial.cells.toMutableList()
        val collected = initial.collected.toMutableMap()

        repeat(100) {
            val matches = findMatches(cells, level.rows, level.columns)

            if (matches.isEmpty()) {
                return updateStatus(
                    initial.copy(cells = cells, collected = collected),
                    level
                )
            }

            for (index in matches) {
                val cell = cells[index] ?: continue
                collected[cell.color.name] =
                    (collected[cell.color.name] ?: 0) + 1
                cells[index] = null
            }

            for (column in 0 until level.columns) {
                val remaining = (level.rows - 1 downTo 0)
                    .mapNotNull { row -> cells[row * level.columns + column] }

                for (row in level.rows - 1 downTo 0) {
                    val index = row * level.columns + column
                    val position = level.rows - 1 - row

                    cells[index] = remaining.getOrNull(position)
                        ?: GameCell(CellColor.entries.random())
                }
            }
        }

        return updateStatus(
            initial.copy(cells = cells, collected = collected),
            level
        )
    }

    private fun findMatches(
        cells: List<GameCell?>,
        rows: Int,
        columns: Int
    ): Set<Int> {
        val result = mutableSetOf<Int>()

        for (row in 0 until rows) {
            var column = 0

            while (column < columns) {
                val color = cells[row * columns + column]?.color
                var end = column + 1

                while (
                    color != null &&
                    end < columns &&
                    cells[row * columns + end]?.color == color
                ) {
                    end++
                }

                if (color != null && end - column >= 3) {
                    for (c in column until end) {
                        result.add(row * columns + c)
                    }
                }

                column = end
            }
        }

        for (column in 0 until columns) {
            var row = 0

            while (row < rows) {
                val color = cells[row * columns + column]?.color
                var end = row + 1

                while (
                    color != null &&
                    end < rows &&
                    cells[end * columns + column]?.color == color
                ) {
                    end++
                }

                if (color != null && end - row >= 3) {
                    for (r in row until end) {
                        result.add(r * columns + column)
                    }
                }

                row = end
            }
        }

        return result
    }

    private fun updateStatus(
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

        val status = when {
            won -> GameStatus.WON
            state.movesLeft <= 0 -> GameStatus.LOST
            else -> GameStatus.PLAYING
        }

        return state.copy(status = status)
    }

    private fun areAdjacent(
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
}