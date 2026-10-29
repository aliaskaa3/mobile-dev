package com.example.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.game.data.domain.GameEngine
import com.example.game.data.model.BoosterType
import com.example.game.data.model.GameCell
import com.example.game.data.repository.BoosterRepository
import com.example.game.data.repository.LevelRepository
import com.example.game.data.utils.cellColor
import com.example.game.ui.components.BoosterButton
import com.example.game.ui.components.InfoBox
import com.example.game.ui.theme.GreenLight
import com.example.game.ui.theme.OrangeDark

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    levelId: Int = 1,
    onBoosterClick: (BoosterType) -> Unit = {}
) {
    val context = LocalContext.current

    val level = remember(levelId) {
        LevelRepository(context).getLevel(levelId)
    }

    val gameEngine = remember { GameEngine() }
    val boosterRepository = remember { BoosterRepository() }
    val boosters = boosterRepository.boosterItems

    var cells by remember(levelId) {
        mutableStateOf(
            gameEngine.createBoard(level.rows, level.columns)
        )
    }

    var selectedCell by remember(levelId) {
        mutableIntStateOf(-1)
    }

    var movesLeft by remember(levelId) {
        mutableIntStateOf(level.moves)
    }

    val goalText = level.goals.joinToString(separator = "\n") { goal ->
        when (goal.type) {
            "COLLECT" -> {
                val colorName = when (goal.color) {
                    "RED" -> "красных"
                    "BLUE" -> "синих"
                    "YELLOW" -> "жёлтых"
                    "GREEN" -> "зелёных"
                    "PINK" -> "розовых"
                    else -> "фишек"
                }

                "Собрать ${goal.target} $colorName"
            }

            "BREAK_FROZEN" ->
                "Разбить ${goal.target} замороженных фишек"

            else ->
                "Выполнить цель: ${goal.target}"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GreenLight)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoBox(
                title = "Уровень",
                value = level.id.toString(),
                modifier = Modifier.width(70.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            InfoBox(
                title = "Цель",
                value = goalText,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            InfoBox(
                title = "Ходы",
                value = movesLeft.toString(),
                modifier = Modifier.width(65.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White, RoundedCornerShape(18.dp))
                .border(2.dp, OrangeDark, RoundedCornerShape(18.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(level.columns),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false
            ) {
                items(cells.indices.toList()) { index ->
                    val cell: GameCell = cells[index]
                    val isSelected = selectedCell == index

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = OrangeDark,
                                        shape = CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable {
                                if (movesLeft <= 0) {
                                    return@clickable
                                }

                                if (selectedCell == -1) {
                                    selectedCell = index
                                } else if (selectedCell == index) {
                                    selectedCell = -1
                                } else {
                                    val updatedCells = gameEngine.swapCells(
                                        board = cells,
                                        first = selectedCell,
                                        second = index,
                                        rows = level.rows,
                                        columns = level.columns
                                    )

                                    if (updatedCells != cells) {
                                        cells = updatedCells
                                        movesLeft--
                                    }

                                    selectedCell = -1
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.8f)
                                .background(
                                    color = cellColor(cell.color),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.dp, OrangeDark, RoundedCornerShape(16.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            boosters.forEach { booster ->
                BoosterButton(
                    icon = booster.icon,
                    contentDescription = booster.contentDescription,
                    onClick = {
                        onBoosterClick(booster.type)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}