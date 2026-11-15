package com.example.no_definido.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NumberTrainViewModelTest {

    private lateinit var viewModel: NumberTrainViewModel

    @Before
    fun setUp() {
        viewModel = NumberTrainViewModel()
    }

    @Test
    fun initialState_startsAtRound1WithThreeItemsAndZeroStars() {
        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.totalRounds)
        assertEquals(3, state.targetCount)
        assertEquals(0, state.loadedCount)
        assertEquals(0, state.starsEarned)
        assertEquals("corn", state.currentCargoItem.id)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertEquals(1, viewModel.currentRound.value)
        assertEquals(0, viewModel.starsEarned.value)
        assertFalse(viewModel.isCompleted.value)
        assertFalse(viewModel.celebrationActive.value)
    }

    @Test
    fun loadCargo_incrementsLoadedCountAndReturnsLoadedResult() {
        val result1 = viewModel.loadCargo()
        assertTrue(result1 is TrainDropResult.Loaded)
        val loaded1 = result1 as TrainDropResult.Loaded
        assertEquals(1, loaded1.count)
        assertEquals("¡Uno!", loaded1.countSpokenWord)
        assertEquals(1, viewModel.uiState.value.loadedCount)
        assertFalse(viewModel.uiState.value.isRoundCompleted)

        val result2 = viewModel.loadCargo()
        assertTrue(result2 is TrainDropResult.Loaded)
        val loaded2 = result2 as TrainDropResult.Loaded
        assertEquals(2, loaded2.count)
        assertEquals("¡Dos!", loaded2.countSpokenWord)
        assertEquals(2, viewModel.uiState.value.loadedCount)
    }

    @Test
    fun loadCargo_round1Completion_awardsOneStarAndMarksRoundCompleted() {
        repeat(3) { viewModel.loadCargo() }

        val state = viewModel.uiState.value
        assertEquals(3, state.loadedCount)
        assertTrue(state.isRoundCompleted)
        assertEquals(1, state.starsEarned)
        assertEquals(1, viewModel.starsEarned.value)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
    }

    @Test
    fun loadCargo_overflowDropsBeyondTargetCountReturnIgnored() {
        repeat(3) { viewModel.loadCargo() }
        assertEquals(3, viewModel.uiState.value.loadedCount)

        val overflowResult = viewModel.loadCargo()
        assertTrue(overflowResult is TrainDropResult.Ignored)
        assertEquals(3, viewModel.uiState.value.loadedCount)
    }

    @Test
    fun nextRound_advancesToRound2WithFiveItemsAndPreservesStars() {
        repeat(3) { viewModel.loadCargo() }
        assertTrue(viewModel.uiState.value.isRoundCompleted)

        val advanced = viewModel.nextRound()
        assertTrue(advanced)

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertEquals(2, viewModel.currentRound.value)
        assertEquals(5, state.targetCount)
        assertEquals(0, state.loadedCount)
        assertEquals(1, state.starsEarned)
        assertEquals("clay_pot", state.currentCargoItem.id)
        assertFalse(state.isRoundCompleted)
    }

    @Test
    fun round2Completion_awardsTwoStars() {
        repeat(3) { viewModel.loadCargo() }
        viewModel.nextRound()

        repeat(5) { viewModel.loadCargo() }

        val state = viewModel.uiState.value
        assertEquals(5, state.loadedCount)
        assertTrue(state.isRoundCompleted)
        assertEquals(2, state.starsEarned)
        assertEquals(2, viewModel.starsEarned.value)
        assertFalse(state.isCompleted)
    }

    @Test
    fun nextRound_advancesToRound3WithEightItems() {
        repeat(3) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(5) { viewModel.loadCargo() }
        viewModel.nextRound()

        val state = viewModel.uiState.value
        assertEquals(3, state.currentRound)
        assertEquals(3, viewModel.currentRound.value)
        assertEquals(8, state.targetCount)
        assertEquals(0, state.loadedCount)
        assertEquals(2, state.starsEarned)
        assertEquals("jade_stone", state.currentCargoItem.id)
    }

    @Test
    fun round3Completion_awardsThreeStarsAndTriggersCelebration() {
        repeat(3) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(5) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(8) { viewModel.loadCargo() }

        val state = viewModel.uiState.value
        assertEquals(8, state.loadedCount)
        assertTrue(state.isRoundCompleted)
        assertTrue(state.isCompleted)
        assertTrue(state.celebrationActive)
        assertEquals(3, state.starsEarned)
        assertTrue(viewModel.isCompleted.value)
        assertTrue(viewModel.celebrationActive.value)
    }

    @Test
    fun adversarial_nextRoundBeyondRound3ReturnsFalse() {
        repeat(3) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(5) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(8) { viewModel.loadCargo() }

        val overflow = viewModel.nextRound()
        assertFalse(overflow)
        assertEquals(3, viewModel.uiState.value.currentRound)
    }

    @Test
    fun adversarial_dropsAfterGameCompletionReturnIgnored() {
        repeat(3) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(5) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(8) { viewModel.loadCargo() }

        val postResult = viewModel.loadCargo()
        assertTrue(postResult is TrainDropResult.Ignored)
    }

    @Test
    fun dismissCelebration_clearsCelebrationActiveFlag() {
        repeat(3) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(5) { viewModel.loadCargo() }
        viewModel.nextRound()
        repeat(8) { viewModel.loadCargo() }

        assertTrue(viewModel.celebrationActive.value)
        viewModel.dismissCelebration()
        assertFalse(viewModel.celebrationActive.value)
        assertFalse(viewModel.uiState.value.celebrationActive)
    }

    @Test
    fun resetGame_restoresInitialRound1State() {
        repeat(3) { viewModel.loadCargo() }
        viewModel.nextRound()
        assertEquals(2, viewModel.uiState.value.currentRound)

        viewModel.resetGame()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.targetCount)
        assertEquals(0, state.loadedCount)
        assertEquals(0, state.starsEarned)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
    }
}
