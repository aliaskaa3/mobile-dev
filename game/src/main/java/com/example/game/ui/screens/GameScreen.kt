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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.domain.GameEngine
import com.example.game.data.model.GameState
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelGoal
import com.example.game.data.repository.LevelRepository
import com.example.game.data.utils.cellColor
import com.example.game.ui.components.InfoBox
import com.example.game.ui.theme.GreenLight
import com.example.game.ui.theme.OrangeDark

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    levelId: Int = 1
) {
    val context = LocalContext.current
    val level = remember(levelId) {
        LevelRepository(context).getLevel(levelId)
    }
    val engine = remember { GameEngine() }

    var game by remember(levelId) {
        mutableStateOf(engine.createInitialState(level))
    }
    var selectedCell by remember(levelId) {
        mutableIntStateOf(-1)
    }

    val goalText = level.goals.joinToString("\n") { goal ->
        formatGoal(goal, game.collected, game.frozenBroken)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GreenLight)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoBox(
                title = "Уровень",
                value = level.id.toString(),
                modifier = Modifier.width(65.dp)
            )

            Spacer(Modifier.width(6.dp))

            InfoBox(
                title = "Цели",
                value = goalText,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(6.dp))

            InfoBox(
                title = "Ходы",
                value = game.movesLeft.toString(),
                modifier = Modifier.width(60.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White, RoundedCornerShape(18.dp))
                .border(2.dp, OrangeDark, RoundedCornerShape(18.dp))
                .padding(8.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(level.columns),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                userScrollEnabled = false
            ) {
                items(game.cells.indices.toList()) { index ->
                    val cell = game.cells[index]

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .then(
                                if (selectedCell == index) {
                                    Modifier.border(
                                        3.dp,
                                        OrangeDark,
                                        CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(
                                enabled = cell != null &&
                                        game.status == GameStatus.PLAYING
                            ) {
                                if (selectedCell == -1) {
                                    selectedCell = index
                                } else if (selectedCell == index) {
                                    selectedCell = -1
                                } else {
                                    game = engine.swapCells(
                                        state = game,
                                        first = selectedCell,
                                        second = index,
                                        level = level
                                    )
                                    selectedCell = -1
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.8f)
                                    .background(cellColor(cell.color), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cell.isFrozen) {
                                    Text("❄", fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (game.status != GameStatus.PLAYING) {
            Spacer(Modifier.height(12.dp))

            Text(
                text = if (game.status == GameStatus.WON) {
                    "Уровень пройден!"
                } else {
                    "Ходы закончились"
                },
                color = OrangeDark,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Button(
                onClick = {
                    game = engine.createInitialState(level)
                    selectedCell = -1
                }
            ) {
                Text("Начать заново")
            }
        }
    }
}

private fun formatGoal(
    goal: LevelGoal,
    collected: Map<String, Int>,
    frozenBroken: Int
): String {
    return when (goal.type) {
        "COLLECT" -> {
            val colorName = when (goal.color) {
                "RED" -> "красных"
                "BLUE" -> "синих"
                "YELLOW" -> "жёлтых"
                "GREEN" -> "зелёных"
                "PINK" -> "розовых"
                else -> "фишек"
            }

            "$colorName: ${collected[goal.color] ?: 0}/${goal.target}"
        }

        "BREAK_FROZEN" -> "Лёд: $frozenBroken/${goal.target}"
        else -> "${goal.type}: ${goal.target}"
    }
}