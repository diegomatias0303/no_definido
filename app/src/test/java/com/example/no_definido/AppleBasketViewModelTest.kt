package com.example.no_definido

import com.example.no_definido.ui.viewmodel.AppleBasketViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppleBasketViewModelTest {

    private lateinit var viewModel: AppleBasketViewModel

    @Before
    fun setUp() {
        viewModel = AppleBasketViewModel()
    }

    @Test
    fun initialState_startsAtRound1WithThreeApplesAndZeroStars() {
        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.totalRounds)
        assertEquals(3, state.targetCount)
        assertEquals(0, state.currentCount)
        assertEquals(0, state.starsEarned)
        assertEquals(3, state.treeApples.size)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
    }

    @Test
    fun dropApple_collectsAppleAndIncrementsCount() {
        val appleId = viewModel.uiState.value.treeApples.first().id
        val newCount = viewModel.dropApple(appleId)

        assertEquals(1, newCount)
        assertEquals(1, viewModel.uiState.value.currentCount)
        assertTrue(viewModel.uiState.value.treeApples.first { it.id == appleId }.isCollected)
    }

    @Test
    fun dropApple_duplicateOrInvalidIdReturnsNull() {
        val appleId = viewModel.uiState.value.treeApples.first().id
        viewModel.dropApple(appleId)

        val duplicateResult = viewModel.dropApple(appleId)
        assertNull(duplicateResult)

        val invalidResult = viewModel.dropApple(999)
        assertNull(invalidResult)
    }

    @Test
    fun round1Completion_awardsOneStarAndMarksRoundCompleted() {
        val apples = viewModel.uiState.value.treeApples
        apples.forEach { viewModel.dropApple(it.id) }

        val state = viewModel.uiState.value
        assertEquals(3, state.currentCount)
        assertTrue(state.isRoundCompleted)
        assertEquals(1, state.starsEarned)
        assertFalse(state.isCompleted)
    }

    @Test
    fun nextRound_advancesToRound2WithFiveApples() {
        // Complete Round 1
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }
        assertTrue(viewModel.uiState.value.isRoundCompleted)

        val advanced = viewModel.nextRound()
        assertTrue(advanced)

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertEquals(5, state.targetCount)
        assertEquals(0, state.currentCount)
        assertEquals(1, state.starsEarned)
        assertEquals(5, state.treeApples.size)
        assertFalse(state.isRoundCompleted)
    }

    @Test
    fun round2Completion_awardsTwoStars() {
        // Round 1
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }
        viewModel.nextRound()

        // Round 2
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }

        val state = viewModel.uiState.value
        assertEquals(5, state.currentCount)
        assertTrue(state.isRoundCompleted)
        assertEquals(2, state.starsEarned)
        assertFalse(state.isCompleted)
    }

    @Test
    fun round3Completion_awardsThreeStarsAndTriggersCelebration() {
        // Round 1
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }
        viewModel.nextRound()

        // Round 2
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }
        viewModel.nextRound()

        val r3State = viewModel.uiState.value
        assertEquals(3, r3State.currentRound)
        assertEquals(7, r3State.targetCount)
        assertEquals(7, r3State.treeApples.size)

        // Round 3
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }

        val finalState = viewModel.uiState.value
        assertEquals(7, finalState.currentCount)
        assertTrue(finalState.isRoundCompleted)
        assertTrue(finalState.isCompleted)
        assertTrue(finalState.celebrationActive)
        assertEquals(3, finalState.starsEarned)
    }

    @Test
    fun resetGame_restoresToInitialRound1() {
        // Progress into Round 2
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }
        viewModel.nextRound()

        viewModel.resetGame()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.targetCount)
        assertEquals(0, state.currentCount)
        assertEquals(0, state.starsEarned)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
    }

    @Test
    fun treeAppleSlots_allRounds_maintainMinimumDistanceOfPoint12() {
        for (round in 1..3) {
            val apples = viewModel.uiState.value.treeApples
            assertTrue("Round $round should have apples", apples.isNotEmpty())
            for (i in apples.indices) {
                for (j in i + 1 until apples.size) {
                    val a1 = apples[i]
                    val a2 = apples[j]
                    val dx = a1.relX - a2.relX
                    val dy = a1.relY - a2.relY
                    val distance = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    assertTrue(
                        "Round $round slots ${a1.id} and ${a2.id} distance $distance should be >= 0.12f",
                        distance >= 0.12f
                    )
                }
            }
            if (round < 3) {
                // Collect apples to advance
                viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }
                viewModel.nextRound()
            }
        }
    }

    @Test
    fun round3_apple4_hasTargetCoordinates() {
        // Advance to Round 3
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.treeApples.forEach { viewModel.dropApple(it.id) }
        viewModel.nextRound()

        val r3Apples = viewModel.uiState.value.treeApples
        val apple4 = r3Apples.find { it.id == 4 }
        assertNotNull("Apple 4 must exist in Round 3", apple4)
        assertEquals(0.46f, apple4!!.relX, 0.001f)
        assertEquals(0.33f, apple4.relY, 0.001f)
    }
}

