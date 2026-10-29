package com.example.game.data.domain

import com.example.game.data.model.CellColor
import com.example.game.data.model.GameCell
import kotlin.math.abs

class GameEngine {

    fun createBoard(rows: Int, columns: Int): List<GameCell> {
        val board = mutableListOf<GameCell>()

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val availableColors = CellColor.entries.filter { color ->
                    val horizontalMatch = column >= 2 &&
                            board[row * columns + column - 1].color == color &&
                            board[row * columns + column - 2].color == color

                    val verticalMatch = row >= 2 &&
                            board[(row - 1) * columns + column].color == color &&
                            board[(row - 2) * columns + column].color == color

                    !horizontalMatch && !verticalMatch
                }

                board.add(GameCell(availableColors.random()))
            }
        }

        return board
    }

    fun swapCells(
        board: List<GameCell>,
        first: Int,
        second: Int,
        rows: Int,
        columns: Int
    ): List<GameCell> {
        if (first !in board.indices || second !in board.indices) {
            return board
        }

        if (!areAdjacent(first, second, columns)) {
            return board
        }

        val swappedBoard = board.toMutableList()
        val temporary = swappedBoard[first]

        swappedBoard[first] = swappedBoard[second]
        swappedBoard[second] = temporary

        if (findMatches(swappedBoard, rows, columns).isEmpty()) {
            return board
        }

        return resolveMatches(swappedBoard, rows, columns)
    }

    private fun areAdjacent(
        first: Int,
        second: Int,
        columns: Int
    ): Boolean {
        val firstRow = first / columns
        val firstColumn = first % columns
        val secondRow = second / columns
        val secondColumn = second % columns

        return (
                firstRow == secondRow &&
                        abs(firstColumn - secondColumn) == 1
                ) || (
                firstColumn == secondColumn &&
                        abs(firstRow - secondRow) == 1
                )
    }

    private fun resolveMatches(
        initialBoard: List<GameCell>,
        rows: Int,
        columns: Int
    ): List<GameCell> {
        var board: List<GameCell?> = initialBoard

        repeat(100) {
            val matches = findMatches(board, rows, columns)

            if (matches.isEmpty()) {
                return board.filterNotNull()
            }

            val nextBoard: MutableList<GameCell?> =
                board.toMutableList()

            matches.forEach { index ->
                nextBoard[index] = null
            }

            for (column in 0 until columns) {
                val remainingCells = (rows - 1 downTo 0)
                    .mapNotNull { row ->
                        nextBoard[row * columns + column]
                    }

                for (row in rows - 1 downTo 0) {
                    val index = row * columns + column
                    val positionFromBottom = rows - 1 - row

                    nextBoard[index] =
                        if (positionFromBottom < remainingCells.size) {
                            remainingCells[positionFromBottom]
                        } else {
                            GameCell(CellColor.entries.random())
                        }
                }
            }

            board = nextBoard
        }

        return board.filterNotNull()
    }

    private fun findMatches(
        board: List<GameCell?>,
        rows: Int,
        columns: Int
    ): Set<Int> {
        val matches = mutableSetOf<Int>()

        // Ищем горизонтальные совпадения.
        for (row in 0 until rows) {
            var column = 0

            while (column < columns) {
                val color = board[row * columns + column]?.color
                var endColumn = column + 1

                while (
                    color != null &&
                    endColumn < columns &&
                    board[row * columns + endColumn]?.color == color
                ) {
                    endColumn++
                }

                if (color != null && endColumn - column >= 3) {
                    for (c in column until endColumn) {
                        matches.add(row * columns + c)
                    }
                }

                column = endColumn
            }
        }

        // Ищем вертикальные совпадения.
        for (column in 0 until columns) {
            var row = 0

            while (row < rows) {
                val color = board[row * columns + column]?.color
                var endRow = row + 1

                while (
                    color != null &&
                    endRow < rows &&
                    board[endRow * columns + column]?.color == color
                ) {
                    endRow++
                }

                if (color != null && endRow - row >= 3) {
                    for (r in row until endRow) {
                        matches.add(r * columns + column)
                    }
                }

                row = endRow
            }
        }

        return matches
    }
}
