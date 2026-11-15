package com.example.no_definido.ui.viewmodel

import com.example.no_definido.data.FirebaseProgressRepository
import com.example.no_definido.data.LocalProgressDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MiniGameConsistencyTest {

    private lateinit var localDataSource: LocalProgressDataSource
    private lateinit var repository: FirebaseProgressRepository
    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    @Before
    fun setUp() {
        localDataSource = LocalProgressDataSource(sharedPreferences = null)
        localDataSource.clear()
        repository = FirebaseProgressRepository(
            localDataSource = localDataSource,
            firestoreProvider = { null }
        )
    }

    @Test
    fun allSixMiniGames_startAtRound1WithZeroStarsAnd3TotalRounds() {
        val appleVm = AppleBasketViewModel(repository, testScope)
        val houseVm = ActivityViewModel(repository, testScope)
        val clockVm = InteractiveClockViewModel(repository, testScope)
        val kitchenVm = KitchenSortingViewModel(repository, testScope)
        val trainVm = NumberTrainViewModel(repository, testScope)
        val gardenVm = ColorFlowerGardenViewModel(repository, testScope)

        assertEquals(3, appleVm.totalRounds)
        assertEquals(3, houseVm.totalRounds)
        assertEquals(3, clockVm.totalRounds)
        assertEquals(3, kitchenVm.totalRounds)
        assertEquals(3, trainVm.totalRounds)
        assertEquals(3, gardenVm.totalRounds)

        assertEquals(1, appleVm.currentRound.value)
        assertEquals(1, houseVm.currentRound.value)
        assertEquals(1, clockVm.currentRound.value)
        assertEquals(1, kitchenVm.currentRound.value)
        assertEquals(1, trainVm.currentRound.value)
        assertEquals(1, gardenVm.currentRound.value)

        assertEquals(0, appleVm.starsEarned.value)
        assertEquals(0, houseVm.starsEarned.value)
        assertEquals(0, clockVm.starsEarned.value)
        assertEquals(0, kitchenVm.starsEarned.value)
        assertEquals(0, trainVm.starsEarned.value)
        assertEquals(0, gardenVm.starsEarned.value)
    }

    @Test
    fun completingAllSixMiniGames_awards18TotalStarsAndPersistsOffline() = runBlocking {
        val userId = "calmecac_campeon"

        // 1. Apple Orchard (3 rounds: 3 + 5 + 7 = 15 apples)
        val appleVm = AppleBasketViewModel(repository, testScope)
        appleVm.setActiveUserId(userId)
        for (r in 1..3) {
            val slots = appleVm.uiState.value.treeApples
            slots.forEach { appleVm.dropApple(it.id) }
            if (r < 3) appleVm.nextRound()
        }
        assertTrue(appleVm.isCompleted.value)
        assertEquals(3, appleVm.starsEarned.value)
        appleVm.dismissCelebration()
        assertFalse(appleVm.celebrationActive.value)

        // 2. 4-Room House Sorting (3 rounds)
        val houseVm = ActivityViewModel(repository, testScope)
        houseVm.setActiveUserId(userId)
        for (r in 1..3) {
            repeat(houseVm.uiState.value.totalItemsInRound) {
                val item = houseVm.uiState.value.currentItem!!
                houseVm.onItemDroppedInRoom(item.targetRoom)
            }
            if (r < 3) houseVm.nextRound()
        }
        assertTrue(houseVm.isCompleted.value)
        assertEquals(3, houseVm.starsEarned.value)
        houseVm.dismissCelebration()
        assertFalse(houseVm.celebrationActive.value)

        // 3. Interactive Analog Clock (3 rounds)
        val clockVm = InteractiveClockViewModel(repository, testScope)
        clockVm.setActiveUserId(userId)
        for (r in 1..3) {
            val target = clockVm.uiState.value.targetHour
            clockVm.setHour(target)
            if (r < 3) clockVm.nextRound()
        }
        assertTrue(clockVm.isCompleted.value)
        assertEquals(3, clockVm.starsEarned.value)
        clockVm.dismissCelebration()
        assertFalse(clockVm.celebrationActive.value)

        // 4. Kitchen Sorting (3 rounds)
        val kitchenVm = KitchenSortingViewModel(repository, testScope)
        kitchenVm.setActiveUserId(userId)
        for (r in 1..3) {
            val items = kitchenVm.uiState.value.remainingTrayItems.toList()
            items.forEach { kitchenVm.onItemDropped(it.id, it.target) }
            if (r < 3) kitchenVm.nextRound()
        }
        assertTrue(kitchenVm.isCompleted.value)
        assertEquals(3, kitchenVm.starsEarned.value)
        kitchenVm.dismissCelebration()
        assertFalse(kitchenVm.celebrationActive.value)

        // 5. Number Counting Train (3 rounds)
        val trainVm = NumberTrainViewModel(repository, testScope)
        trainVm.setActiveUserId(userId)
        for (r in 1..3) {
            val targetCount = trainVm.uiState.value.targetCount
            repeat(targetCount) {
                trainVm.loadCargo()
            }
            if (r < 3) trainVm.nextRound()
        }
        assertTrue(trainVm.isCompleted.value)
        assertEquals(3, trainVm.starsEarned.value)
        trainVm.dismissCelebration()
        assertFalse(trainVm.celebrationActive.value)

        // 6. Color & Shape Flower Garden (3 rounds)
        val gardenVm = ColorFlowerGardenViewModel(repository, testScope)
        gardenVm.setActiveUserId(userId)
        for (r in 1..3) {
            val flowers = gardenVm.uiState.value.remainingFlowers.toList()
            flowers.forEach { gardenVm.matchFlowerToPot(it.id, it.id) }
            if (r < 3) gardenVm.nextRound()
        }
        assertTrue(gardenVm.isCompleted.value)
        assertEquals(3, gardenVm.starsEarned.value)
        gardenVm.dismissCelebration()
        assertFalse(gardenVm.celebrationActive.value)

        // Wait briefly for Dispatchers.IO coroutines to flush into LocalProgressDataSource
        Thread.sleep(150)

        val finalProgress = repository.getProgress(userId)
        assertNotNull(finalProgress)
        assertEquals(18, finalProgress?.totalStars)
        assertEquals(360, finalProgress?.totalScore)
        assertEquals(6, finalProgress?.objectGamesCompleted)
        assertEquals(15, finalProgress?.applesCollectedTotal)
    }
}
