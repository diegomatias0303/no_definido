package com.example.no_definido

import com.example.no_definido.data.HouseRoomId
import com.example.no_definido.ui.viewmodel.ActivityViewModel
import com.example.no_definido.ui.viewmodel.AppleBasketViewModel
import com.example.no_definido.ui.viewmodel.DropResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdversarialStateTest {

    // --- AppleBasketViewModel Adversarial Tests ---

    @Test
    fun appleBasket_outOfOrderDropsAndInvalidIds() {
        val vm = AppleBasketViewModel()
        assertEquals(1, vm.currentRound.value)
        assertEquals(3, vm.uiState.value.targetCount)

        // Drop invalid IDs
        assertNull(vm.dropApple(-1))
        assertNull(vm.dropApple(100))
        assertEquals(0, vm.uiState.value.currentCount)

        // Drop out of order: slot 2 first, then slot 0
        val count1 = vm.dropApple(2)
        assertEquals(1, count1)
        assertEquals(1, vm.uiState.value.currentCount)

        // Duplicate drop on slot 2
        assertNull(vm.dropApple(2))
        assertEquals(1, vm.uiState.value.currentCount)

        // Drop slot 0
        val count2 = vm.dropApple(0)
        assertEquals(2, count2)

        // Drop slot 1 to complete round 1
        val count3 = vm.dropApple(1)
        assertEquals(3, count3)
        assertTrue(vm.uiState.value.isRoundCompleted)
        assertEquals(1, vm.starsEarned.value)
        assertEquals(1, vm.uiState.value.starsEarned)
        assertFalse(vm.isCompleted.value)

        // Attempting to drop an apple after round completed returns null
        assertNull(vm.dropApple(0))
        assertNull(vm.dropApple(1))
        assertNull(vm.dropApple(2))
    }

    @Test
    fun appleBasket_fullThreeRoundsProgressionAndOverflowDefense() {
        val vm = AppleBasketViewModel()

        // Round 1 (target 3)
        listOf(1, 0, 2).forEach { vm.dropApple(it) }
        assertTrue(vm.uiState.value.isRoundCompleted)
        assertEquals(1, vm.starsEarned.value)

        // Advance to Round 2
        assertTrue(vm.nextRound())
        assertEquals(2, vm.currentRound.value)
        assertEquals(2, vm.uiState.value.currentRound)
        assertEquals(5, vm.uiState.value.targetCount)
        assertEquals(0, vm.uiState.value.currentCount)
        assertEquals(1, vm.starsEarned.value) // Star from round 1 preserved

        // Round 2 out-of-order drops (target 5)
        listOf(4, 2, 0, 3, 1).forEach { id ->
            val res = vm.dropApple(id)
            assertNotNull(res)
        }
        assertTrue(vm.uiState.value.isRoundCompleted)
        assertEquals(2, vm.starsEarned.value)
        assertEquals(2, vm.uiState.value.starsEarned)
        assertFalse(vm.isCompleted.value)

        // Advance to Round 3
        assertTrue(vm.nextRound())
        assertEquals(3, vm.currentRound.value)
        assertEquals(3, vm.uiState.value.currentRound)
        assertEquals(7, vm.uiState.value.targetCount)
        assertEquals(0, vm.uiState.value.currentCount)
        assertEquals(2, vm.starsEarned.value)

        // Round 3 out-of-order drops (target 7)
        listOf(6, 1, 5, 2, 4, 0, 3).forEach { id ->
            val res = vm.dropApple(id)
            assertNotNull(res)
        }
        assertTrue(vm.uiState.value.isRoundCompleted)
        assertTrue(vm.isCompleted.value)
        assertTrue(vm.uiState.value.isCompleted)
        assertTrue(vm.celebrationActive.value)
        assertTrue(vm.uiState.value.celebrationActive)
        assertEquals(3, vm.starsEarned.value)
        assertEquals(3, vm.uiState.value.starsEarned)

        // Adversarial: Attempt nextRound() beyond round 3
        assertFalse(vm.nextRound())
        assertEquals(3, vm.currentRound.value)

        // Adversarial: Attempt drops after game completion
        assertNull(vm.dropApple(0))
        assertNull(vm.dropApple(6))
        assertEquals(7, vm.uiState.value.currentCount)

        // Test Reset restores clean state
        vm.resetGame()
        assertEquals(1, vm.currentRound.value)
        assertEquals(1, vm.uiState.value.currentRound)
        assertEquals(3, vm.uiState.value.targetCount)
        assertEquals(0, vm.uiState.value.currentCount)
        assertEquals(0, vm.starsEarned.value)
        assertEquals(0, vm.uiState.value.starsEarned)
        assertFalse(vm.isCompleted.value)
        assertFalse(vm.celebrationActive.value)
        assertFalse(vm.uiState.value.isRoundCompleted)
        assertFalse(vm.uiState.value.isCompleted)
        assertEquals(3, vm.uiState.value.treeApples.size)
        assertTrue(vm.uiState.value.treeApples.none { it.isCollected })
    }

    @Test
    fun appleBasket_idempotentResets() {
        val vm = AppleBasketViewModel()
        vm.dropApple(0)
        assertEquals(1, vm.uiState.value.currentCount)

        // Reset once
        vm.resetGame()
        assertEquals(0, vm.uiState.value.currentCount)
        assertEquals(1, vm.uiState.value.currentRound)

        // Reset multiple times consecutively
        vm.resetGame()
        vm.resetGame()
        assertEquals(0, vm.uiState.value.currentCount)
        assertEquals(1, vm.uiState.value.currentRound)
        assertEquals(0, vm.starsEarned.value)
    }

    // --- ActivityViewModel Adversarial Tests ---

    @Test
    fun activityViewModel_wrongRoomRejectionAndAttemptsTracking() {
        val vm = ActivityViewModel()
        assertEquals(1, vm.currentRound.value)
        assertEquals(setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM), vm.uiState.value.activeRooms)

        val firstItem = vm.uiState.value.currentItem
        assertNotNull(firstItem)
        assertEquals("toothbrush", firstItem!!.id)
        assertEquals(HouseRoomId.BATHROOM, firstItem.targetRoom)

        // 1. Drop into inactive room: KITCHEN
        val res1 = vm.onItemDroppedInRoom(HouseRoomId.KITCHEN)
        assertTrue(res1 is DropResult.Incorrect)
        assertEquals(0, vm.uiState.value.currentItemIndex)
        assertEquals(1, vm.uiState.value.attempts)
        assertEquals(firstItem, vm.uiState.value.currentItem)

        // 2. Drop into inactive room: LIVING_ROOM
        val res2 = vm.onItemDroppedInRoom(HouseRoomId.LIVING_ROOM)
        assertTrue(res2 is DropResult.Incorrect)
        assertEquals(0, vm.uiState.value.currentItemIndex)
        assertEquals(2, vm.uiState.value.attempts)

        // 3. Drop into active but incorrect room: BEDROOM
        val res3 = vm.onItemDroppedInRoom(HouseRoomId.BEDROOM)
        assertTrue(res3 is DropResult.Incorrect)
        assertEquals(0, vm.uiState.value.currentItemIndex)
        assertEquals(3, vm.uiState.value.attempts)

        // 4. Drop into correct room: BATHROOM
        val res4 = vm.onItemDroppedInRoom(HouseRoomId.BATHROOM)
        assertTrue(res4 is DropResult.Correct)
        assertEquals(1, vm.uiState.value.currentItemIndex)
        assertEquals(listOf("toothbrush"), vm.uiState.value.placedItemsByRoom[HouseRoomId.BATHROOM])
        assertEquals(3, vm.uiState.value.attempts) // Attempts should not increase on correct drop
    }

    @Test
    fun activityViewModel_fullThreeRoundsProgressionAndOverflowDefense() {
        val vm = ActivityViewModel()

        // --- ROUND 1 (4 items: BEDROOM, BATHROOM) ---
        assertEquals(4, vm.uiState.value.totalItemsInRound)
        repeat(4) {
            val item = vm.uiState.value.currentItem
            assertNotNull(item)
            val res = vm.onItemDroppedInRoom(item!!.targetRoom)
            assertTrue(res is DropResult.Correct)
        }
        assertTrue(vm.uiState.value.isRoundCompleted)
        assertFalse(vm.isCompleted.value)
        assertEquals(1, vm.starsEarned.value)
        assertEquals(1, vm.uiState.value.starsEarned)

        // Adversarial: dropped after round completed returns Ignored
        val ignoredDrop = vm.onItemDroppedInRoom(HouseRoomId.BEDROOM)
        assertTrue(ignoredDrop is DropResult.Ignored)

        // --- ADVANCE TO ROUND 2 (6 items: BEDROOM, BATHROOM, KITCHEN) ---
        assertTrue(vm.nextRound())
        assertEquals(2, vm.currentRound.value)
        assertEquals(2, vm.uiState.value.currentRound)
        assertEquals(6, vm.uiState.value.totalItemsInRound)
        assertEquals(0, vm.uiState.value.currentItemIndex)
        assertEquals(1, vm.starsEarned.value) // Star from round 1 retained
        assertEquals(setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM, HouseRoomId.KITCHEN), vm.uiState.value.activeRooms)
        assertTrue(vm.uiState.value.placedItemsByRoom.values.all { it.isEmpty() })

        repeat(6) {
            val item = vm.uiState.value.currentItem
            assertNotNull(item)
            val res = vm.onItemDroppedInRoom(item!!.targetRoom)
            assertTrue(res is DropResult.Correct)
        }
        assertTrue(vm.uiState.value.isRoundCompleted)
        assertFalse(vm.isCompleted.value)
        assertEquals(2, vm.starsEarned.value)
        assertEquals(2, vm.uiState.value.starsEarned)

        // --- ADVANCE TO ROUND 3 (8 items: ALL 4 ROOMS) ---
        assertTrue(vm.nextRound())
        assertEquals(3, vm.currentRound.value)
        assertEquals(3, vm.uiState.value.currentRound)
        assertEquals(8, vm.uiState.value.totalItemsInRound)
        assertEquals(0, vm.uiState.value.currentItemIndex)
        assertEquals(2, vm.starsEarned.value)
        assertEquals(HouseRoomId.entries.toSet(), vm.uiState.value.activeRooms)

        repeat(8) {
            val item = vm.uiState.value.currentItem
            assertNotNull(item)
            val res = vm.onItemDroppedInRoom(item!!.targetRoom)
            assertTrue(res is DropResult.Correct)
        }
        assertTrue(vm.uiState.value.isRoundCompleted)
        assertTrue(vm.isCompleted.value)
        assertTrue(vm.uiState.value.isActivityCompleted)
        assertTrue(vm.celebrationActive.value)
        assertTrue(vm.uiState.value.celebrationActive)
        assertEquals(3, vm.starsEarned.value)
        assertEquals(3, vm.uiState.value.starsEarned)

        // Adversarial: attempt nextRound() beyond round 3
        assertFalse(vm.nextRound())
        assertEquals(3, vm.currentRound.value)

        // Adversarial: attempt drop after game completion
        val ignoredDrop2 = vm.onItemDroppedInRoom(HouseRoomId.BEDROOM)
        assertTrue(ignoredDrop2 is DropResult.Ignored)

        // --- RESET VERIFICATION ---
        vm.resetGame()
        assertEquals(1, vm.currentRound.value)
        assertEquals(1, vm.uiState.value.currentRound)
        assertEquals(0, vm.starsEarned.value)
        assertEquals(0, vm.uiState.value.starsEarned)
        assertEquals(0, vm.uiState.value.currentItemIndex)
        assertEquals(4, vm.uiState.value.totalItemsInRound)
        assertEquals(setOf(HouseRoomId.BEDROOM, HouseRoomId.BATHROOM), vm.uiState.value.activeRooms)
        assertFalse(vm.uiState.value.isRoundCompleted)
        assertFalse(vm.uiState.value.isActivityCompleted)
        assertFalse(vm.isCompleted.value)
        assertFalse(vm.celebrationActive.value)
    }

    @Test
    fun activityViewModel_restartActivityAliasWorksIdenticallyToReset() {
        val vm = ActivityViewModel()
        val item = vm.uiState.value.currentItem
        assertNotNull(item)
        vm.onItemDroppedInRoom(item!!.targetRoom)
        assertEquals(1, vm.uiState.value.currentItemIndex)

        vm.restartActivity()
        assertEquals(1, vm.uiState.value.currentRound)
        assertEquals(0, vm.uiState.value.currentItemIndex)
        assertEquals(0, vm.starsEarned.value)
    }
}
