package com.example.game.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Pix
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.game.R
import com.example.game.data.model.Booster
import com.example.game.data.model.BoosterType

class BoosterRepository {
    @Composable
    fun boosterItems() = listOf(
        Booster(
            type = BoosterType.SWAP,
            icon = Icons.Default.SwapHoriz,
            contentDescription = stringResource(R.string.replace)
        ),
        Booster(
            type = BoosterType.CROSS_CLEAR,
            icon = Icons.Default.Pix,
            contentDescription = stringResource(R.string.cross_clear)
        ),
        Booster(
            type = BoosterType.HAMMER,
            icon = Icons.Default.Gavel,
            contentDescription = stringResource(R.string.hammer)
        )
    )
}
