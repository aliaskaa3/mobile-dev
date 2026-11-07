package com.example.game.data.domain

import com.example.game.data.model.CellColor
import com.example.game.data.model.GameCell
import com.example.game.data.model.GameState
import com.example.game.data.model.LevelConfig
import com.example.game.data.model.SpecialType

internal class MatchResolver(
    private val rules: GameRules
) {

    fun resolve(
        initial: GameState,
        level: LevelConfig,
        swappedIndex: Int? = null
    ): GameState {
        var cells = initial.cells.toMutableList()
        val collected = initial.collected.toMutableMap()
        var frozenBroken = initial.frozenBroken

        repeat(100) { iteration ->
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

            val specialCreations = findSpecialCreations(
                cells = cells,
                level = level,
                swappedIndex = if (iteration == 0) swappedIndex else null
            )

            val frozenNeighbors = matches
                .flatMap { rules.neighbors(it, level) }
                .filter { cells[it]?.isFrozen == true }
                .toSet()

            for (index in matches) {
                val cell = cells[index] ?: continue
                if (cell.isFrozen) continue

                if (index in specialCreations) {
                    val specialCell = specialCreations[index]!!
                    collected[cell.color.name] = (collected[cell.color.name] ?: 0) + 1
                    cells[index] = specialCell
                } else {
                    collected[cell.color.name] = (collected[cell.color.name] ?: 0) + 1
                    cells[index] = null
                }
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

    private fun findSpecialCreations(
        cells: List<GameCell?>,
        level: LevelConfig,
        swappedIndex: Int?
    ): Map<Int, GameCell> {
        val result = mutableMapOf<Int, GameCell>()

        val line5Matches = findLineMatchesOfLength(cells, level, minLength = 5)
        for ((indices, color) in line5Matches) {
            val spawnIndex = swappedIndex?.takeIf { it in indices } ?: indices.first()
            result[spawnIndex] = GameCell(color = color, specialType = SpecialType.DISCO_BALL)
        }

        val cornerMatches = findCornerMatches(cells, level)
        for ((indices, color) in cornerMatches) {
            val spawnIndex = swappedIndex?.takeIf { it in indices } ?: indices.first()
            if (spawnIndex !in result) {
                result[spawnIndex] = GameCell(color = color, specialType = SpecialType.BOMB)
            }
        }

        val squareMatches = findSquareMatches(cells, level)
        for ((indices, color) in squareMatches) {
            val spawnIndex = swappedIndex?.takeIf { it in indices } ?: indices.first()
            if (spawnIndex !in result) {
                result[spawnIndex] = GameCell(color = color, specialType = SpecialType.AIRPLANE)
            }
        }

        return result
    }

    private fun findLineMatchesOfLength(
        cells: List<GameCell?>,
        level: LevelConfig,
        minLength: Int
    ): List<Pair<Set<Int>, CellColor>> {
        val result = mutableListOf<Pair<Set<Int>, CellColor>>()

        for (row in 0 until level.rows) {
            var col = 0
            while (col < level.columns) {
                if (!rules.isPlayable(row, col, level)) {
                    col++
                    continue
                }
                val cell = cells[row * level.columns + col]
                if (cell == null || cell.isFrozen) {
                    col++
                    continue
                }

                var end = col + 1
                while (end < level.columns && rules.isPlayable(row, end, level)) {
                    val next = cells[row * level.columns + end]
                    if (next == null || next.isFrozen || next.color != cell.color) break
                    end++
                }

                if (end - col >= minLength) {
                    val indices = (col until end).map { row * level.columns + it }.toSet()
                    result.add(indices to cell.color)
                }
                col = end
            }
        }

        for (col in 0 until level.columns) {
            var row = 0
            while (row < level.rows) {
                if (!rules.isPlayable(row, col, level)) {
                    row++
                    continue
                }
                val cell = cells[row * level.columns + col]
                if (cell == null || cell.isFrozen) {
                    row++
                    continue
                }

                var end = row + 1
                while (end < level.rows && rules.isPlayable(end, col, level)) {
                    val next = cells[end * level.columns + col]
                    if (next == null || next.isFrozen || next.color != cell.color) break
                    end++
                }

                if (end - row >= minLength) {
                    val indices = (row until end).map { it * level.columns + col }.toSet()
                    result.add(indices to cell.color)
                }
                row = end
            }
        }

        return result
    }

    private fun findCornerMatches(
        cells: List<GameCell?>,
        level: LevelConfig
    ): List<Pair<Set<Int>, CellColor>> {
        val result = mutableListOf<Pair<Set<Int>, CellColor>>()
        val horizLines = findLineMatchesOfLength(cells, level, minLength = 3)
        val vertLines = findLineMatchesOfLength(cells, level, minLength = 3)

        for ((hIndices, hColor) in horizLines) {
            for ((vIndices, vColor) in vertLines) {
                if (hColor == vColor) {
                    val intersection = hIndices.intersect(vIndices)
                    if (intersection.isNotEmpty()) {
                        val combined = hIndices + vIndices
                        if (combined.size >= 5) {
                            result.add(combined to hColor)
                        }
                    }
                }
            }
        }
        return result
    }

    private fun findSquareMatches(
        cells: List<GameCell?>,
        level: LevelConfig
    ): List<Pair<Set<Int>, CellColor>> {
        val result = mutableListOf<Pair<Set<Int>, CellColor>>()

        for (row in 0 until level.rows - 1) {
            for (col in 0 until level.columns - 1) {
                if (!rules.isPlayable(row, col, level) || !rules.isPlayable(row, col + 1, level) ||
                    !rules.isPlayable(row + 1, col, level) || !rules.isPlayable(row + 1, col + 1, level)
                ) {
                    continue
                }

                val i1 = row * level.columns + col
                val i2 = row * level.columns + (col + 1)
                val i3 = (row + 1) * level.columns + col
                val i4 = (row + 1) * level.columns + (col + 1)

                val c1 = cells[i1]
                val c2 = cells[i2]
                val c3 = cells[i3]
                val c4 = cells[i4]

                if (c1 != null && !c1.isFrozen &&
                    c2 != null && !c2.isFrozen && c2.color == c1.color &&
                    c3 != null && !c3.isFrozen && c3.color == c1.color &&
                    c4 != null && !c4.isFrozen && c4.color == c1.color
                ) {
                    result.add(setOf(i1, i2, i3, i4) to c1.color)
                }
            }
        }

        return result
    }

    fun findMatches(
        cells: List<GameCell?>,
        level: LevelConfig
    ): Set<Int> {
        val matches = mutableSetOf<Int>()

        val lines = findLineMatchesOfLength(cells, level, minLength = 3)
        for ((indices, _) in lines) {
            matches.addAll(indices)
        }

        val squares = findSquareMatches(cells, level)
        for ((indices, _) in squares) {
            matches.addAll(indices)
        }

        return matches
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

    fun activateAirplane(
        state: GameState,
        index: Int,
        level: LevelConfig
    ): GameState {
        val cells = state.cells.toMutableList()
        val collected = state.collected.toMutableMap()
        var frozenBroken = state.frozenBroken

        var targetIndex: Int? = null

        val frozenGoal = level.goals.find { it.type == "BREAK_FROZEN" }
        if (frozenGoal != null && frozenBroken < frozenGoal.target) {
            val frozenIndices = cells.indices.filter { cells[it]?.isFrozen == true && it != index }
            if (frozenIndices.isNotEmpty()) {
                targetIndex = frozenIndices.random()
            }
        }

        if (targetIndex == null) {
            val uncompletedCollectGoals = level.goals.filter { goal ->
                goal.type == "COLLECT" && (collected[goal.color] ?: 0) < goal.target
            }
            if (uncompletedCollectGoals.isNotEmpty()) {
                val targetGoalColor = uncompletedCollectGoals.random().color
                val colorIndices = cells.indices.filter {
                    cells[it]?.color?.name == targetGoalColor && it != index
                }
                if (colorIndices.isNotEmpty()) {
                    targetIndex = colorIndices.random()
                }
            }
        }

        if (targetIndex == null) {
            val validIndices = cells.indices.filter {
                it != index && cells[it] != null && rules.isPlayable(it / level.columns, it % level.columns, level)
            }
            if (validIndices.isNotEmpty()) {
                targetIndex = validIndices.random()
            }
        }

        cells[index] = null

        if (targetIndex != null && targetIndex in cells.indices) {
            val targetCell = cells[targetIndex]
            if (targetCell != null) {
                if (targetCell.isFrozen) {
                    cells[targetIndex] = targetCell.copy(isFrozen = false)
                    frozenBroken++
                } else {
                    collected[targetCell.color.name] = (collected[targetCell.color.name] ?: 0) + 1
                    cells[targetIndex] = null
                }
            }
        }

        val refilled = refill(cells, level)
        return resolve(
            state.copy(
                cells = refilled,
                collected = collected,
                frozenBroken = frozenBroken
            ),
            level
        )
    }

    fun activateBomb(
        state: GameState,
        index: Int,
        level: LevelConfig
    ): GameState {
        val cells = state.cells.toMutableList()
        val collected = state.collected.toMutableMap()
        var frozenBroken = state.frozenBroken

        val r = index / level.columns
        val c = index % level.columns

        for (row in maxOf(0, r - 1)..minOf(level.rows - 1, r + 1)) {
            for (col in maxOf(0, c - 1)..minOf(level.columns - 1, c + 1)) {
                if (!rules.isPlayable(row, col, level)) continue

                val i = row * level.columns + col
                val cell = cells[i] ?: continue

                if (cell.isFrozen) {
                    cells[i] = cell.copy(isFrozen = false)
                    frozenBroken++
                } else {
                    collected[cell.color.name] = (collected[cell.color.name] ?: 0) + 1
                    cells[i] = null
                }
            }
        }

        val refilled = refill(cells, level)
        return resolve(
            state.copy(
                cells = refilled,
                collected = collected,
                frozenBroken = frozenBroken
            ),
            level
        )
    }

    fun activateDiscoBall(
        state: GameState,
        discoIndex: Int,
        targetColor: CellColor,
        level: LevelConfig
    ): GameState {
        val cells = state.cells.toMutableList()
        val collected = state.collected.toMutableMap()
        var frozenBroken = state.frozenBroken

        cells[discoIndex] = null

        for (i in cells.indices) {
            val cell = cells[i] ?: continue
            if (cell.color == targetColor) {
                if (cell.isFrozen) {
                    cells[i] = cell.copy(isFrozen = false)
                    frozenBroken++
                } else {
                    collected[cell.color.name] = (collected[cell.color.name] ?: 0) + 1
                    cells[i] = null
                }
            }
        }

        val refilled = refill(cells, level)
        return resolve(
            state.copy(
                cells = refilled,
                collected = collected,
                frozenBroken = frozenBroken
            ),
            level
        )
    }
}
