package com.example.mobiledevlabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mobiledevlabs.ui.screens.MapScreen

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    MapScreen(modifier = modifier)
}