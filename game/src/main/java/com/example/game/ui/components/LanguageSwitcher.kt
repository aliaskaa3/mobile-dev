package com.example.game.ui.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.repository.GamePreferencesRepository
import com.example.game.data.utils.LocaleHelper
import com.example.game.ui.theme.GreenDark
import com.example.game.ui.theme.GreenMedium
import kotlinx.coroutines.launch

@Composable
fun LanguageSwitcher() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = GamePreferencesRepository(context)
    val currentLanguage = LocaleHelper.getLocale(context)
    val targetLanguage = if (currentLanguage == "en") "ru" else "en"
    val targetLabel = targetLanguage.uppercase()

    OutlinedButton(
        onClick = {
            scope.launch {
                repository.setLanguage(targetLanguage)
                (context as? Activity)?.recreate()
            }
        },
        border = BorderStroke(2.dp, GreenDark),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GreenMedium),
    ) {
        Text(text = targetLabel, fontSize = 20.sp)
    }
}
