package com.example.panicbutton

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PersistenceManager(private val settings: KmmSettings = KmmSettings()) {

    fun savePushCount(roomId: Int, count: Int) {
        settings.putInt("push_count_$roomId", count)
    }

    fun getPushCount(roomId: Int): Int {
        return settings.getInt("push_count_$roomId", 0)
    }

    fun saveLastResetTime(timestamp: Long) {
        settings.putLong("last_reset_time", timestamp)
    }

    fun getLastResetTime(): Long {
        return settings.getLong("last_reset_time", 0L)
    }

    fun saveHistory(history: List<HistoryEntry>) {
        val jsonString = Json.encodeToString(history)
        settings.putString("history_log", jsonString)
    }

    fun getHistory(): List<HistoryEntry> {
        val historyString = settings.getString("history_log") ?: return emptyList()
        return try {
            Json.decodeFromString(historyString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearCounts(roomCount: Int) {
        for (i in 1..roomCount) {
            settings.putInt("push_count_$i", 0)
        }
    }
}
