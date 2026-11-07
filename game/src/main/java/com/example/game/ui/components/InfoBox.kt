package com.example.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ui.theme.OrangeDark

@Composable
fun InfoBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isMultiLine = value.contains("\n") || value.length > 14
    val fontSize = if (isMultiLine) 11.sp else 16.sp
    val lineHeight = if (isMultiLine) 13.sp else 18.sp

    Column(
        modifier = modifier
            .heightIn(min = 65.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, OrangeDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = OrangeDark,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Text(
            text = value,
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = OrangeDark,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 4
        )
    }
}
