package com.example.no_definido.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InteractiveClockViewModelTest {

    private lateinit var viewModel: InteractiveClockViewModel

    @Before
    fun setUp() {
        viewModel = InteractiveClockViewModel()
    }

    @Test
    fun initialState_startsAtRound1WithMorningRoutineAndZeroStars() {
        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.totalRounds)
        assertEquals(8, state.targetHour)
        assertEquals(12, state.selectedHour)
        assertEquals(0, state.starsEarned)
        assertEquals(ClockTimeOfDay.MORNING, state.timeOfDay)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertEquals(0, viewModel.starsEarned.value)
        assertEquals(1, viewModel.currentRound.value)
        assertFalse(viewModel.isCompleted.value)
        assertFalse(viewModel.celebrationActive.value)
        assertNotNull(state.currentChallenge)
        assertEquals("milk", state.currentChallenge.visualItemId)
    }

    @Test
    fun setHour_incorrectHour_updatesSelectedHourAndSkyWithoutCompletingRound() {
        val matched = viewModel.setHour(3)
        assertFalse(matched)

        val state = viewModel.uiState.value
        assertEquals(3, state.selectedHour)
        assertEquals(ClockTimeOfDay.AFTERNOON, state.timeOfDay)
        assertFalse(state.isRoundCompleted)
        assertEquals(0, state.starsEarned)
        assertTrue(state.attempts >= 1)
    }

    @Test
    fun setHour_normalizesHoursBeyond12AndBelow1() {
        viewModel.setHour(13)
        assertEquals(1, viewModel.uiState.value.selectedHour)

        viewModel.setHour(0)
        assertEquals(12, viewModel.uiState.value.selectedHour)

        viewModel.setHour(-1)
        assertEquals(11, viewModel.uiState.value.selectedHour)
    }

    @Test
    fun verifyRoutine_incorrectHour_returnsFalseAndTracksAttempts() {
        viewModel.setHour(4)
        val match = viewModel.verifyRoutine()
        assertFalse(match)

        val state = viewModel.uiState.value
        assertFalse(state.isRoundCompleted)
        assertEquals(0, state.starsEarned)
        assertTrue(state.attempts >= 1)
    }

    @Test
    fun setHour_correctHour_completesRound1AndAwardsOneStar() {
        val matched = viewModel.setHour(8)
        assertTrue(matched)

        val state = viewModel.uiState.value
        assertEquals(8, state.selectedHour)
        assertTrue(state.isRoundCompleted)
        assertEquals(1, state.starsEarned)
        assertEquals(1, viewModel.starsEarned.value)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertEquals(25, state.score)
    }

    @Test
    fun verifyRoutine_whenCorrectHourAlreadySet_returnsTrue() {
        viewModel.setHour(8)
        val verified = viewModel.verifyRoutine()
        assertTrue(verified)
        assertTrue(viewModel.uiState.value.isRoundCompleted)
    }

    @Test
    fun nextRound_advancesToRound2WithNoonTargetAndMaintainsStars() {
        viewModel.setHour(8)
        assertTrue(viewModel.uiState.value.isRoundCompleted)

        val advanced = viewModel.nextRound()
        assertTrue(advanced)

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertEquals(2, viewModel.currentRound.value)
        assertEquals(12, state.targetHour)
        assertEquals(1, state.starsEarned)
        assertEquals(ClockTimeOfDay.NOON, state.timeOfDay)
        assertEquals(12, state.selectedHour)
        assertFalse(state.isRoundCompleted)
        assertEquals("plate", state.currentChallenge.visualItemId)
    }

    @Test
    fun round2Completion_awardsTwoStars() {
        viewModel.setHour(8)
        viewModel.nextRound()

        val matched = viewModel.setHour(12)
        assertTrue(matched)

        val state = viewModel.uiState.value
        assertTrue(state.isRoundCompleted)
        assertEquals(2, state.starsEarned)
        assertEquals(2, viewModel.starsEarned.value)
        assertFalse(state.isCompleted)
        assertEquals(50, state.score)
    }

    @Test
    fun round3Completion_awardsThreeStarsAndTriggersCelebration() {
        // Round 1
        viewModel.setHour(8)
        viewModel.nextRound()

        // Round 2
        viewModel.setHour(12)
        viewModel.nextRound()

        val r3State = viewModel.uiState.value
        assertEquals(3, r3State.currentRound)
        assertEquals(8, r3State.targetHour)
        assertEquals(ClockTimeOfDay.NIGHT, r3State.timeOfDay)
        assertEquals("pillow", r3State.currentChallenge.visualItemId)

        // Round 3
        val matched = viewModel.setHour(8)
        assertTrue(matched)

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isRoundCompleted)
        assertTrue(finalState.isCompleted)
        assertTrue(finalState.celebrationActive)
        assertEquals(3, finalState.starsEarned)
        assertTrue(viewModel.isCompleted.value)
        assertTrue(viewModel.celebrationActive.value)
        assertEquals(75, finalState.score)
    }

    @Test
    fun adversarial_nextRoundBeyondRound3ReturnsFalse() {
        // Complete Round 1
        viewModel.setHour(8)
        viewModel.nextRound()

        // Complete Round 2
        viewModel.setHour(12)
        viewModel.nextRound()

        // Complete Round 3
        viewModel.setHour(8)

        val overflow = viewModel.nextRound()
        assertFalse(overflow)
        assertEquals(3, viewModel.uiState.value.currentRound)
    }

    @Test
    fun dismissCelebration_clearsCelebrationActive() {
        viewModel.setHour(8)
        viewModel.nextRound()
        viewModel.setHour(12)
        viewModel.nextRound()
        viewModel.setHour(8)

        assertTrue(viewModel.celebrationActive.value)

        viewModel.dismissCelebration()
        assertFalse(viewModel.celebrationActive.value)
        assertFalse(viewModel.uiState.value.celebrationActive)
    }

    @Test
    fun resetGame_restoresInitialRound1State() {
        viewModel.setHour(8)
        viewModel.nextRound()
        assertEquals(2, viewModel.uiState.value.currentRound)
        assertEquals(1, viewModel.uiState.value.starsEarned)

        viewModel.resetGame()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(8, state.targetHour)
        assertEquals(12, state.selectedHour)
        assertEquals(0, state.starsEarned)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertEquals(0, state.score)
    }

    @Test
    fun timeOfDayMapping_coversAllFiveTimesOfDay() {
        assertEquals(ClockTimeOfDay.MORNING, ClockTimeOfDay.fromHour(8))
        assertEquals(ClockTimeOfDay.NOON, ClockTimeOfDay.fromHour(12))
        assertEquals(ClockTimeOfDay.AFTERNOON, ClockTimeOfDay.fromHour(3))
        assertEquals(ClockTimeOfDay.SUNSET, ClockTimeOfDay.fromHour(6))
        assertEquals(ClockTimeOfDay.NIGHT, ClockTimeOfDay.fromHour(9))
    }
}
