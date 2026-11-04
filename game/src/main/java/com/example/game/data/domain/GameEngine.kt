package com.example.game.data.domain

import com.example.game.data.model.BoosterType
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
        level: LevelConfig,
        allowNonAdjacent: Boolean = false
    ): GameState {
        if (state.status != GameStatus.PLAYING) return state
        if (first !in state.cells.indices || second !in state.cells.indices) return state
        if (first == second) return state

        if (!allowNonAdjacent && !areAdjacent(first, second, level.columns)) {
            return state
        }

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

    fun useBooster(
        state: GameState,
        type: BoosterType,
        index: Int,
        level: LevelConfig,
        targetIndex: Int? = null
    ): GameState {
        if (state.status != GameStatus.PLAYING) return state
        if (index !in state.cells.indices || state.cells[index] == null) return state
        if (!isPlayable(index / level.columns, index % level.columns, level)) return state

        return when (type) {
            BoosterType.SWAP -> applySwapBooster(state, index, targetIndex, level)
            BoosterType.HAMMER -> applyHammerBooster(state, index, level)
            BoosterType.CROSS_CLEAR -> applyCrossClearBooster(state, index, level)
        }
    }

    private fun applySwapBooster(
        state: GameState,
        index: Int,
        targetIndex: Int?,
        level: LevelConfig
    ): GameState {
        if (targetIndex == null || targetIndex !in state.cells.indices) return state
        val cell1 = state.cells[index] ?: return state
        val cell2 = state.cells[targetIndex] ?: return state

        if (targetIndex !in neighbors(
                index,
                level
            ) || cell1.isFrozen || cell2.isFrozen
        ) return state

        val swappedCells = state.cells.toMutableList()
        swappedCells[index] = cell2
        swappedCells[targetIndex] = cell1

        return resolve(state.copy(cells = swappedCells), level)
    }

    private fun applyHammerBooster(state: GameState, index: Int, level: LevelConfig): GameState {
        val cells = state.cells.toMutableList()
        val cell = cells[index] ?: return state

        if (cell.isFrozen) {
            cells[index] = cell.copy(isFrozen = false)
            return updateStatus(
                state.copy(cells = cells, frozenBroken = state.frozenBroken + 1),
                level
            )
        }

        val collected = state.collected.toMutableMap()
        collected[cell.color.name] = (collected[cell.color.name] ?: 0) + 1

        val targetColumn = index % level.columns
        var writeRow = index / level.columns

        for (readRow in (writeRow - 1) downTo 0) {
            val writeIndex = writeRow * level.columns + targetColumn
            val readIndex = readRow * level.columns + targetColumn
            val readCell = cells[readIndex]

            if (readCell?.isFrozen == true) continue

            if (cells[writeIndex]?.isFrozen == true) {
                writeRow--
                cells[writeRow * level.columns + targetColumn] = readCell
            } else {
                cells[writeIndex] = readCell
            }
            if (readRow != writeRow) cells[readIndex] = null
            writeRow--
        }

        return resolve(state.copy(cells = refill(cells, level), collected = collected), level)
    }

    private fun applyCrossClearBooster(
        state: GameState,
        index: Int,
        level: LevelConfig
    ): GameState {
        val targetRow = index / level.columns
        val targetColumn = index % level.columns
        val cells = state.cells.toMutableList()
        val collected = state.collected.toMutableMap()
        var currentFrozenBroken = state.frozenBroken

        for (i in cells.indices) {
            val r = i / level.columns
            val c = i % level.columns
            if ((r == targetRow || c == targetColumn) && isPlayable(r, c, level)) {
                val cell = cells[i] ?: continue
                if (cell.isFrozen) {
                    cells[i] = cell.copy(isFrozen = false)
                    currentFrozenBroken++
                } else {
                    collected[cell.color.name] = (collected[cell.color.name] ?: 0) + 1
                    cells[i] = null
                }
            }
        }

        val filledCells = refill(cells, level).toMutableList()
        val structuralFrozenIndexes =
            filledCells.indices.filter { filledCells[it]?.isFrozen == true }
        for (i in structuralFrozenIndexes) {
            filledCells[i] = filledCells[i]?.copy(isFrozen = false)
        }

        val intermediateState = state.copy(
            cells = filledCells,
            collected = collected,
            frozenBroken = currentFrozenBroken
        )
        val finalState = resolve(intermediateState, level)

        val finalCells = finalState.cells.toMutableList()
        for (i in structuralFrozenIndexes) {
            if (i in finalCells.indices) {
                finalCells[i] = finalCells[i]?.copy(isFrozen = true)
            }
        }
        return finalState.copy(cells = finalCells)
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
                if (cell.isFrozen) continue

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

                while (end < level.columns && isPlayable(row, end, level)) {
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

                while (end < level.rows && isPlayable(end, column, level)) {
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

            "CENTER_HOLE" -> {
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