package com.example.no_definido.data

interface ProgressRepository {
    suspend fun saveProgress(progress: UserProgress): Boolean

    suspend fun saveProgress(userId: String, stars: Int, points: Int): Boolean {
        val current = getProgress(userId) ?: UserProgress(userId = userId)
        return saveProgress(
            current.copy(
                totalStars = current.totalStars + stars,
                totalScore = current.totalScore + points,
                objectGamesCompleted = current.objectGamesCompleted + 1,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun getProgress(userId: String): UserProgress?
}
