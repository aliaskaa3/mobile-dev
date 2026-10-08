package com.example.mobiledevlabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mobiledevlabs.ui.screens.WordCounterScreen
@Composable
fun AppRoot (modifier: Modifier = Modifier){
    WordCounterScreen(modifier = modifier)
}