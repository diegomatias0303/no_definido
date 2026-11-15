package com.example.no_definido.data

import com.example.no_definido.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProgressRepositoryTest {

    private lateinit var localDataSource: LocalProgressDataSource
    private lateinit var repository: FirebaseProgressRepository

    @Before
    fun setUp() {
        localDataSource = LocalProgressDataSource(sharedPreferences = null)
        localDataSource.clear()
        repository = FirebaseProgressRepository(
            localDataSource = localDataSource,
            firestoreProvider = { null } // Simulates offline / JVM host without Firebase
        )
    }

    @Test
    fun localDataSource_savesAndRetrievesInMemoryOnJvm() {
        val progress = UserProgress(
            userId = "niño_mexica",
            applesCollectedTotal = 15,
            objectGamesCompleted = 2,
            totalScore = 120,
            totalStars = 6
        )

        val saved = localDataSource.saveProgress(progress)
        assertTrue(saved)

        val loaded = localDataSource.getProgress("niño_mexica")
        assertNotNull(loaded)
        assertEquals("niño_mexica", loaded?.userId)
        assertEquals(15, loaded?.applesCollectedTotal)
        assertEquals(2, loaded?.objectGamesCompleted)
        assertEquals(120, loaded?.totalScore)
        assertEquals(6, loaded?.totalStars)
    }

    @Test
    fun firebaseProgressRepository_savesLocallyWhenOfflineWithoutCrashing() = runBlocking {
        val progress = UserProgress(
            userId = "quetzal",
            applesCollectedTotal = 30,
            objectGamesCompleted = 4,
            totalScore = 240,
            totalStars = 12
        )

        val result = repository.saveProgress(progress)
        assertTrue(result)

        val retrieved = repository.getProgress("quetzal")
        assertNotNull(retrieved)
        assertEquals(12, retrieved?.totalStars)
        assertEquals(240, retrieved?.totalScore)
        assertEquals(4, retrieved?.objectGamesCompleted)
    }

    @Test
    fun progressRepository_incrementalSaveAccumulatesStarsAndScore() = runBlocking {
        val firstWin = repository.saveProgress(userId = "tlaloc", stars = 3, points = 60)
        assertTrue(firstWin)

        val secondWin = repository.saveProgress(userId = "tlaloc", stars = 3, points = 60)
        assertTrue(secondWin)

        val loaded = repository.getProgress("tlaloc")
        assertNotNull(loaded)
        assertEquals(6, loaded?.totalStars)
        assertEquals(120, loaded?.totalScore)
        assertEquals(2, loaded?.objectGamesCompleted)
    }

    @Test
    fun getProgress_returnsNullForUnknownUserWhenOffline() = runBlocking {
        val loaded = repository.getProgress("usuario_inexistente")
        assertNull(loaded)
    }

    @Test
    fun homeViewModel_fetchesRealStarsFromRepository() = runBlocking {
        repository.saveProgress(
            UserProgress(
                userId = "xochitl",
                applesCollectedTotal = 15,
                objectGamesCompleted = 3,
                totalScore = 180,
                totalStars = 9
            )
        )

        val homeViewModel = HomeViewModel(
            repository = repository,
            customScope = CoroutineScope(Dispatchers.Unconfined)
        )
        homeViewModel.setUserId("xochitl")

        val state = homeViewModel.userProgress.value
        assertNotNull(state)
        assertEquals("xochitl", state?.userId)
        assertEquals(9, state?.totalStars)
        assertEquals(180, state?.totalScore)
        assertEquals(15, state?.applesCollectedTotal)
    }
}
