package com.example.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.model.GameCell
import com.example.game.ui.theme.OrangeDark
import com.example.game.ui.theme.OrangeLight

@Composable
fun AnimatedGameBoard(
    cells: List<GameCell?>,
    rows: Int,
    columns: Int,
    selectedCell: Int,
    onCellClick: (Int) -> Unit,
    cellColor: (GameCell) -> Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(2.dp, OrangeDark, RoundedCornerShape(16.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(columns) { columnIndex ->
                    val index = rowIndex * columns + columnIndex
                    val cell = cells[index]
                    val selected = index == selectedCell

                    AnimatedGameCell(
                        cell = cell,
                        selected = selected,
                        enabled = enabled,
                        color = cell?.let(cellColor) ?: Color.Transparent,
                        onClick = { onCellClick(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedGameCell(
    cell: GameCell?,
    selected: Boolean,
    enabled: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedCell by remember { mutableStateOf(cell) }

    val alpha = remember { Animatable(if (cell == null) 0f else 1f) }
    val scale = remember { Animatable(if (cell == null) 0.7f else 1f) }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(cell) {
        if (cell == null) {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(120)
            )
            scale.animateTo(
                targetValue = 0.65f,
                animationSpec = tween(120)
            )
            displayedCell = null
        } else if (displayedCell != cell) {
            if (displayedCell != null) {
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(100)
                )
                scale.animateTo(
                    targetValue = 0.65f,
                    animationSpec = tween(100)
                )
            }

            displayedCell = cell
            alpha.snapTo(0f)
            scale.snapTo(0.8f)
            offsetY.snapTo(-48f)

            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(180)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(180)
            )
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(220)
            )
        }
    }

    val borderColor = if (selected) {
        OrangeDark
    } else {
        OrangeLight
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                translationY = offsetY.value
            }
            .alpha(alpha.value)
            .scale(scale.value)
            .background(
                color = if (displayedCell == null) {
                    Color.White
                } else {
                    color
                },
                shape = CircleShape
            )
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        displayedCell?.let { currentCell ->
            when {
                currentCell.isFrozen -> {
                    Text(
                        text = "❄",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                currentCell.specialType.name != "NONE" -> {
                    val symbol = when (currentCell.specialType.name) {
                        "AIRPLANE" -> "✈"
                        "BOMB" -> "✹"
                        "DISCO_BALL" -> "✦"
                        else -> ""
                    }

                    Text(
                        text = symbol,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}