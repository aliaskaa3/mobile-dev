package com.example.game.data.repository

import android.content.Context
import com.example.game.data.model.LevelConfig
import com.example.game.data.parser.LevelJsonParser

class LevelRepository(
    context: Context,
    private val parser: LevelJsonParser = LevelJsonParser()
) {
    private val appContext = context.applicationContext

    fun getLevel(levelId: Int): LevelConfig {
        require(levelId > 0) {
            "ID уровня должен быть положительным"
        }

        val fileName = "levels/level_$levelId.json"

        val json = appContext.assets
            .open(fileName)
            .bufferedReader()
            .use { reader -> reader.readText() }

        val level = parser.parse(json)

        require(level.id == levelId) {
            "ID в файле $fileName не совпадает с запрошенным уровнем $levelId"
        }

        return level
    }
}