package com.example.no_definido.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ColorFlowerGardenViewModelTest {

    private lateinit var viewModel: ColorFlowerGardenViewModel

    @Before
    fun setUp() {
        viewModel = ColorFlowerGardenViewModel()
    }

    @Test
    fun initialState_startsAtRound1WithThreeFlowersAndZeroStars() {
        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.totalRounds)
        assertEquals(3, state.totalFlowersInRound)
        assertEquals(3, state.remainingFlowers.size)
        assertTrue(state.matchedFlowers.isEmpty())
        assertEquals(0, state.starsEarned)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertEquals(1, viewModel.currentRound.value)
        assertEquals(0, viewModel.starsEarned.value)
        assertFalse(viewModel.isCompleted.value)
        assertFalse(viewModel.celebrationActive.value)
    }

    @Test
    fun matchFlowerToPot_correctMatch_movesFlowerToMatchedList() {
        val target = viewModel.uiState.value.remainingFlowers.first()
        val result = viewModel.matchFlowerToPot(target.id, target.id)

        assertTrue(result is FlowerMatchResult.Correct)
        val state = viewModel.uiState.value
        assertEquals(2, state.remainingFlowers.size)
        assertEquals(1, state.matchedFlowers.size)
        assertEquals(target.id, state.matchedFlowers.first().id)
        assertFalse(state.isRoundCompleted)
    }

    @Test
    fun matchFlowerToPot_incorrectMatch_returnsMismatchWithoutModifyingLists() {
        val remaining = viewModel.uiState.value.remainingFlowers
        val flower1 = remaining[0]
        val flower2 = remaining[1]

        val result = viewModel.matchFlowerToPot(flower1.id, flower2.id)

        assertTrue(result is FlowerMatchResult.Mismatch)
        val state = viewModel.uiState.value
        assertEquals(3, state.remainingFlowers.size)
        assertTrue(state.matchedFlowers.isEmpty())
        assertEquals(1, state.attempts)
    }

    @Test
    fun matchFlowerToPot_unknownFlowerOrPotIdReturnsIgnored() {
        val result = viewModel.matchFlowerToPot("non_existent_id", "red_circle")
        assertTrue(result is FlowerMatchResult.Ignored)
        assertEquals(0, viewModel.uiState.value.attempts)
    }

    @Test
    fun round1Completion_awardsOneStarAndMarksRoundCompleted() {
        val flowers = viewModel.uiState.value.remainingFlowers.toList()
        flowers.forEach { flower ->
            val res = viewModel.matchFlowerToPot(flower.id, flower.id)
            assertTrue(res is FlowerMatchResult.Correct)
        }

        val state = viewModel.uiState.value
        assertTrue(state.remainingFlowers.isEmpty())
        assertEquals(3, state.matchedFlowers.size)
        assertTrue(state.isRoundCompleted)
        assertEquals(1, state.starsEarned)
        assertEquals(1, viewModel.starsEarned.value)
        assertFalse(state.isCompleted)
    }

    @Test
    fun nextRound_advancesToRound2WithFiveFlowers() {
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        assertTrue(viewModel.uiState.value.isRoundCompleted)

        val advanced = viewModel.nextRound()
        assertTrue(advanced)

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertEquals(2, viewModel.currentRound.value)
        assertEquals(5, state.totalFlowersInRound)
        assertEquals(5, state.remainingFlowers.size)
        assertTrue(state.matchedFlowers.isEmpty())
        assertEquals(1, state.starsEarned)
        assertFalse(state.isRoundCompleted)
    }

    @Test
    fun round2Completion_awardsTwoStars() {
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()

        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }

        val state = viewModel.uiState.value
        assertEquals(5, state.matchedFlowers.size)
        assertTrue(state.isRoundCompleted)
        assertEquals(2, state.starsEarned)
        assertEquals(2, viewModel.starsEarned.value)
        assertFalse(state.isCompleted)
    }

    @Test
    fun nextRound_advancesToRound3WithEightFlowers() {
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()

        val state = viewModel.uiState.value
        assertEquals(3, state.currentRound)
        assertEquals(3, viewModel.currentRound.value)
        assertEquals(8, state.totalFlowersInRound)
        assertEquals(8, state.remainingFlowers.size)
        assertTrue(state.matchedFlowers.isEmpty())
        assertEquals(2, state.starsEarned)
    }

    @Test
    fun round3Completion_awardsThreeStarsAndTriggersCelebration() {
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }

        val state = viewModel.uiState.value
        assertEquals(8, state.matchedFlowers.size)
        assertTrue(state.isRoundCompleted)
        assertTrue(state.isCompleted)
        assertTrue(state.celebrationActive)
        assertEquals(3, state.starsEarned)
        assertTrue(viewModel.isCompleted.value)
        assertTrue(viewModel.celebrationActive.value)
    }

    @Test
    fun adversarial_matchesAfterGameCompletionReturnIgnored() {
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }

        val postResult = viewModel.matchFlowerToPot("red_circle", "red_circle")
        assertTrue(postResult is FlowerMatchResult.Ignored)
    }

    @Test
    fun adversarial_nextRoundBeyondRound3ReturnsFalse() {
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }

        val overflow = viewModel.nextRound()
        assertFalse(overflow)
        assertEquals(3, viewModel.uiState.value.currentRound)
    }

    @Test
    fun dismissCelebration_clearsCelebrationActiveFlag() {
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }

        assertTrue(viewModel.celebrationActive.value)
        viewModel.dismissCelebration()
        assertFalse(viewModel.celebrationActive.value)
        assertFalse(viewModel.uiState.value.celebrationActive)
    }

    @Test
    fun resetGame_restoresInitialRound1State() {
        viewModel.uiState.value.remainingFlowers.toList().forEach { viewModel.matchFlowerToPot(it.id, it.id) }
        viewModel.nextRound()
        assertEquals(2, viewModel.uiState.value.currentRound)

        viewModel.resetGame()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.totalFlowersInRound)
        assertEquals(3, state.remainingFlowers.size)
        assertTrue(state.matchedFlowers.isEmpty())
        assertEquals(0, state.starsEarned)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
    }
}
