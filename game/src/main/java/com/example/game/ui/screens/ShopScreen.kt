package com.example.game.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.R
import com.example.game.data.model.BoosterType
import com.example.game.data.repository.GamePreferencesRepository
import com.example.game.data.repository.ShopRepository
import com.example.game.data.repository.UserPreferences
import com.example.game.ui.components.ShopItemCard
import com.example.game.ui.theme.OrangeDark
import com.example.game.ui.theme.OrangeLight
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { GamePreferencesRepository(context) }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = UserPreferences())
    val scope = rememberCoroutineScope()
    val shopItems = ShopRepository.shopItems()

    val replaceTitle = stringResource(R.string.replace)
    val crossClearTitle = stringResource(R.string.cross_clear)
    val hammerTitle = stringResource(R.string.hammer)
    val lifeTitle = stringResource(R.string.life)

    val notEnoughCoinsMsg = stringResource(R.string.not_enough_coins_message)
    val maxLivesReachedMsg = stringResource(R.string.max_lives_reached)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = CircleShape,
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeLight),
                border = BorderStroke(2.dp, OrangeDark)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }

            Text(
                stringResource(R.string.shop_title),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeDark
            )

            Row(
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp)
                    .border(2.dp, OrangeDark, shape = RoundedCornerShape(12.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = stringResource(R.string.coin_description),
                    tint = Color.Yellow,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = userPrefs.coins.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeDark
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(shopItems) { shopItem ->
                val countText = when (shopItem.title) {
                    replaceTitle -> stringResource(R.string.owned_count, userPrefs.swapBoosters)
                    crossClearTitle -> stringResource(R.string.owned_count, userPrefs.crossClearBoosters)
                    hammerTitle -> stringResource(R.string.owned_count, userPrefs.hammerBoosters)
                    lifeTitle -> stringResource(R.string.max_lives_count, userPrefs.lives)
                    else -> null
                }

                ShopItemCard(
                    item = shopItem,
                    countText = countText,
                    onBuy = {
                        scope.launch {
                            when (shopItem.title) {
                                lifeTitle -> {
                                    if (userPrefs.lives >= GamePreferencesRepository.MAX_LIVES) {
                                        Toast.makeText(context, maxLivesReachedMsg, Toast.LENGTH_SHORT).show()
                                    } else {
                                        val success = repository.buyLife(shopItem.price)
                                        if (!success) {
                                            Toast.makeText(context, notEnoughCoinsMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                replaceTitle -> {
                                    val success = repository.buyBooster(BoosterType.SWAP, shopItem.price)
                                    if (!success) {
                                        Toast.makeText(context, notEnoughCoinsMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                crossClearTitle -> {
                                    val success = repository.buyBooster(BoosterType.CROSS_CLEAR, shopItem.price)
                                    if (!success) {
                                        Toast.makeText(context, notEnoughCoinsMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                hammerTitle -> {
                                    val success = repository.buyBooster(BoosterType.HAMMER, shopItem.price)
                                    if (!success) {
                                        Toast.makeText(context, notEnoughCoinsMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
