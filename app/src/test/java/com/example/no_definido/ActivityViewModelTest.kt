package com.example.no_definido

import com.example.no_definido.data.HouseRoomId
import com.example.no_definido.ui.viewmodel.ActivityViewModel
import com.example.no_definido.ui.viewmodel.DropResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ActivityViewModelTest {

    private lateinit var viewModel: ActivityViewModel

    @Before
    fun setUp() {
        viewModel = ActivityViewModel()
    }

    @Test
    fun initialState_startsAtRound1WithTwoRoomsAndZeroStars() {
        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.totalRounds)
        assertEquals(0, state.starsEarned)
        assertEquals(4, state.totalItemsInRound)
        assertEquals(0, state.currentItemIndex)
        assertNotNull(state.currentItem)
        assertEquals(setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM), state.activeRooms)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isActivityCompleted)
        assertFalse(state.celebrationActive)
    }

    @Test
    fun correctDrop_advancesItemIndexAndRecordsInRoom() {
        val currentItem = viewModel.uiState.value.currentItem
        assertNotNull(currentItem)

        val result = viewModel.onItemDroppedInRoom(currentItem!!.targetRoom)
        assertTrue(result is DropResult.Correct)

        val updatedState = viewModel.uiState.value
        assertEquals(1, updatedState.currentItemIndex)
        val placedList = updatedState.placedItemsByRoom[currentItem.targetRoom]
        assertNotNull(placedList)
        assertTrue(placedList!!.contains(currentItem.id))
    }

    @Test
    fun incorrectDrop_returnsIncorrectWithoutAdvancing() {
        val currentItem = viewModel.uiState.value.currentItem
        assertNotNull(currentItem)

        // Find a room that is not the target
        val wrongRoom = HouseRoomId.entries.first { it != currentItem!!.targetRoom }
        val result = viewModel.onItemDroppedInRoom(wrongRoom)
        assertTrue(result is DropResult.Incorrect)

        val updatedState = viewModel.uiState.value
        assertEquals(0, updatedState.currentItemIndex)
        assertEquals(1, updatedState.attempts)
    }

    @Test
    fun round1Completion_awardsOneStarAndMarksRoundCompleted() {
        val totalItems = viewModel.uiState.value.totalItemsInRound
        repeat(totalItems) {
            val item = viewModel.uiState.value.currentItem
            assertNotNull(item)
            viewModel.onItemDroppedInRoom(item!!.targetRoom)
        }

        val state = viewModel.uiState.value
        assertTrue(state.isRoundCompleted)
        assertEquals(1, state.starsEarned)
        assertFalse(state.isActivityCompleted)
    }

    @Test
    fun nextRound_advancesToRound2WithKitchenActive() {
        // Complete Round 1
        repeat(viewModel.uiState.value.totalItemsInRound) {
            val item = viewModel.uiState.value.currentItem
            viewModel.onItemDroppedInRoom(item!!.targetRoom)
        }

        val advanced = viewModel.nextRound()
        assertTrue(advanced)

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertEquals(1, state.starsEarned)
        assertEquals(6, state.totalItemsInRound)
        assertEquals(0, state.currentItemIndex)
        assertTrue(state.activeRooms.contains(HouseRoomId.KITCHEN))
        assertFalse(state.isRoundCompleted)
    }

    @Test
    fun round2Completion_awardsTwoStars() {
        // Round 1
        repeat(viewModel.uiState.value.totalItemsInRound) {
            val item = viewModel.uiState.value.currentItem
            viewModel.onItemDroppedInRoom(item!!.targetRoom)
        }
        viewModel.nextRound()

        // Round 2
        repeat(viewModel.uiState.value.totalItemsInRound) {
            val item = viewModel.uiState.value.currentItem
            viewModel.onItemDroppedInRoom(item!!.targetRoom)
        }

        val state = viewModel.uiState.value
        assertTrue(state.isRoundCompleted)
        assertEquals(2, state.starsEarned)
        assertFalse(state.isActivityCompleted)
    }

    @Test
    fun round3Completion_awardsThreeStarsAndTriggersCelebration() {
        // Round 1
        repeat(viewModel.uiState.value.totalItemsInRound) {
            val item = viewModel.uiState.value.currentItem
            viewModel.onItemDroppedInRoom(item!!.targetRoom)
        }
        viewModel.nextRound()

        // Round 2
        repeat(viewModel.uiState.value.totalItemsInRound) {
            val item = viewModel.uiState.value.currentItem
            viewModel.onItemDroppedInRoom(item!!.targetRoom)
        }
        viewModel.nextRound()

        val r3State = viewModel.uiState.value
        assertEquals(3, r3State.currentRound)
        assertEquals(8, r3State.totalItemsInRound)
        assertEquals(HouseRoomId.entries.toSet(), r3State.activeRooms)

        // Round 3
        repeat(viewModel.uiState.value.totalItemsInRound) {
            val item = viewModel.uiState.value.currentItem
            viewModel.onItemDroppedInRoom(item!!.targetRoom)
        }

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isRoundCompleted)
        assertTrue(finalState.isActivityCompleted)
        assertTrue(finalState.celebrationActive)
        assertEquals(3, finalState.starsEarned)
    }

    @Test
    fun resetGame_restoresToInitialRound1() {
        // Advance into Round 2
        repeat(viewModel.uiState.value.totalItemsInRound) {
            val item = viewModel.uiState.value.currentItem
            viewModel.onItemDroppedInRoom(item!!.targetRoom)
        }
        viewModel.nextRound()

        viewModel.resetGame()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(0, state.starsEarned)
        assertEquals(0, state.currentItemIndex)
        assertEquals(setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM), state.activeRooms)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isActivityCompleted)
    }
}
