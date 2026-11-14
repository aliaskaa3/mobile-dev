package com.example.game.data.utils

import androidx.compose.ui.graphics.Color
import com.example.game.data.model.CellColor
import com.example.game.ui.theme.GreenMedium
import com.example.game.ui.theme.OrangeDark

fun cellColor(color: CellColor): Color {
    return when (color) {
        CellColor.RED -> Color(0xFFE85D5D)
        CellColor.BLUE -> Color(0xFF5196E8)
        CellColor.YELLOW -> Color(0xFFFFD54F)
        CellColor.GREEN -> GreenMedium
        CellColor.PINK -> Color(0xFFE58AC8)
    }
}

fun goalColor(colorString: String?): Color {
    return when (colorString?.uppercase()) {
        "RED" -> Color(0xFFE85D5D)
        "BLUE" -> Color(0xFF5196E8)
        "YELLOW" -> Color(0xFFFFD54F)
        "GREEN" -> GreenMedium
        "PINK" -> Color(0xFFE58AC8)
        "ICE", "FROZEN", "BREAK_FROZEN" -> Color(0xFF81D4FA)
        else -> OrangeDark
    }
}
