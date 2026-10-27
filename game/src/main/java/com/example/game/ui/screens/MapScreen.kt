package com.example.mobiledevlabs.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MapScreen(modifier: Modifier) {
    val coinsCount = 500
    val livesCount = 5
    val timeToRestore = "0:00"
    val context = LocalContext.current
    val currentLevel = 3

    Column(
        modifier = Modifier
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
            Row(
                modifier = Modifier
                    .width(170.dp)
                    .height(70.dp)
                    .border(2.dp, Color.Green, shape = RoundedCornerShape(12.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Монета",
                    tint = Color.Yellow,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = coinsCount.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
            }

            Row(
                modifier = Modifier
                    .width(170.dp)
                    .height(70.dp)
                    .border(2.dp, Color.Green, shape = RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Сердце",
                        tint = Color.Red,
                        modifier = Modifier.size(45.dp)
                    )
                    Text(
                        "  $livesCount",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    timeToRestore,
                    fontSize = 20.sp,
                    color = Color.Green,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {},
                border = BorderStroke(2.dp, Color.Green),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Магазин",
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }

            OutlinedButton(
                onClick = {},
                border = BorderStroke(2.dp, Color.Green),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            ) {
                Text(
                    "EN",
                    fontSize = 20.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            (5 downTo 1).forEach { level ->
                val buttonColor = when {
                    level < currentLevel -> Color.Green
                    level == currentLevel -> Color.Yellow
                    else -> Color.LightGray
                }
                val offset = if (level % 2 == 0) 80.dp else (-80).dp

                OutlinedButton(
                    onClick = {
                        if (level <= currentLevel) {
                        } else {
                            Toast.makeText(
                                context,
                                "Уровень недоступен",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    border = BorderStroke(2.dp, color = buttonColor),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = offset)
                        .padding(4.dp)
                ) {
                    Text(
                        level.toString(),
                        color = Color.White,
                        fontSize = 40.sp
                    )
                }
            }
        }

        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            border = BorderStroke(2.dp, Color.Green),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                "Начать игру >",
                fontSize = 25.sp
            )
        }

    }
}