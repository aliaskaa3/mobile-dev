package com.example.game.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Pix
import androidx.compose.material.icons.filled.SwapHoriz
import com.example.game.data.model.ShopItem

object ShopRepository {
    val shopItems = listOf(
        ShopItem(
            title = "Перестановка",
            description = "Меняет местами соседние фигурки",
            price = 100,
            icon = Icons.Default.SwapHoriz
        ),
        ShopItem(
            title = "Перекрестный огонь",
            description = "Уничтожает все фигурки в строке и столбце",
            price = 100,
            icon = Icons.Default.Pix
        ),
        ShopItem(
            title = "Молот",
            description = "Уничтожает одну выбранную фигурку",
            price = 100,
            icon = Icons.Default.Gavel
        ),
        ShopItem(
            title = "Жизнь",
            description = "Прибавляет одну жизнь",
            price = 200,
            icon = Icons.Default.Favorite
        )
    )
}
