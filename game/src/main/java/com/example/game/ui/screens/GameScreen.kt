package com.example.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.model.BoosterType
import com.example.game.data.model.CellColor
import com.example.game.data.model.GameCell
import com.example.game.data.repository.BoosterRepository
import com.example.game.data.utils.cellColor
import com.example.game.ui.components.BoosterButton
import com.example.game.ui.theme.GreenLight
import com.example.game.ui.theme.OrangeDark

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    levelId: Int = 1,
    levelGoal: String = "20 красных",
    onBoosterClick: (BoosterType) -> Unit = {}
) {
    val cells = remember {
        List(64) { index ->
            GameCell(
                color = CellColor.entries[index % CellColor.entries.size]
            )
        }
    }

    val boosterRepository = remember { BoosterRepository() }
    val boosters = boosterRepository.boosterItems

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
            Column(
                modifier = Modifier
                    .width(70.dp)
                    .height(65.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(2.dp, OrangeDark, RoundedCornerShape(12.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Уровень",
                    fontSize = 11.sp,
                    color = OrangeDark,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = levelId.toString(),
                    fontSize = 26.sp,
                    color = OrangeDark,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(65.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, OrangeDark, RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Цель",
                    fontSize = 11.sp,
                    color = OrangeDark,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = levelGoal,
                    fontSize = 26.sp,
                    color = OrangeDark,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .width(70.dp)
                    .height(65.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(2.dp, OrangeDark, RoundedCornerShape(12.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ходы",
                    fontSize = 11.sp,
                    color = OrangeDark,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "20",
                    fontSize = 26.sp,
                    color = OrangeDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(1f)
                .aspectRatio(1f)
                .background(Color.White, RoundedCornerShape(18.dp))
                .border(2.dp, OrangeDark, RoundedCornerShape(18.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false
            ) {
                items(cells) { cell ->
                    Box(
                        modifier = Modifier.aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.8f)
                                .clip(CircleShape)
                                .background(cellColor(cell.color))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
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
                    onClick = { onBoosterClick(booster.type) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}