package com.example.game.data.domain

import com.example.game.data.model.CellColor
import com.example.game.data.model.GameCell
import com.example.game.data.model.GameState
import com.example.game.data.model.LevelConfig

internal class MatchResolver(
    private val rules: GameRules
) {

    fun resolve(
        initial: GameState,
        level: LevelConfig
    ): GameState {
        var cells = initial.cells.toMutableList()
        val collected = initial.collected.toMutableMap()
        var frozenBroken = initial.frozenBroken

        repeat(100) {
            val matches = findMatches(cells, level)

            if (matches.isEmpty()) {
                return rules.updateStatus(
                    initial.copy(
                        cells = cells,
                        collected = collected,
                        frozenBroken = frozenBroken
                    ),
                    level
                )
            }

            val frozenNeighbors = matches
                .flatMap { rules.neighbors(it, level) }
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

        return rules.updateStatus(
            initial.copy(
                cells = cells,
                collected = collected,
                frozenBroken = frozenBroken
            ),
            level
        )
    }

    fun refill(
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

                if (!rules.isPlayable(row, column, level)) {
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

    fun findMatches(
        cells: List<GameCell?>,
        level: LevelConfig
    ): Set<Int> {
        val matches = mutableSetOf<Int>()

        for (row in 0 until level.rows) {
            var column = 0

            while (column < level.columns) {
                if (!rules.isPlayable(row, column, level)) {
                    column++
                    continue
                }

                val cell = cells[row * level.columns + column]

                if (cell == null || cell.isFrozen) {
                    column++
                    continue
                }

                var end = column + 1

                while (
                    end < level.columns &&
                    rules.isPlayable(row, end, level)
                ) {
                    val next = cells[row * level.columns + end]

                    if (
                        next == null ||
                        next.isFrozen ||
                        next.color != cell.color
                    ) {
                        break
                    }

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
                if (!rules.isPlayable(row, column, level)) {
                    row++
                    continue
                }

                val cell = cells[row * level.columns + column]

                if (cell == null || cell.isFrozen) {
                    row++
                    continue
                }

                var end = row + 1

                while (
                    end < level.rows &&
                    rules.isPlayable(end, column, level)
                ) {
                    val next = cells[end * level.columns + column]

                    if (
                        next == null ||
                        next.isFrozen ||
                        next.color != cell.color
                    ) {
                        break
                    }

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
}