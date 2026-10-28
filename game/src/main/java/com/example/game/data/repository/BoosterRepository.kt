package com.example.game.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Pix
import androidx.compose.material.icons.filled.SwapHoriz
import com.example.game.data.model.Booster
import com.example.game.data.model.BoosterType

class BoosterRepository {
    val boosterItems = listOf(
        Booster(
            type = BoosterType.SWAP,
            icon = Icons.Default.SwapHoriz,
            contentDescription = "Перестановка"
        ),
        Booster(
            type = BoosterType.CROSS_CLEAR,
            icon = Icons.Default.Pix,
            contentDescription = "Перекрестный огонь"
        ),
        Booster(
            type = BoosterType.HAMMER,
            icon = Icons.Default.Gavel,
            contentDescription = "Молот"
        )
    )
}