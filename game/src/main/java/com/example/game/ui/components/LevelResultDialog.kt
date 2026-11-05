package com.example.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.data.model.GameStatus
import com.example.game.R
import com.example.game.ui.theme.GreenMedium
import com.example.game.ui.theme.OrangeDark

@Composable
fun LevelResultDialog(
    status: GameStatus,
    reward: Int,
    onRetry: () -> Unit,
    onNextLevel: () -> Unit,
    onMap: () -> Unit,
    onBuyMoves: () -> Unit = {}
) {
    if (status == GameStatus.PLAYING) return

    val won = status == GameStatus.WON

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(0.82f)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(2.dp, OrangeDark, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = if (won) stringResource(R.string.result_win) else stringResource(R.string.result_loss),
                    color = OrangeDark,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal
                )

                if (won) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.result_reward),
                            color = OrangeDark,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White, shape = RoundedCornerShape(16.dp))
                                .border(2.dp, OrangeDark, shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = stringResource(R.string.coin_description),
                                tint = Color.Yellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.result_reward_amount, reward),
                                color = OrangeDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.heart_description),
                            tint = Color.Red,
                            modifier = Modifier.size(45.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = stringResource(R.string.result_lost_life),
                            color = OrangeDark,
                            fontSize = 38.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DialogIconButton(
                        icon = Icons.Default.Refresh,
                        onClick = onRetry
                    )

                    DialogIconButton(
                        icon = Icons.Default.Home,
                        onClick = onMap
                    )

                    if (won) {
                        DialogIconButton(
                            icon = Icons.Default.ArrowForward,
                            onClick = onNextLevel
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(width = 75.dp, height = 60.dp)
                                .background(GreenMedium, shape = RoundedCornerShape(16.dp))
                                .border(2.dp, OrangeDark, shape = RoundedCornerShape(16.dp))
                                .clickable { onBuyMoves() }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    stringResource(R.string.result_buy_moves),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = stringResource(R.string.coin_description),
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        stringResource(R.string.result_buy_moves_cost),
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogIconButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 75.dp, height = 60.dp)
            .background(GreenMedium, shape = RoundedCornerShape(14.dp))
            .border(2.dp, OrangeDark, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}
