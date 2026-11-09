package com.example.game.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data class ShopItem(
    val title: String,
    val description: String,
    val price: Int,
    val icon: ImageVector
)