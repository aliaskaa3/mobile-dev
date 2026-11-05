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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.data.model.BoosterType
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelGoal
import com.example.game.data.repository.BoosterRepository
import com.example.game.data.repository.LevelRepository
import com.example.game.data.utils.cellColor
import com.example.game.R
import com.example.game.ui.components.BoosterButton
import com.example.game.ui.components.InfoBox
import com.example.game.ui.components.LevelResultDialog
import com.example.game.ui.theme.GreenLight
import com.example.game.ui.theme.OrangeDark

private const val LEVEL_REWARD = 50
private const val LAST_LEVEL_ID = 5

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    levelId: Int = 1,
    onBoosterClick: (BoosterType) -> Unit = {},
    onNextLevel: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onRewardEarned: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    val level = remember(levelId) {
        LevelRepository(context).getLevel(levelId)
    }

    val factory = remember(level) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                    return GameViewModel(level) as T
                }

                throw IllegalArgumentException(
                    "Неизвестная модель: ${modelClass.name}"
                )
            }
        }
    }

    val gameViewModel: GameViewModel = viewModel(
        key = "game-$levelId",
        factory = factory
    )

    val uiState = gameViewModel.uiState
    val game = uiState.game
    val boosters = BoosterRepository().boosterItems()

    var rewardReported by remember(levelId) {
        mutableStateOf(false)
    }

    LaunchedEffect(game.status) {
        if (game.status == GameStatus.WON && !rewardReported) {
            rewardReported = true
            onRewardEarned(LEVEL_REWARD)
        }
    }

    val goalText = level.goals.joinToString("\n") { goal ->
        formatGoal(
            goal = goal,
            collected = game.collected,
            frozenBroken = game.frozenBroken,
            context = context
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GreenLight)
            .padding(16.dp)
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoBox(
                title = stringResource(R.string.level),
                value = level.id.toString(),
                modifier = Modifier.width(65.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            InfoBox(
                title = stringResource(R.string.goals),
                value = goalText,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            InfoBox(
                title = stringResource(R.string.moves),
                value = game.movesLeft.toString(),
                modifier = Modifier.width(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                userScrollEnabled = false
            ) {
                items(game.cells.indices.toList()) { index ->
                    val cell = game.cells[index]
                    val selected = uiState.selectedCell == index

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .then(
                                if (selected) {
                                    Modifier.border(
                                        width = 3.dp,
                                        color = OrangeDark,
                                        shape = CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(
                                enabled = cell != null &&
                                        game.status == GameStatus.PLAYING
                            ) {
                                gameViewModel.onCellTap(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.82f)
                                    .background(
                                        color = cellColor(cell.color),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cell.isFrozen) {
                                    Text(
                                        text = "❄",
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.dp, OrangeDark, RoundedCornerShape(16.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            boosters.forEach { booster ->
                BoosterButton(
                    icon = booster.icon,
                    contentDescription = booster.contentDescription,
                    onClick = {
                        if (game.status == GameStatus.PLAYING) {
                            gameViewModel.activateBooster(booster.type)
                            onBoosterClick(booster.type)
                        }
                    }
                )
            }
        }
    }

    if (game.status != GameStatus.PLAYING) {
        LevelResultDialog(
            status = game.status,
            reward = LEVEL_REWARD,
            onRetry = {
                rewardReported = false
                gameViewModel.restart()
            },
            onNextLevel = {
                if (levelId < LAST_LEVEL_ID) {
                    onNextLevel()
                } else {
                    onMapClick()
                }
            },
            onMap = onMapClick
        )
    }
}

private fun formatGoal(
    goal: LevelGoal,
    collected: Map<String, Int>,
    frozenBroken: Int,
    context: android.content.Context
): String {
    return when (goal.type) {
        "COLLECT" -> {
            val current = collected[goal.color] ?: 0
            val colorName = when (goal.color) {
                "RED" -> context.getString(R.string.color_red)
                "BLUE" -> context.getString(R.string.color_blue)
                "YELLOW" -> context.getString(R.string.color_yellow)
                "GREEN" -> context.getString(R.string.color_green)
                "PINK" -> context.getString(R.string.color_pink)
                else -> context.getString(R.string.chips)
            }

            context.getString(R.string.goal_progress, colorName, current, goal.target)
        }

        "BREAK_FROZEN" ->
            context.getString(R.string.ice_progress, frozenBroken, goal.target)

        else ->
            "${goal.type}: ${goal.target}"
    }
}