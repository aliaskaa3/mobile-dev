package com.example.game.ui.components

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.game.data.utils.LocaleHelper
import com.example.game.ui.theme.GreenDark
import com.example.game.ui.theme.GreenMedium

@Composable
fun LanguageSwitcher() {
    val context = LocalContext.current
    val currentLanguage = LocaleHelper.getLocale(context)
    val targetLanguage = if (currentLanguage == "en") "ru" else "en"
    val targetLabel = targetLanguage.uppercase()

    OutlinedButton(
        onClick = {
            LocaleHelper.setLocale(context, targetLanguage)
            (context as? Activity)?.recreate()
        },
        border = BorderStroke(2.dp, GreenDark),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenMedium),
    ) {
        Text(text = targetLabel, fontSize = 20.sp)
    }
}
