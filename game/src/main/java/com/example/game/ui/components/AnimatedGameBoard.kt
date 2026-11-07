package com.example.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.model.GameCell
import com.example.game.data.model.SpecialType
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
    val scale = remember { Animatable(if (cell == null) 0.5f else 1f) }
    val offsetY = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "cellInfinite")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val bombPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bombPulse"
    )

    val airplaneRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "airplaneRotation"
    )

    val discoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "discoRotation"
    )

    LaunchedEffect(cell) {
        if (cell == null) {
            scale.animateTo(1.25f, tween(80))
            alpha.animateTo(0f, tween(120))
            scale.animateTo(0.2f, tween(120))
            displayedCell = null
        } else if (displayedCell != cell) {
            if (displayedCell != null) {
                scale.animateTo(1.2f, tween(60))
                alpha.animateTo(0f, tween(80))
            }

            displayedCell = cell
            alpha.snapTo(0f)
            scale.snapTo(0.6f)
            offsetY.snapTo(-50f)

            alpha.animateTo(1f, tween(150))
            scale.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
            offsetY.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
        }
    }

    val finalScale = when {
        selected -> scale.value * pulseScale
        displayedCell?.specialType == SpecialType.BOMB -> scale.value * bombPulse
        else -> scale.value
    }

    val finalRotation = when (displayedCell?.specialType) {
        SpecialType.AIRPLANE -> airplaneRotation
        SpecialType.DISCO_BALL -> discoRotation
        else -> 0f
    }

    val borderColor = when {
        selected -> OrangeDark
        displayedCell?.specialType == SpecialType.DISCO_BALL -> Color.Yellow
        displayedCell?.specialType == SpecialType.BOMB -> Color.Red
        displayedCell?.specialType == SpecialType.AIRPLANE -> Color.Blue
        else -> OrangeLight
    }

    val borderWidth = when {
        selected -> 3.5.dp
        displayedCell?.specialType != SpecialType.NONE -> 2.5.dp
        else -> 1.dp
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                translationY = offsetY.value
            }
            .alpha(alpha.value)
            .scale(finalScale)
            .rotate(finalRotation)
            .background(
                color = if (displayedCell == null) {
                    Color.White
                } else {
                    color
                },
                shape = CircleShape
            )
            .border(
                width = borderWidth,
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

                currentCell.specialType != SpecialType.NONE -> {
                    val symbol = when (currentCell.specialType) {
                        SpecialType.AIRPLANE -> "✈"
                        SpecialType.BOMB -> "💣"
                        SpecialType.DISCO_BALL -> "✦"
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
