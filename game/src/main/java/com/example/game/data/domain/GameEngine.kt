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
                if (!isPlayable(row, column, level)) continue

                val index = row * level.columns + column

                val available = CellColor.entries.filter { color ->
                    val horizontal = column >= 2 &&
                            cells[index - 1]?.let {
                                !it.isFrozen && it.color == color
                            } == true &&
                            cells[index - 2]?.let {
                                !it.isFrozen && it.color == color
                            } == true

                    val vertical = row >= 2 &&
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
        if (state.cells[first]?.isFrozen != false) return state
        if (state.cells[second]?.isFrozen != false) return state

        val swapped = state.cells.toMutableList()
        val temp = swapped[first]
        swapped[first] = swapped[second]
        swapped[second] = temp

        if (findMatches(swapped, level).isEmpty()) return state

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
        var frozenBroken = initial.frozenBroken

        repeat(100) {
            val matches = findMatches(cells, level)

            if (matches.isEmpty()) {
                return updateStatus(
                    initial.copy(
                        cells = cells,
                        collected = collected,
                        frozenBroken = frozenBroken
                    ),
                    level
                )
            }

            val frozenNeighbors = matches
                .flatMap { neighbors(it, level) }
                .filter { cells[it]?.isFrozen == true }
                .toSet()

            for (index in matches) {
                val cell = cells[index] ?: continue

                collected[cell.color.name] =
                    (collected[cell.color.name] ?: 0) + 1

                cells[index] = null
            }

            for (index in frozenNeighbors) {
                val cell = cells[index] ?: continue
                cells[index] = cell.copy(isFrozen = false)
                frozenBroken++
            }

            cells = refill(cells, level).toMutableList()
        }

        return updateStatus(
            initial.copy(
                cells = cells,
                collected = collected,
                frozenBroken = frozenBroken
            ),
            level
        )
    }

    private fun refill(
        source: List<GameCell?>,
        level: LevelConfig
    ): List<GameCell?> {
        val result = source.toMutableList()

        for (column in 0 until level.columns) {
            var segment = mutableListOf<Int>()

            fun compactSegment() {
                if (segment.isEmpty()) return

                val remaining = segment
                    .mapNotNull { result[it] }
                    .filter { !it.isFrozen }

                for ((position, index) in segment.withIndex()) {
                    result[index] = remaining.getOrNull(position)
                        ?: GameCell(CellColor.entries.random())
                }

                segment = mutableListOf()
            }

            for (row in level.rows - 1 downTo 0) {
                val index = row * level.columns + column

                if (!isPlayable(row, column, level)) {
                    compactSegment()
                } else if (result[index]?.isFrozen == true) {
                    compactSegment()
                } else {
                    segment.add(index)
                }
            }

            compactSegment()
        }

        return result
    }

    private fun findMatches(
        cells: List<GameCell?>,
        level: LevelConfig
    ): Set<Int> {
        val matches = mutableSetOf<Int>()

        for (row in 0 until level.rows) {
            var column = 0

            while (column < level.columns) {
                if (!isPlayable(row, column, level)) {
                    column++
                    continue
                }

                val cell = cells[row * level.columns + column]

                if (cell == null || cell.isFrozen) {
                    column++
                    continue
                }

                var end = column + 1

                while (end < level.columns) {
                    if (!isPlayable(row, end, level)) break

                    val next = cells[row * level.columns + end]
                    if (next == null || next.isFrozen || next.color != cell.color) break
                    end++
                }

                if (end - column >= 3) {
                    for (c in column until end) {
                        matches.add(row * level.columns + c)
                    }
                }

                column = end
            }
        }

        for (column in 0 until level.columns) {
            var row = 0

            while (row < level.rows) {
                if (!isPlayable(row, column, level)) {
                    row++
                    continue
                }

                val cell = cells[row * level.columns + column]

                if (cell == null || cell.isFrozen) {
                    row++
                    continue
                }

                var end = row + 1

                while (end < level.rows) {
                    if (!isPlayable(end, column, level)) break

                    val next = cells[end * level.columns + column]
                    if (next == null || next.isFrozen || next.color != cell.color) break
                    end++
                }

                if (end - row >= 3) {
                    for (r in row until end) {
                        matches.add(r * level.columns + column)
                    }
                }

                row = end
            }
        }

        return matches
    }

    private fun neighbors(index: Int, level: LevelConfig): List<Int> {
        val row = index / level.columns
        val column = index % level.columns

        return listOf(
            row - 1 to column,
            row + 1 to column,
            row to column - 1,
            row to column + 1
        ).mapNotNull { (r, c) ->
            if (r in 0 until level.rows &&
                c in 0 until level.columns &&
                isPlayable(r, c, level)
            ) {
                r * level.columns + c
            } else {
                null
            }
        }
    }

    private fun isPlayable(
        row: Int,
        column: Int,
        level: LevelConfig
    ): Boolean {
        val centerRow = (level.rows - 1) / 2f
        val centerColumn = (level.columns - 1) / 2f

        return when (level.shape.uppercase()) {
            "CROSS" -> {
                val isCorner = (row == 0 || row == level.rows - 1) &&
                        (column == 0 || column == level.columns - 1)
                !isCorner
            }

            "CENTER_HOLE" ->{

                val isInsideCenterHole = abs(row - centerRow) < 1.0f &&
                        abs(column - centerColumn) < 1.0f

                !isInsideCenterHole
            }

            else -> true
        }
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

        return state.copy(
            status = when {
                won -> GameStatus.WON
                state.movesLeft <= 0 -> GameStatus.LOST
                else -> GameStatus.PLAYING
            }
        )
    }

    private fun areAdjacent(first: Int, second: Int, columns: Int): Boolean {
        val row1 = first / columns
        val col1 = first % columns
        val row2 = second / columns
        val col2 = second % columns

        return (row1 == row2 && abs(col1 - col2) == 1) ||
                (col1 == col2 && abs(row1 - row2) == 1)
    }
}