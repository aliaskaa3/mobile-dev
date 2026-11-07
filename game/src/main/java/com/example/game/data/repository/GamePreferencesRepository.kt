package com.example.game.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.game.data.model.BoosterType
import com.example.game.data.utils.LocaleHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

data class UserPreferences(
    val currentLevel: Int = 1,
    val coins: Int = 500,
    val lives: Int = 5,
    val lastRecoveryTimestamp: Long = 0L,
    val swapBoosters: Int = 3,
    val crossClearBoosters: Int = 3,
    val hammerBoosters: Int = 3,
    val language: String = "en"
)

class GamePreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val COINS = intPreferencesKey("coins")
        val LIVES = intPreferencesKey("lives")
        val LAST_RECOVERY_TIMESTAMP = longPreferencesKey("last_recovery_timestamp")
        val BOOSTER_SWAP = intPreferencesKey("booster_swap")
        val BOOSTER_CROSS_CLEAR = intPreferencesKey("booster_cross_clear")
        val BOOSTER_HAMMER = intPreferencesKey("booster_hammer")
        val LANGUAGE = stringPreferencesKey("language")
    }

    companion object {
        const val MAX_LIVES = 5
        const val RECOVERY_INTERVAL_MS = 3 * 60 * 1000L
        const val EXTRA_MOVES_COST = 100
        const val LIFE_COST = 200
        const val BOOSTER_COST = 100
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            currentLevel = prefs[PreferencesKeys.CURRENT_LEVEL] ?: 1,
            coins = prefs[PreferencesKeys.COINS] ?: 500,
            lives = prefs[PreferencesKeys.LIVES] ?: MAX_LIVES,
            lastRecoveryTimestamp = prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] ?: 0L,
            swapBoosters = prefs[PreferencesKeys.BOOSTER_SWAP] ?: 3,
            crossClearBoosters = prefs[PreferencesKeys.BOOSTER_CROSS_CLEAR] ?: 3,
            hammerBoosters = prefs[PreferencesKeys.BOOSTER_HAMMER] ?: 3,
            language = prefs[PreferencesKeys.LANGUAGE] ?: LocaleHelper.getLocale(context)
        )
    }

    suspend fun checkAndUpdateLifeRecovery(currentTimeMs: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            val currentLives = prefs[PreferencesKeys.LIVES] ?: MAX_LIVES
            val timestamp = prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] ?: 0L

            if (currentLives >= MAX_LIVES) {
                if (timestamp != 0L) {
                    prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] = 0L
                }
                if (currentLives > MAX_LIVES) {
                    prefs[PreferencesKeys.LIVES] = MAX_LIVES
                }
                return@edit
            }

            if (timestamp == 0L) {
                prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] = currentTimeMs
                return@edit
            }

            val elapsed = currentTimeMs - timestamp
            if (elapsed >= RECOVERY_INTERVAL_MS) {
                val livesToRecover = (elapsed / RECOVERY_INTERVAL_MS).toInt()
                val newLives = minOf(MAX_LIVES, currentLives + livesToRecover)
                if (newLives >= MAX_LIVES) {
                    prefs[PreferencesKeys.LIVES] = MAX_LIVES
                    prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] = 0L
                } else {
                    prefs[PreferencesKeys.LIVES] = newLives
                    prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] = timestamp + (livesToRecover * RECOVERY_INTERVAL_MS)
                }
            }
        }
    }

    fun getRemainingRecoveryTimeFormatted(
        lives: Int,
        lastRecoveryTimestamp: Long,
        currentTimeMs: Long = System.currentTimeMillis()
    ): String {
        if (lives >= MAX_LIVES || lastRecoveryTimestamp == 0L) {
            return "0:00"
        }
        val elapsed = currentTimeMs - lastRecoveryTimestamp
        val remainingMs = maxOf(0L, RECOVERY_INTERVAL_MS - (elapsed % RECOVERY_INTERVAL_MS))
        val remainingSeconds = remainingMs / 1000
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    suspend fun consumeLife(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        var consumed = false
        context.dataStore.edit { prefs ->
            val currentLives = prefs[PreferencesKeys.LIVES] ?: MAX_LIVES
            val timestamp = prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] ?: 0L

            if (currentLives > 0) {
                val newLives = currentLives - 1
                prefs[PreferencesKeys.LIVES] = newLives
                consumed = true
                if (currentLives == MAX_LIVES || timestamp == 0L) {
                    prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] = currentTimeMs
                }
            }
        }
        return consumed
    }

    suspend fun buyLife(cost: Int = LIFE_COST): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val coins = prefs[PreferencesKeys.COINS] ?: 500
            val currentLives = prefs[PreferencesKeys.LIVES] ?: MAX_LIVES

            if (coins >= cost && currentLives < MAX_LIVES) {
                val newLives = currentLives + 1
                prefs[PreferencesKeys.COINS] = coins - cost
                prefs[PreferencesKeys.LIVES] = newLives
                if (newLives >= MAX_LIVES) {
                    prefs[PreferencesKeys.LAST_RECOVERY_TIMESTAMP] = 0L
                }
                success = true
            }
        }
        return success
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { prefs ->
            val coins = prefs[PreferencesKeys.COINS] ?: 500
            prefs[PreferencesKeys.COINS] = coins + amount
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val coins = prefs[PreferencesKeys.COINS] ?: 500
            if (coins >= amount) {
                prefs[PreferencesKeys.COINS] = coins - amount
                success = true
            }
        }
        return success
    }

    suspend fun buyBooster(type: BoosterType, cost: Int = BOOSTER_COST): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val coins = prefs[PreferencesKeys.COINS] ?: 500
            if (coins >= cost) {
                prefs[PreferencesKeys.COINS] = coins - cost
                val key = when (type) {
                    BoosterType.SWAP -> PreferencesKeys.BOOSTER_SWAP
                    BoosterType.CROSS_CLEAR -> PreferencesKeys.BOOSTER_CROSS_CLEAR
                    BoosterType.HAMMER -> PreferencesKeys.BOOSTER_HAMMER
                }
                val count = prefs[key] ?: 3
                prefs[key] = count + 1
                success = true
            }
        }
        return success
    }

    suspend fun consumeBooster(type: BoosterType): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val key = when (type) {
                BoosterType.SWAP -> PreferencesKeys.BOOSTER_SWAP
                BoosterType.CROSS_CLEAR -> PreferencesKeys.BOOSTER_CROSS_CLEAR
                BoosterType.HAMMER -> PreferencesKeys.BOOSTER_HAMMER
            }
            val count = prefs[key] ?: 3
            if (count > 0) {
                prefs[key] = count - 1
                success = true
            }
        }
        return success
    }

    suspend fun unlockNextLevel(completedLevelId: Int) {
        context.dataStore.edit { prefs ->
            val currentLevel = prefs[PreferencesKeys.CURRENT_LEVEL] ?: 1
            if (completedLevelId >= currentLevel && currentLevel < 5) {
                prefs[PreferencesKeys.CURRENT_LEVEL] = completedLevelId + 1
            }
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LANGUAGE] = lang
        }
        LocaleHelper.setLocale(context, lang)
    }
}
