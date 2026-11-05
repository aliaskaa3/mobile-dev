package com.example.game.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Pix
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.game.R
import com.example.game.data.model.ShopItem

object ShopRepository {
    @Composable
    fun shopItems() = listOf(
        ShopItem(
            title = stringResource(R.string.replace),
            description = stringResource(R.string.swap_description),
            price = 100,
            icon = Icons.Default.SwapHoriz
        ),
        ShopItem(
            title = stringResource(R.string.cross_clear),
            description = stringResource(R.string.cross_clear_description),
            price = 100,
            icon = Icons.Default.Pix
        ),
        ShopItem(
            title = stringResource(R.string.hammer),
            description = stringResource(R.string.hammer_description),
            price = 100,
            icon = Icons.Default.Gavel
        ),
        ShopItem(
            title = stringResource(R.string.life),
            description = stringResource(R.string.life_description),
            price = 200,
            icon = Icons.Default.Favorite
        )
    )
}
