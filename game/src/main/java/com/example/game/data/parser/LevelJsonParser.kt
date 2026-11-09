package com.example.game.data.parser

import com.example.game.data.model.LevelConfig
import com.example.game.data.model.LevelGoal
import org.json.JSONException
import org.json.JSONObject

class LevelJsonParser {

    fun parse(json: String): LevelConfig {
        try {
            val root = JSONObject(json)

            val goalsJson = root.getJSONArray("goals")
            val goals = buildList {
                for (i in 0 until goalsJson.length()) {
                    val goalJson = goalsJson.getJSONObject(i)

                    val color = if (
                        goalJson.isNull("color") ||
                        !goalJson.has("color")
                    ) {
                        null
                    } else {
                        goalJson.getString("color")
                    }

                    add(
                        LevelGoal(
                            type = goalJson.getString("type"),
                            color = color,
                            target = goalJson.getInt("target")
                        )
                    )
                }
            }

            val frozenJson = root.optJSONArray("frozenCells")
            val frozenCells = if (frozenJson == null) {
                emptyList()
            } else {
                buildList {
                    for (i in 0 until frozenJson.length()) {
                        add(frozenJson.getInt(i))
                    }
                }
            }

            val config = LevelConfig(
                id = root.getInt("id"),
                rows = root.getInt("rows"),
                columns = root.getInt("columns"),
                moves = root.getInt("moves"),
                shape = root.optString("shape", "FULL"),
                goals = goals,
                frozenCells = frozenCells
            )

            validate(config)
            return config
        } catch (exception: JSONException) {
            throw IllegalArgumentException(
                "Не удалось разобрать JSON уровня: ${exception.message}",
                exception
            )
        }
    }

    private fun validate(config: LevelConfig) {
        require(config.id > 0) {
            "ID уровня должен быть положительным"
        }
        require(config.rows > 0 && config.columns > 0) {
            "Количество строк и столбцов должно быть положительным"
        }
        require(config.moves > 0) {
            "Количество ходов должно быть положительным"
        }
        require(config.goals.isNotEmpty()) {
            "У уровня должна быть хотя бы одна цель"
        }
        require(config.goals.all { it.target > 0 }) {
            "Целевое количество должно быть положительным"
        }

        val cellCount = config.rows * config.columns
        require(config.frozenCells.all { it in 0 until cellCount }) {
            "Индекс замороженной клетки выходит за пределы поля"
        }
    }
}