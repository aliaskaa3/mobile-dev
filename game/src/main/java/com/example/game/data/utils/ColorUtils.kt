package com.example.game.data.utils

import androidx.compose.ui.graphics.Color
import com.example.game.data.model.CellColor
import com.example.game.ui.theme.GreenMedium

fun cellColor(color: CellColor): Color {
    return when (color) {
        CellColor.RED -> Color(0xFFE85D5D)
        CellColor.BLUE -> Color(0xFF5196E8)
        CellColor.YELLOW -> Color(0xFFFFD54F)
        CellColor.GREEN -> GreenMedium
        CellColor.PINK -> Color(0xFFE58AC8)
    }
}
