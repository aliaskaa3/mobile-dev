package com.example.game.data.domain

import com.example.game.data.model.BoosterType
import com.example.game.data.model.GameState
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelConfig
import com.example.game.data.model.SpecialType

class GameEngine {

    private val rules = GameRules()
    private val boardGenerator = BoardGenerator(rules)
    private val matchResolver = MatchResolver(rules)
    private val boosterHandler = BoosterHandler(rules, matchResolver)

    fun createInitialState(level: LevelConfig): GameState {
        return boardGenerator.createInitialState(level)
    }

    fun swapCells(
        state: GameState,
        first: Int,
        second: Int,
        level: LevelConfig,
        allowNonAdjacent: Boolean = false
    ): GameState {
        if (state.status != GameStatus.PLAYING) return state
        if (first !in state.cells.indices) return state
        if (second !in state.cells.indices) return state
        if (first == second) return state

        if (
            !allowNonAdjacent &&
            !rules.areAdjacent(first, second, level.columns)
        ) {
            return state
        }

        val cell1 = state.cells[first] ?: return state
        val cell2 = state.cells[second] ?: return state

        if (cell1.isFrozen || cell2.isFrozen) return state

        if (cell1.specialType == SpecialType.DISCO_BALL || cell2.specialType == SpecialType.DISCO_BALL) {
            val stateAfterMove = state.copy(movesLeft = state.movesLeft - 1)
            val discoIndex = if (cell1.specialType == SpecialType.DISCO_BALL) first else second
            val otherCell = if (discoIndex == first) cell2 else cell1

            return if (otherCell.specialType == SpecialType.DISCO_BALL) {
                val clearedCells = stateAfterMove.cells.map { null }
                matchResolver.resolve(stateAfterMove.copy(cells = clearedCells), level)
            } else {
                matchResolver.activateDiscoBall(stateAfterMove, discoIndex, otherCell.color, level)
            }
        }

        if (cell1.specialType == SpecialType.BOMB || cell2.specialType == SpecialType.BOMB) {
            val swapped = state.cells.toMutableList()
            swapped[first] = cell2
            swapped[second] = cell1
            val bombIndex = if (cell1.specialType == SpecialType.BOMB) second else first
            val stateAfterMove = state.copy(cells = swapped, movesLeft = state.movesLeft - 1)
            return matchResolver.activateBomb(stateAfterMove, bombIndex, level)
        }

        if (cell1.specialType == SpecialType.AIRPLANE || cell2.specialType == SpecialType.AIRPLANE) {
            val swapped = state.cells.toMutableList()
            swapped[first] = cell2
            swapped[second] = cell1
            val airplaneIndex = if (cell1.specialType == SpecialType.AIRPLANE) second else first
            val stateAfterMove = state.copy(cells = swapped, movesLeft = state.movesLeft - 1)
            return matchResolver.activateAirplane(stateAfterMove, airplaneIndex, level)
        }

        val swapped = state.cells.toMutableList()
        swapped[first] = cell2
        swapped[second] = cell1

        if (matchResolver.findMatches(swapped, level).isEmpty()) {
            return state
        }

        return matchResolver.resolve(
            state.copy(
                cells = swapped,
                movesLeft = state.movesLeft - 1
            ),
            level,
            swappedIndex = second
        )
    }

    fun activateSpecialPiece(
        state: GameState,
        index: Int,
        level: LevelConfig
    ): GameState {
        if (state.status != GameStatus.PLAYING) return state
        val cell = state.cells.getOrNull(index) ?: return state

        val stateAfterMove = state.copy(movesLeft = state.movesLeft - 1)
        return when (cell.specialType) {
            SpecialType.AIRPLANE -> matchResolver.activateAirplane(stateAfterMove, index, level)
            SpecialType.BOMB -> matchResolver.activateBomb(stateAfterMove, index, level)
            SpecialType.DISCO_BALL -> {
                val mostCommonColor = state.cells.filterNotNull()
                    .filter { !it.isFrozen }
                    .groupingBy { it.color }
                    .eachCount()
                    .maxByOrNull { it.value }?.key ?: cell.color
                matchResolver.activateDiscoBall(stateAfterMove, index, mostCommonColor, level)
            }
            SpecialType.NONE -> state
        }
    }

    fun useBooster(
        state: GameState,
        type: BoosterType,
        index: Int,
        level: LevelConfig,
        targetIndex: Int? = null
    ): GameState {
        if (state.status != GameStatus.PLAYING) return state

        return boosterHandler.useBooster(
            state = state,
            type = type,
            index = index,
            level = level,
            targetIndex = targetIndex
        )
    }
}
