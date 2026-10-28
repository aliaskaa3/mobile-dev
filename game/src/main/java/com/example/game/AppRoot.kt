package com.example.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.game.ui.screens.MapScreen
import com.example.game.ui.screens.ShopScreen

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    ShopScreen(modifier = modifier)
}