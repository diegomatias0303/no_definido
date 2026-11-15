package com.example.no_definido

import com.example.no_definido.ui.viewmodel.ActivityViewModel
import com.example.no_definido.ui.viewmodel.AppleBasketViewModel
import com.example.no_definido.ui.viewmodel.BubblePopViewModel
import com.example.no_definido.ui.viewmodel.BubbleTapResult
import com.example.no_definido.ui.viewmodel.ColorFlowerGardenViewModel
import com.example.no_definido.ui.viewmodel.InteractiveClockViewModel
import com.example.no_definido.ui.viewmodel.KitchenSortingViewModel
import com.example.no_definido.ui.viewmodel.MemoryFlipResult
import com.example.no_definido.ui.viewmodel.MemoryMatchViewModel
import com.example.no_definido.ui.viewmodel.NumberTrainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NewGamesAndRandomizationTest {

    @Test
    fun `MemoryMatchViewModel completes all 3 rounds and awards 3 stars`() {
        val vm = MemoryMatchViewModel()
        assertEquals(1, vm.uiState.value.currentRound)
        assertEquals(4, vm.uiState.value.cards.size)

        for (round in 1..3) {
            val cards = vm.uiState.value.cards
            val groupedByItem = cards.groupBy { it.itemId }
            groupedByItem.values.forEach { pairCards ->
                assertEquals(2, pairCards.size)
                val firstRes = vm.flipCard(pairCards[0].cardId)
                assertTrue(firstRes is MemoryFlipResult.FirstCardFlipped)
                val secondRes = vm.flipCard(pairCards[1].cardId)
                assertTrue(secondRes is MemoryFlipResult.PairMatched)
            }
            assertTrue(vm.uiState.value.isRoundCompleted)
            assertEquals(round, vm.starsEarned.value)
            if (round < 3) {
                assertTrue(vm.nextRound())
            }
        }

        assertTrue(vm.isCompleted.value)
        assertTrue(vm.celebrationActive.value)
        assertEquals(3, vm.starsEarned.value)
    }

    @Test
    fun `MemoryMatchViewModel handles mismatch and clearMismatch properly`() {
        val vm = MemoryMatchViewModel()
        val cards = vm.uiState.value.cards
        val firstCard = cards.first()
        val differentCard = cards.first { it.itemId != firstCard.itemId }

        vm.flipCard(firstCard.cardId)
        val res = vm.flipCard(differentCard.cardId)
        assertTrue(res is MemoryFlipResult.PairMismatch)
        assertTrue(vm.uiState.value.hasPendingMismatch)

        vm.clearMismatch()
        assertFalse(vm.uiState.value.hasPendingMismatch)
        assertTrue(vm.uiState.value.cards.none { it.isFaceUp })
    }

    @Test
    fun `BubblePopViewModel completes all 3 rounds and handles distractor bubbles`() {
        val vm = BubblePopViewModel()
        assertEquals(1, vm.uiState.value.currentRound)

        // Probar tocar una burbuja distractora en la ronda 1
        val distractor = vm.uiState.value.bubbles.first { !it.isTarget }
        val wrongRes = vm.tapBubble(distractor.bubbleId)
        assertTrue(wrongRes is BubbleTapResult.WrongBubble)
        assertEquals(0, vm.uiState.value.poppedCount)

        for (round in 1..3) {
            val targetBubbles = vm.uiState.value.bubbles.filter { it.isTarget }
            assertEquals(vm.uiState.value.targetCount, targetBubbles.size)
            targetBubbles.forEach { targetBubble ->
                val popRes = vm.tapBubble(targetBubble.bubbleId)
                assertTrue(popRes is BubbleTapResult.PoppedTarget)
            }
            assertTrue(vm.uiState.value.isRoundCompleted)
            assertEquals(round, vm.starsEarned.value)
            if (round < 3) {
                assertTrue(vm.nextRound())
            }
        }

        assertTrue(vm.isCompleted.value)
        assertTrue(vm.celebrationActive.value)
        assertEquals(3, vm.starsEarned.value)
    }

    @Test
    fun `All ViewModels support randomize mode without breaking round invariants`() {
        val appleVm = AppleBasketViewModel()
        appleVm.resetGame(randomize = true)
        assertTrue(appleVm.uiState.value.targetCount in 2..4)
        assertEquals(appleVm.uiState.value.targetCount, appleVm.uiState.value.treeApples.size)

        val houseVm = ActivityViewModel()
        houseVm.restartActivity(randomize = true)
        assertNotNull(houseVm.uiState.value.currentItem)
        assertEquals(4, houseVm.uiState.value.currentRoundData?.items?.size)

        val clockVm = InteractiveClockViewModel()
        clockVm.resetGame(randomize = true)
        assertTrue(clockVm.uiState.value.targetHour in 1..12)
        assertTrue(clockVm.uiState.value.selectedHour != clockVm.uiState.value.targetHour)

        val kitchenVm = KitchenSortingViewModel()
        kitchenVm.resetGame(randomize = true)
        assertEquals(4, kitchenVm.uiState.value.remainingTrayItems.size)

        val trainVm = NumberTrainViewModel()
        trainVm.resetGame(randomize = true)
        assertTrue(trainVm.uiState.value.targetCount in 2..4)

        val gardenVm = ColorFlowerGardenViewModel()
        gardenVm.resetGame(randomize = true)
        assertEquals(3, gardenVm.uiState.value.activePots.size)
        assertEquals(3, gardenVm.uiState.value.remainingFlowers.size)

        val memoryVm = MemoryMatchViewModel()
        memoryVm.resetGame(randomize = true)
        assertEquals(4, memoryVm.uiState.value.cards.size)

        val bubbleVm = BubblePopViewModel()
        bubbleVm.resetGame(randomize = true)
        assertTrue(bubbleVm.uiState.value.targetCount in 2..3)
    }
}
