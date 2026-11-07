package com.example.game.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.R
import com.example.game.data.model.BoosterType
import com.example.game.data.model.GameStatus
import com.example.game.data.model.LevelGoal
import com.example.game.data.repository.BoosterRepository
import com.example.game.data.repository.GamePreferencesRepository
import com.example.game.data.repository.LevelRepository
import com.example.game.data.repository.UserPreferences
import com.example.game.data.utils.cellColor
import com.example.game.ui.components.AnimatedGameBoard
import com.example.game.ui.components.BoosterButton
import com.example.game.ui.components.InfoBox
import com.example.game.ui.components.LevelResultDialog
import com.example.game.ui.theme.GreenLight
import com.example.game.ui.theme.OrangeDark
import kotlinx.coroutines.launch

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
    val repository = remember { GamePreferencesRepository(context) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = UserPreferences())
    val scope = rememberCoroutineScope()

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

    val noBoostersMsg = stringResource(R.string.no_boosters_message)
    val notEnoughCoinsMsg = stringResource(R.string.not_enough_coins_message)
    val movesBoughtMsg = stringResource(R.string.moves_bought)
    val noLivesMsg = stringResource(R.string.no_lives_message)

    LaunchedEffect(game.status) {
        if (game.status == GameStatus.WON && !rewardReported) {
            rewardReported = true
            repository.addCoins(LEVEL_REWARD)
            repository.unlockNextLevel(levelId)
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

        AnimatedGameBoard(
            cells = game.cells,
            rows = level.rows,
            columns = level.columns,
            selectedCell = uiState.selectedCell,
            onCellClick = { index ->
                gameViewModel.onCellTap(index) { usedBooster ->
                    scope.launch {
                        repository.consumeBooster(usedBooster)
                    }
                }
            },
            cellColor = { cell -> cellColor(cell.color) },
            enabled = game.status == GameStatus.PLAYING
        )

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
                val count = when (booster.type) {
                    BoosterType.SWAP -> userPrefs.swapBoosters
                    BoosterType.CROSS_CLEAR -> userPrefs.crossClearBoosters
                    BoosterType.HAMMER -> userPrefs.hammerBoosters
                }

                BoosterButton(
                    icon = booster.icon,
                    contentDescription = booster.contentDescription,
                    count = count,
                    onClick = {
                        if (game.status == GameStatus.PLAYING) {
                            if (count > 0) {
                                gameViewModel.activateBooster(booster.type)
                                onBoosterClick(booster.type)
                            } else {
                                Toast.makeText(context, noBoostersMsg, Toast.LENGTH_SHORT).show()
                            }
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
                scope.launch {
                    val remainingLives = userPrefs.lives
                    repository.consumeLife()
                    if (remainingLives > 1) {
                        rewardReported = false
                        gameViewModel.restart()
                    } else {
                        Toast.makeText(context, noLivesMsg, Toast.LENGTH_SHORT).show()
                        onMapClick()
                    }
                }
            },
            onNextLevel = {
                if (levelId < LAST_LEVEL_ID) {
                    onNextLevel()
                } else {
                    onMapClick()
                }
            },
            onMap = {
                scope.launch {
                    repository.consumeLife()
                    onMapClick()
                }
            },
            onBuyMoves = {
                scope.launch {
                    val success = repository.spendCoins(GamePreferencesRepository.EXTRA_MOVES_COST)
                    if (success) {
                        Toast.makeText(context, movesBoughtMsg, Toast.LENGTH_SHORT).show()
                        gameViewModel.addExtraMoves(5)
                    } else {
                        Toast.makeText(context, notEnoughCoinsMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

private fun formatGoal(
    goal: LevelGoal,
    collected: Map<String, Int>,
    frozenBroken: Int,
    context: Context
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
