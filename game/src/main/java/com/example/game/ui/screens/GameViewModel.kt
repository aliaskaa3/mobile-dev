package com.example.game.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.game.data.domain.GameEngine
import com.example.game.data.model.BoosterType
import com.example.game.data.model.GameState
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelConfig
import com.example.game.data.model.SpecialType

data class GameUiState(
    val game: GameState,
    val selectedCell: Int = -1,
    val pendingBooster: BoosterType? = null
)

class GameViewModel(
    private val level: LevelConfig
) : ViewModel() {

    private val engine = GameEngine()

    var uiState by mutableStateOf(
        GameUiState(game = engine.createInitialState(level))
    )
        private set

    fun onCellTap(index: Int, onBoosterUsed: (BoosterType) -> Unit = {}) {
        val current = uiState
        val game = current.game

        if (game.status != GameStatus.PLAYING) return
        if (index !in game.cells.indices || game.cells[index] == null) return

        when (val booster = current.pendingBooster) {
            BoosterType.SWAP -> {
                if (current.selectedCell == -1) {
                    uiState = current.copy(selectedCell = index)
                } else {
                    val updated = engine.useBooster(
                        state = game,
                        type = BoosterType.SWAP,
                        index = current.selectedCell,
                        level = level,
                        targetIndex = index
                    )

                    if (updated != game) {
                        uiState = current.copy(
                            game = updated,
                            selectedCell = -1,
                            pendingBooster = null
                        )
                        onBoosterUsed(BoosterType.SWAP)
                    } else {
                        uiState = current.copy(
                            selectedCell = -1,
                            pendingBooster = null
                        )
                    }
                }
            }

            BoosterType.HAMMER, BoosterType.CROSS_CLEAR -> {
                val updated = engine.useBooster(
                    state = game,
                    type = booster,
                    index = index,
                    level = level
                )

                if (updated != game) {
                    uiState = current.copy(
                        game = updated,
                        selectedCell = -1,
                        pendingBooster = null
                    )
                    onBoosterUsed(booster)
                } else {
                    uiState = current.copy(
                        selectedCell = -1,
                        pendingBooster = null
                    )
                }
            }

            null -> {
                if (current.selectedCell == -1) {
                    uiState = current.copy(selectedCell = index)
                } else if (current.selectedCell == index) {
                    val cell = game.cells.getOrNull(index)
                    if (cell != null && cell.specialType != SpecialType.NONE) {
                        val updated = engine.activateSpecialPiece(
                            state = game,
                            index = index,
                            level = level
                        )
                        uiState = current.copy(
                            game = updated,
                            selectedCell = -1
                        )
                    } else {
                        uiState = current.copy(selectedCell = -1)
                    }
                } else {
                    val updated = engine.swapCells(
                        state = game,
                        first = current.selectedCell,
                        second = index,
                        level = level
                    )

                    uiState = current.copy(
                        game = updated,
                        selectedCell = -1
                    )
                }
            }
        }
    }

    fun activateBooster(type: BoosterType) {
        if (uiState.game.status != GameStatus.PLAYING) return

        uiState = uiState.copy(
            selectedCell = -1,
            pendingBooster = type
        )
    }

    fun addExtraMoves(count: Int = 5) {
        val current = uiState
        uiState = current.copy(
            game = current.game.copy(
                movesLeft = current.game.movesLeft + count,
                status = GameStatus.PLAYING
            )
        )
    }

    fun restart() {
        uiState = GameUiState(
            game = engine.createInitialState(level)
        )
    }
}
