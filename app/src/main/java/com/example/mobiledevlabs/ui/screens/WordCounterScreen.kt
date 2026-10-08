package com.example.mobiledevlabs.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobiledevlabs.domain.WordCounter
import com.example.mobiledevlabs.ui.theme.MobileDevLabsTheme

@Composable
fun WordCounterScreen(modifier: Modifier = Modifier) {
    val sourceText = WordCounter.sourceText
    var output by remember { mutableStateOf("")}

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = sourceText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Исходный текст") }
        )

        OutlinedTextField(
            value = output,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Результат") }
        )

        Button(
            onClick = {
                output = WordCounter.runCounter(sourceText)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Вывести количество повторений")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WordCounterScreenPreview() {
    MobileDevLabsTheme {
        WordCounterScreen()
    }
}