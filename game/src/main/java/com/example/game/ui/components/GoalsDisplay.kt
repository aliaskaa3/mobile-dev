package com.example.game.ui.components

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.model.LevelGoal
import com.example.game.data.utils.goalColor

@Composable
fun GoalsDisplay(
    goals: List<LevelGoal>,
    collected: Map<String, Int>,
    frozenBroken: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        goals.forEachIndexed { index, goal ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(10.dp))
            }
            GoalItem(
                goal = goal,
                collected = collected,
                frozenBroken = frozenBroken
            )
        }
    }
}

@Composable
fun GoalItem(
    goal: LevelGoal,
    collected: Map<String, Int>,
    frozenBroken: Int
) {
    val current = when (goal.type) {
        "COLLECT" -> collected[goal.color] ?: 0
        "BREAK_FROZEN" -> frozenBroken
        else -> 0
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (goal.type == "BREAK_FROZEN") {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❄",
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF29B6F6),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(goalColor(goal.color, MaterialTheme.colorScheme.primary), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "$current/${goal.target}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
