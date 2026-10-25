package com.example.mobiledevlabs.data

object ResultFormatter {

    fun format(result: Map<String, Int>): String =
        result.entries.joinToString("\n") {"Слово \"${it.key}\" повторяется ${it.value} раз"}
}