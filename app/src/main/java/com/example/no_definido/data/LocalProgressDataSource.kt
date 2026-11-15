package com.example.no_definido.data

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

class LocalProgressDataSource(
    private val sharedPreferences: SharedPreferences? = defaultSharedPreferences
) {
    private val memoryCache = ConcurrentHashMap<String, UserProgress>()

    fun saveProgress(progress: UserProgress): Boolean {
        memoryCache[progress.userId] = progress
        val prefs = sharedPreferences ?: return true
        return try {
            prefs.edit()
                .putBoolean(key(progress.userId, "exists"), true)
                .putInt(key(progress.userId, "apples"), progress.applesCollectedTotal)
                .putInt(key(progress.userId, "games"), progress.objectGamesCompleted)
                .putInt(key(progress.userId, "score"), progress.totalScore)
                .putInt(key(progress.userId, "stars"), progress.totalStars)
                .putLong(key(progress.userId, "updated"), progress.lastUpdated)
                .apply()
            true
        } catch (_: Throwable) {
            true
        }
    }

    fun getProgress(userId: String): UserProgress? {
        memoryCache[userId]?.let { return it }
        val prefs = sharedPreferences ?: return null
        return try {
            if (!prefs.getBoolean(key(userId, "exists"), false)) {
                null
            } else {
                val loaded = UserProgress(
                    userId = userId,
                    applesCollectedTotal = prefs.getInt(key(userId, "apples"), 0),
                    objectGamesCompleted = prefs.getInt(key(userId, "games"), 0),
                    totalScore = prefs.getInt(key(userId, "score"), 0),
                    totalStars = prefs.getInt(key(userId, "stars"), 0),
                    lastUpdated = prefs.getLong(key(userId, "updated"), 0L)
                )
                memoryCache[userId] = loaded
                loaded
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun clear() {
        memoryCache.clear()
        try {
            sharedPreferences?.edit()?.clear()?.apply()
        } catch (_: Throwable) {
        }
    }

    private fun key(userId: String, field: String): String = "user_${userId}_$field"

    companion object {
        private const val PREFS_NAME = "calmecac_progress_prefs"

        @Volatile
        private var defaultSharedPreferences: SharedPreferences? = null

        @Volatile
        private var sharedInstance: LocalProgressDataSource? = null

        fun init(context: Context) {
            try {
                val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                defaultSharedPreferences = prefs
                sharedInstance = LocalProgressDataSource(prefs)
            } catch (_: Throwable) {
            }
        }

        fun getInstance(): LocalProgressDataSource {
            return sharedInstance ?: synchronized(this) {
                sharedInstance ?: LocalProgressDataSource(defaultSharedPreferences).also {
                    sharedInstance = it
                }
            }
        }
    }
}
