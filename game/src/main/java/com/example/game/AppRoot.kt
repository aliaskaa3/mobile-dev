package com.example.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.game.ui.screens.GameScreen
import com.example.game.ui.screens.MapScreen
import com.example.game.ui.screens.ShopScreen

object Routes {
    const val MAP = "map"
    const val SHOP = "shop"
    const val GAME = "game/{levelId}"

    fun game(levelId: Int): String = "game/$levelId"
}

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAP,
        modifier = modifier
    ) {
        composable(Routes.MAP) {
            MapScreen(
                onShopClick = {
                    navController.navigate(Routes.SHOP)
                },
                onLevelClick = { levelId ->
                    navController.navigate(Routes.game(levelId))
                }
            )
        }

        composable(Routes.SHOP) {
            ShopScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument("levelId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments
                ?.getInt("levelId") ?: 1

            GameScreen(
                levelId = levelId
            )
        }
    }
}
