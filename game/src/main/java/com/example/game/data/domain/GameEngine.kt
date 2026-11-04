package com.example.game.data.domain

import com.example.game.data.model.BoosterType
import com.example.game.data.model.GameState
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelConfig

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

        if (state.cells[first]?.isFrozen != false) return state
        if (state.cells[second]?.isFrozen != false) return state

        val swapped = state.cells.toMutableList()
        val temp = swapped[first]

        swapped[first] = swapped[second]
        swapped[second] = temp

        if (matchResolver.findMatches(swapped, level).isEmpty()) {
            return state
        }

        return matchResolver.resolve(
            state.copy(
                cells = swapped,
                movesLeft = state.movesLeft - 1
            ),
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

        return boosterHandler.useBooster(
            state = state,
            type = type,
            index = index,
            level = level,
            targetIndex = targetIndex
        )
    }
}