package com.example.game.data.domain

import com.example.game.data.model.BoosterType
import com.example.game.data.model.GameState
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelConfig

internal class BoosterHandler(
    private val rules: GameRules,
    private val resolver: MatchResolver
) {

    fun useBooster(
        state: GameState,
        type: BoosterType,
        index: Int,
        level: LevelConfig,
        targetIndex: Int? = null
    ): GameState {
        if (state.status != GameStatus.PLAYING) return state

        if (
            !rules.isPlayable(
                index / level.columns,
                index % level.columns,
                level
            )
        ) {
            return state
        }

        return when (type) {
            BoosterType.SWAP ->
                applySwapBooster(state, index, targetIndex, level)

            BoosterType.HAMMER ->
                applyHammerBooster(state, index, level)

            BoosterType.CROSS_CLEAR ->
                applyCrossClearBooster(state, index, level)
        }
    }

    private fun applySwapBooster(
        state: GameState,
        index: Int,
        targetIndex: Int?,
        level: LevelConfig
    ): GameState {
        if (
            targetIndex == null ||
            targetIndex !in state.cells.indices
        ) {
            return state
        }

        val cell1 = state.cells[index] ?: return state
        val cell2 = state.cells[targetIndex] ?: return state

        if (
            targetIndex !in rules.neighbors(index, level) ||
            cell1.isFrozen ||
            cell2.isFrozen
        ) {
            return state
        }

        val swappedCells = state.cells.toMutableList()
        swappedCells[index] = cell2
        swappedCells[targetIndex] = cell1

        return resolver.resolve(
            state.copy(cells = swappedCells),
            level,
            swappedIndex = targetIndex
        )
    }

    private fun applyHammerBooster(
        state: GameState,
        index: Int,
        level: LevelConfig
    ): GameState {
        val cells = state.cells.toMutableList()
        val cell = cells[index] ?: return state

        if (cell.isFrozen) {
            cells[index] = cell.copy(isFrozen = false)

            return rules.updateStatus(
                state.copy(
                    cells = cells,
                    frozenBroken = state.frozenBroken + 1
                ),
                level
            )
        }

        val collected = state.collected.toMutableMap()
        collected[cell.color.name] = (collected[cell.color.name] ?: 0) + 1
        cells[index] = null

        val refilled = resolver.refill(cells, level)
        return resolver.resolve(
            state.copy(
                cells = refilled,
                collected = collected
            ),
            level
        )
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
            val row = i / level.columns
            val column = i % level.columns

            if (
                (row == targetRow || column == targetColumn) &&
                rules.isPlayable(row, column, level)
            ) {
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

        val refilled = resolver.refill(cells, level)
        return resolver.resolve(
            state.copy(
                cells = refilled,
                collected = collected,
                frozenBroken = currentFrozenBroken
            ),
            level
        )
    }
}
