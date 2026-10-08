package com.example.mobiledevlabs.domain

import com.example.mobiledevlabs.data.ResultFormatter

object WordCounter {

    val sourceText = "Раз два, раз три два, пять, три, раз"

    fun count(text: String): Map<String, Int> {
        return text
            .lowercase()
            .split(Regex("[,\\s]+"))
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
    }
    fun runCounter(text: String): String {
        if (text.isBlank()) return "Ошибка: Текст отсутствует"
        return ResultFormatter.format(count(text))
    }
}