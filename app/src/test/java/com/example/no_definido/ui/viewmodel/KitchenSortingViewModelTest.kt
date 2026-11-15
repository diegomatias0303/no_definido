package com.example.no_definido.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KitchenSortingViewModelTest {

    private lateinit var viewModel: KitchenSortingViewModel

    @Before
    fun setUp() {
        viewModel = KitchenSortingViewModel()
    }

    @Test
    fun initialState_startsAtRound1WithFourItemsAndZeroStars() {
        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(3, state.totalRounds)
        assertEquals(0, state.starsEarned)
        assertEquals(4, state.roundTotalItems)
        assertEquals(4, state.remainingTrayItems.size)
        assertEquals(4, state.trayItems.size)
        assertTrue(state.fridgePlacedItems.isEmpty())
        assertTrue(state.shelfPlacedItems.isEmpty())
        assertTrue(state.fridgeItemsPlaced.isEmpty())
        assertTrue(state.shelfItemsPlaced.isEmpty())
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertEquals(0, viewModel.starsEarned.value)
        assertEquals(1, viewModel.currentRound.value)
        assertFalse(viewModel.isCompleted.value)
        assertFalse(viewModel.celebrationActive.value)

        // Verify balance: 2 fridge, 2 shelf
        val fridgeCount = state.remainingTrayItems.count { it.target == KitchenTarget.FRIDGE }
        val shelfCount = state.remainingTrayItems.count { it.target == KitchenTarget.SHELF }
        assertEquals(2, fridgeCount)
        assertEquals(2, shelfCount)
    }

    @Test
    fun dropItem_correctFridgeDrop_movesItemToFridgeList() {
        val fridgeItem = viewModel.uiState.value.remainingTrayItems.first { it.target == KitchenTarget.FRIDGE }
        val result = viewModel.dropItem(fridgeItem.id, KitchenTarget.FRIDGE)

        assertTrue(result is KitchenDropResult.Correct)
        val state = viewModel.uiState.value
        assertEquals(3, state.remainingTrayItems.size)
        assertEquals(1, state.fridgePlacedItems.size)
        assertEquals(fridgeItem.id, state.fridgePlacedItems.first().id)
        assertFalse(state.isRoundCompleted)
        assertEquals(20, state.score)
    }

    @Test
    fun dropItem_correctShelfDrop_movesItemToShelfList() {
        val shelfItem = viewModel.uiState.value.remainingTrayItems.first { it.target == KitchenTarget.SHELF }
        val result = viewModel.dropItem(shelfItem.id, KitchenTarget.SHELF)

        assertTrue(result is KitchenDropResult.Correct)
        val state = viewModel.uiState.value
        assertEquals(3, state.remainingTrayItems.size)
        assertEquals(1, state.shelfPlacedItems.size)
        assertEquals(shelfItem.id, state.shelfPlacedItems.first().id)
        assertFalse(state.isRoundCompleted)
        assertEquals(20, state.score)
    }

    @Test
    fun onItemDropped_delegatesProperlyToDropItem() {
        val fridgeItem = viewModel.uiState.value.remainingTrayItems.first { it.target == KitchenTarget.FRIDGE }
        val result = viewModel.onItemDropped(fridgeItem.id, KitchenTarget.FRIDGE)

        assertTrue(result is KitchenDropResult.Correct)
        assertEquals(1, viewModel.uiState.value.fridgePlacedItems.size)
    }

    @Test
    fun dropItem_incorrectDrop_returnsIncorrectWithoutRemovingItem() {
        val fridgeItem = viewModel.uiState.value.remainingTrayItems.first { it.target == KitchenTarget.FRIDGE }
        val result = viewModel.dropItem(fridgeItem.id, KitchenTarget.SHELF) // Wrong target: shelf instead of fridge

        assertTrue(result is KitchenDropResult.Incorrect)
        val incorrect = result as KitchenDropResult.Incorrect
        assertEquals(KitchenTarget.SHELF, incorrect.attemptedTarget)
        assertEquals(KitchenTarget.SHELF, incorrect.targetAttempted)
        assertEquals(KitchenTarget.FRIDGE, incorrect.expectedTarget)
        assertTrue(incorrect.hintSpeech.contains("frío") || incorrect.hintSpeech.contains("refrigerador"))

        val state = viewModel.uiState.value
        assertEquals(4, state.remainingTrayItems.size)
        assertTrue(state.fridgePlacedItems.isEmpty())
        assertTrue(state.shelfPlacedItems.isEmpty())
        assertEquals(1, state.attempts)
    }

    @Test
    fun dropItem_unknownIdReturnsIgnored() {
        val result = viewModel.dropItem("non_existent_item", KitchenTarget.FRIDGE)
        assertTrue(result is KitchenDropResult.Ignored)
        assertEquals(0, viewModel.uiState.value.attempts)
    }

    @Test
    fun round1Completion_awardsOneStarAndMarksRoundCompleted() {
        val items = viewModel.uiState.value.remainingTrayItems.toList()
        items.forEach { item ->
            val res = viewModel.dropItem(item.id, item.target)
            assertTrue(res is KitchenDropResult.Correct)
        }

        val state = viewModel.uiState.value
        assertTrue(state.remainingTrayItems.isEmpty())
        assertEquals(4, state.fridgePlacedItems.size + state.shelfPlacedItems.size)
        assertTrue(state.isRoundCompleted)
        assertEquals(1, state.starsEarned)
        assertEquals(1, viewModel.starsEarned.value)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
    }

    @Test
    fun nextRound_advancesToRound2WithSixItems() {
        // Complete Round 1
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        assertTrue(viewModel.uiState.value.isRoundCompleted)

        val advanced = viewModel.nextRound()
        assertTrue(advanced)

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertEquals(2, viewModel.currentRound.value)
        assertEquals(6, state.roundTotalItems)
        assertEquals(6, state.remainingTrayItems.size)
        assertTrue(state.fridgePlacedItems.isEmpty())
        assertTrue(state.shelfPlacedItems.isEmpty())
        assertEquals(1, state.starsEarned) // Preserves 1 star from Round 1
        assertFalse(state.isRoundCompleted)

        // Verify balance: 3 fridge, 3 shelf
        assertEquals(3, state.remainingTrayItems.count { it.target == KitchenTarget.FRIDGE })
        assertEquals(3, state.remainingTrayItems.count { it.target == KitchenTarget.SHELF })
    }

    @Test
    fun round2Completion_awardsTwoStars() {
        // Round 1
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        viewModel.nextRound()

        // Round 2
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }

        val state = viewModel.uiState.value
        assertTrue(state.isRoundCompleted)
        assertEquals(2, state.starsEarned)
        assertEquals(2, viewModel.starsEarned.value)
        assertFalse(state.isCompleted)
    }

    @Test
    fun round3Completion_awardsThreeStarsAndTriggersCelebration() {
        // Round 1
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        viewModel.nextRound()

        // Round 2
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        viewModel.nextRound()

        // Round 3 (8 items: 4 fridge, 4 shelf)
        val r3State = viewModel.uiState.value
        assertEquals(3, r3State.currentRound)
        assertEquals(8, r3State.roundTotalItems)
        assertEquals(8, r3State.remainingTrayItems.size)

        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }

        val finalState = viewModel.uiState.value
        assertTrue(finalState.remainingTrayItems.isEmpty())
        assertTrue(finalState.isRoundCompleted)
        assertTrue(finalState.isCompleted)
        assertTrue(finalState.celebrationActive)
        assertEquals(3, finalState.starsEarned)
        assertTrue(viewModel.isCompleted.value)
        assertTrue(viewModel.celebrationActive.value)
    }

    @Test
    fun adversarial_dropsAfterGameCompletionReturnIgnored() {
        // Complete Round 1, 2, 3
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }

        assertTrue(viewModel.uiState.value.isCompleted)

        // Attempt drop after completion
        val res = viewModel.dropItem("meat", KitchenTarget.FRIDGE)
        assertTrue(res is KitchenDropResult.Ignored)

        val overflow = viewModel.nextRound()
        assertFalse(overflow)
    }

    @Test
    fun dismissCelebration_clearsCelebrationActive() {
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        viewModel.nextRound()
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }

        assertTrue(viewModel.celebrationActive.value)

        viewModel.dismissCelebration()
        assertFalse(viewModel.celebrationActive.value)
        assertFalse(viewModel.uiState.value.celebrationActive)
    }

    @Test
    fun resetGame_restoresToInitialRound1WithCleanLists() {
        // Advance into Round 2
        viewModel.uiState.value.remainingTrayItems.toList().forEach { viewModel.dropItem(it.id, it.target) }
        viewModel.nextRound()
        assertEquals(2, viewModel.uiState.value.currentRound)

        // Reset
        viewModel.resetGame()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(4, state.roundTotalItems)
        assertEquals(4, state.remainingTrayItems.size)
        assertTrue(state.fridgePlacedItems.isEmpty())
        assertTrue(state.shelfPlacedItems.isEmpty())
        assertEquals(0, state.starsEarned)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertEquals(0, state.score)
    }
}
