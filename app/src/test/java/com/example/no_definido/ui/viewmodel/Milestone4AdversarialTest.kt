package com.example.no_definido.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Milestone4AdversarialTest {

    private lateinit var trainViewModel: NumberTrainViewModel
    private lateinit var gardenViewModel: ColorFlowerGardenViewModel

    @Before
    fun setUp() {
        trainViewModel = NumberTrainViewModel()
        gardenViewModel = ColorFlowerGardenViewModel()
    }

    // ==========================================
    // NUMBER TRAIN VIEWMODEL ADVERSARIAL TESTS
    // ==========================================

    @Test
    fun train_stressCargoOverflow_rejectionBeyondTarget() {
        // Round 1 target is 3
        assertEquals(3, trainViewModel.uiState.value.targetCount)

        // Load 3 items
        repeat(3) {
            val result = trainViewModel.loadCargo("corn")
            assertTrue(result is TrainDropResult.Loaded)
        }
        assertEquals(3, trainViewModel.uiState.value.loadedCount)
        assertTrue(trainViewModel.uiState.value.isRoundCompleted)

        // Stress: Attempt 50 additional loads
        repeat(50) {
            val overflowResult = trainViewModel.loadCargo("corn")
            assertTrue("Expected Ignored on overflow", overflowResult is TrainDropResult.Ignored)
            assertFalse(trainViewModel.loadCargoItem("corn"))
        }

        // Verify count, round, and stars did not corrupt
        assertEquals(3, trainViewModel.uiState.value.loadedCount)
        assertEquals(1, trainViewModel.uiState.value.starsEarned)
        assertEquals(1, trainViewModel.uiState.value.currentRound)
    }

    @Test
    fun train_invalidItemIds_strictlyRejectedInEachRound() {
        val invalidItems = listOf("cheese", "watermelon", "clay_pot", "jade_stone", "apple", "", "null", "unknown_123")

        // In Round 1, only "corn" (or null) is accepted
        for (item in invalidItems) {
            val res = trainViewModel.loadCargo(item)
            assertTrue("Item $item should be ignored in Round 1", res is TrainDropResult.Ignored)
            assertFalse(trainViewModel.loadCargoItem(item))
        }
        assertEquals(0, trainViewModel.uiState.value.loadedCount)

        // Complete Round 1
        repeat(3) { trainViewModel.loadCargo("corn") }
        trainViewModel.nextRound()

        // In Round 2, only "clay_pot" is accepted
        val invalidRound2 = listOf("corn", "jade_stone", "cheese", "milk", "bread")
        for (item in invalidRound2) {
            val res = trainViewModel.loadCargo(item)
            assertTrue("Item $item should be ignored in Round 2", res is TrainDropResult.Ignored)
        }
        assertEquals(0, trainViewModel.uiState.value.loadedCount)

        // Complete Round 2
        repeat(5) { trainViewModel.loadCargo("clay_pot") }
        trainViewModel.nextRound()

        // In Round 3, only "jade_stone" is accepted
        val invalidRound3 = listOf("corn", "clay_pot", "ruby", "diamond", "gold")
        for (item in invalidRound3) {
            val res = trainViewModel.loadCargo(item)
            assertTrue("Item $item should be ignored in Round 3", res is TrainDropResult.Ignored)
        }
        assertEquals(0, trainViewModel.uiState.value.loadedCount)
    }

    @Test
    fun train_sequentialTransitions_R1_R2_R3_andStarAccumulation() {
        // Round 1: 3 corn -> 1 star
        assertEquals(1, trainViewModel.currentRound.value)
        assertEquals(0, trainViewModel.starsEarned.value)
        assertEquals(TrainAnimationState.IDLE, trainViewModel.trainAnimationState.value)

        repeat(3) { trainViewModel.loadCargo() }
        assertEquals(1, trainViewModel.starsEarned.value)
        assertEquals(TrainAnimationState.DEPARTING, trainViewModel.trainAnimationState.value)

        // Transition to R2
        assertTrue(trainViewModel.nextRound())
        assertEquals(2, trainViewModel.currentRound.value)
        assertEquals(1, trainViewModel.starsEarned.value)
        assertEquals(5, trainViewModel.uiState.value.targetCount)
        assertEquals(TrainAnimationState.ARRIVING, trainViewModel.trainAnimationState.value)
        trainViewModel.onArrivalComplete()
        assertEquals(TrainAnimationState.IDLE, trainViewModel.trainAnimationState.value)

        // Round 2: 5 clay pots -> 2 stars
        repeat(5) { trainViewModel.loadCargo() }
        assertEquals(2, trainViewModel.starsEarned.value)
        assertEquals(TrainAnimationState.DEPARTING, trainViewModel.trainAnimationState.value)

        // Transition to R3
        assertTrue(trainViewModel.nextRound())
        assertEquals(3, trainViewModel.currentRound.value)
        assertEquals(2, trainViewModel.starsEarned.value)
        assertEquals(8, trainViewModel.uiState.value.targetCount)
        assertEquals(TrainAnimationState.ARRIVING, trainViewModel.trainAnimationState.value)
        trainViewModel.onArrivalComplete()
        assertEquals(TrainAnimationState.IDLE, trainViewModel.trainAnimationState.value)

        // Round 3: 8 jade stones -> 3 stars & celebration
        repeat(8) { trainViewModel.loadCargo() }
        assertEquals(3, trainViewModel.starsEarned.value)
        assertTrue(trainViewModel.isCompleted.value)
        assertTrue(trainViewModel.celebrationActive.value)
        assertEquals(TrainAnimationState.COMPLETED, trainViewModel.trainAnimationState.value)

        // Overflow nextRound beyond R3
        assertFalse(trainViewModel.nextRound())
        assertEquals(3, trainViewModel.currentRound.value)

        // Drops after game completion are ignored
        assertTrue(trainViewModel.loadCargo() is TrainDropResult.Ignored)
    }

    @Test
    fun train_resetGame_cleansAllStatesAndRetainsUserId() {
        trainViewModel.setActiveUserId("test_child_123")
        repeat(3) { trainViewModel.loadCargo() }
        trainViewModel.nextRound()
        repeat(5) { trainViewModel.loadCargo() }
        trainViewModel.nextRound()
        repeat(8) { trainViewModel.loadCargo() }

        assertTrue(trainViewModel.isCompleted.value)
        assertEquals(3, trainViewModel.starsEarned.value)

        trainViewModel.resetGame()

        val state = trainViewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(1, trainViewModel.currentRound.value)
        assertEquals(3, state.targetCount)
        assertEquals(0, state.loadedCount)
        assertEquals(0, state.starsEarned)
        assertEquals(0, trainViewModel.starsEarned.value)
        assertEquals("corn", state.currentCargoItem.id)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertFalse(trainViewModel.isCompleted.value)
        assertFalse(trainViewModel.celebrationActive.value)
        assertEquals(TrainAnimationState.IDLE, trainViewModel.trainAnimationState.value)
        assertEquals(0, state.score)
        assertEquals("test_child_123", state.activeUserId)
    }

    @Test
    fun train_hintSpeechAndCountWords_matchExactProgress() {
        val hint0 = trainViewModel.getCurrentHintSpeech()
        assertTrue(hint0.contains("3"))
        assertTrue(hint0.contains("0"))

        val res1 = trainViewModel.loadCargo() as TrainDropResult.Loaded
        assertEquals("¡Uno!", res1.countSpokenWord)

        val res2 = trainViewModel.loadCargo() as TrainDropResult.Loaded
        assertEquals("¡Dos!", res2.countSpokenWord)

        val res3 = trainViewModel.loadCargo() as TrainDropResult.Loaded
        assertEquals("¡Tres!", res3.countSpokenWord)
        assertTrue(res3.isRoundFinished)
    }

    // ===============================================
    // COLOR FLOWER GARDEN VIEWMODEL ADVERSARIAL TESTS
    // ===============================================

    @Test
    fun garden_matchingValidation_and_mismatchRejection() {
        val remaining = gardenViewModel.uiState.value.remainingFlowers
        assertEquals(3, remaining.size)
        val circle = remaining.first { it.id == "red_circle" }
        val square = remaining.first { it.id == "blue_square" }

        // Mismatch: drop red circle onto blue square pot
        val mismatchRes = gardenViewModel.matchFlowerToPot(circle.id, square.id)
        assertTrue("Mismatch must return FlowerMatchResult.Mismatch", mismatchRes is FlowerMatchResult.Mismatch)
        val mismatch = mismatchRes as FlowerMatchResult.Mismatch
        assertEquals(circle.id, mismatch.flower.id)
        assertEquals(square.id, mismatch.targetPotId)
        assertEquals(circle.id, mismatch.expectedPotId)
        assertTrue(mismatch.hintSpeech.isNotEmpty())

        // Ensure lists remain intact after mismatch
        assertEquals(3, gardenViewModel.uiState.value.remainingFlowers.size)
        assertEquals(0, gardenViewModel.uiState.value.matchedFlowers.size)
        assertEquals(1, gardenViewModel.uiState.value.attempts)

        // Valid match: drop red circle onto red circle pot
        val correctRes = gardenViewModel.matchFlowerToPot(circle.id, circle.id)
        assertTrue("Correct match must return FlowerMatchResult.Correct", correctRes is FlowerMatchResult.Correct)
        val correct = correctRes as FlowerMatchResult.Correct
        assertEquals(circle.id, correct.flower.id)
        assertEquals(2, correct.remainingCount)

        // State check
        assertEquals(2, gardenViewModel.uiState.value.remainingFlowers.size)
        assertEquals(1, gardenViewModel.uiState.value.matchedFlowers.size)
        assertEquals("red_circle", gardenViewModel.uiState.value.matchedFlowers.first().id)
        assertEquals("red_circle", gardenViewModel.uiState.value.lastMatchedFlowerId)

        // Duplicate match attempt of already matched flower
        val duplicateRes = gardenViewModel.matchFlowerToPot(circle.id, circle.id)
        assertTrue("Already matched flower must be Ignored", duplicateRes is FlowerMatchResult.Ignored)
        assertFalse(gardenViewModel.isFlowerMatch(circle.id, circle.id))
    }

    @Test
    fun garden_unknownFlowerOrPotIds_safelyIgnoredWithoutCrash() {
        val bogusIds = listOf("unknown_flower", "", "null", "triangle_purple", "12345")

        for (bogus in bogusIds) {
            val res1 = gardenViewModel.matchFlowerToPot(bogus, "red_circle")
            assertTrue("Unknown flower ID must be Ignored", res1 is FlowerMatchResult.Ignored)

            val res2 = gardenViewModel.matchFlowerToPot("red_circle", bogus)
            assertTrue("Unknown pot ID must be Ignored", res2 is FlowerMatchResult.Ignored)

            val res3 = gardenViewModel.matchFlowerToPot(bogus, bogus)
            assertTrue("Both unknown IDs must be Ignored", res3 is FlowerMatchResult.Ignored)
        }

        // Attempts should NOT increase for Ignored items (only for deliberate user mismatches)
        assertEquals(0, gardenViewModel.uiState.value.attempts)
        assertEquals(3, gardenViewModel.uiState.value.remainingFlowers.size)
    }

    @Test
    fun garden_nonSequentialMatching_orderIndependentResolution() {
        // Round 1 items: red_circle, blue_square, yellow_triangle
        // Match in reverse order: yellow_triangle, then blue_square, then red_circle
        val yellowRes = gardenViewModel.matchFlowerToPot("yellow_triangle", "yellow_triangle")
        assertTrue(yellowRes is FlowerMatchResult.Correct)
        assertEquals(2, gardenViewModel.uiState.value.remainingFlowers.size)

        val blueRes = gardenViewModel.matchFlowerToPot("blue_square", "blue_square")
        assertTrue(blueRes is FlowerMatchResult.Correct)
        assertEquals(1, gardenViewModel.uiState.value.remainingFlowers.size)

        val redRes = gardenViewModel.matchFlowerToPot("red_circle", "red_circle")
        assertTrue(redRes is FlowerMatchResult.Correct)
        val finalCorrect = redRes as FlowerMatchResult.Correct
        assertTrue(finalCorrect.isRoundFinished)
        assertFalse(finalCorrect.isGameFinished)

        assertEquals(0, gardenViewModel.uiState.value.remainingFlowers.size)
        assertEquals(3, gardenViewModel.uiState.value.matchedFlowers.size)
        assertTrue(gardenViewModel.uiState.value.isRoundCompleted)
        assertEquals(1, gardenViewModel.starsEarned.value)
    }

    @Test
    fun garden_sequentialTransitions_R1_3Items_R2_5Items_R3_8Items() {
        // R1: 3 items
        assertEquals(1, gardenViewModel.currentRound.value)
        assertEquals(3, gardenViewModel.uiState.value.totalFlowersInRound)
        gardenViewModel.uiState.value.remainingFlowers.toList().forEach {
            gardenViewModel.matchFlowerToPot(it.id, it.id)
        }
        assertEquals(1, gardenViewModel.starsEarned.value)
        assertTrue(gardenViewModel.uiState.value.isRoundCompleted)

        // Advance to R2
        assertTrue(gardenViewModel.nextRound())
        assertEquals(2, gardenViewModel.currentRound.value)
        assertEquals(5, gardenViewModel.uiState.value.totalFlowersInRound)
        assertEquals(5, gardenViewModel.uiState.value.remainingFlowers.size)
        assertEquals(1, gardenViewModel.starsEarned.value)

        // R2 items: circle, square, triangle, diamond, star
        val r2Expected = setOf("red_circle", "blue_square", "yellow_triangle", "green_diamond", "orange_star")
        assertEquals(r2Expected, gardenViewModel.uiState.value.remainingFlowers.map { it.id }.toSet())

        gardenViewModel.uiState.value.remainingFlowers.toList().forEach {
            gardenViewModel.matchFlowerToPot(it.id, it.id)
        }
        assertEquals(2, gardenViewModel.starsEarned.value)
        assertTrue(gardenViewModel.uiState.value.isRoundCompleted)

        // Advance to R3
        assertTrue(gardenViewModel.nextRound())
        assertEquals(3, gardenViewModel.currentRound.value)
        assertEquals(8, gardenViewModel.uiState.value.totalFlowersInRound)
        assertEquals(8, gardenViewModel.uiState.value.remainingFlowers.size)
        assertEquals(2, gardenViewModel.starsEarned.value)

        // R3 items: 8 distinct shapes
        val r3Expected = setOf(
            "red_circle", "blue_square", "yellow_triangle", "green_diamond",
            "orange_star", "purple_oval", "pink_heart", "cyan_pentagon"
        )
        assertEquals(r3Expected, gardenViewModel.uiState.value.remainingFlowers.map { it.id }.toSet())

        gardenViewModel.uiState.value.remainingFlowers.toList().forEach {
            gardenViewModel.matchFlowerToPot(it.id, it.id)
        }
        assertEquals(3, gardenViewModel.starsEarned.value)
        assertTrue(gardenViewModel.isCompleted.value)
        assertTrue(gardenViewModel.celebrationActive.value)
        assertTrue(gardenViewModel.uiState.value.isCompleted)
        assertTrue(gardenViewModel.uiState.value.celebrationActive)

        // Overflow nextRound beyond R3
        assertFalse(gardenViewModel.nextRound())
        assertEquals(3, gardenViewModel.currentRound.value)

        // Matches after completion return Ignored
        assertTrue(gardenViewModel.matchFlowerToPot("red_circle", "red_circle") is FlowerMatchResult.Ignored)
    }

    @Test
    fun garden_resetGame_cleansAllStatesAndPreservesUserId() {
        gardenViewModel.setActiveUserId("player_mexica_42")

        // Progress all the way through R3
        for (r in 1..3) {
            gardenViewModel.uiState.value.remainingFlowers.toList().forEach {
                gardenViewModel.matchFlowerToPot(it.id, it.id)
            }
            if (r < 3) gardenViewModel.nextRound()
        }
        assertTrue(gardenViewModel.isCompleted.value)
        assertEquals(3, gardenViewModel.starsEarned.value)

        gardenViewModel.resetGame()

        val state = gardenViewModel.uiState.value
        assertEquals(1, state.currentRound)
        assertEquals(1, gardenViewModel.currentRound.value)
        assertEquals(3, state.totalFlowersInRound)
        assertEquals(3, state.remainingFlowers.size)
        assertEquals(0, state.matchedFlowers.size)
        assertEquals(0, state.starsEarned)
        assertEquals(0, gardenViewModel.starsEarned.value)
        assertFalse(state.isRoundCompleted)
        assertFalse(state.isCompleted)
        assertFalse(state.celebrationActive)
        assertFalse(gardenViewModel.isCompleted.value)
        assertFalse(gardenViewModel.celebrationActive.value)
        assertEquals(0, state.score)
        assertEquals(0, state.attempts)
        assertEquals("player_mexica_42", state.activeUserId)
    }

    @Test
    fun garden_allShapesAndColorsAreDistinctAndHaveValidProperties() {
        val allItems = ColorFlowerGardenViewModel.ALL_FLOWERS
        assertEquals(8, allItems.size)

        // All 8 shapes must be unique
        val uniqueShapes = allItems.map { it.shape }.toSet()
        assertEquals(8, uniqueShapes.size)

        // All 8 IDs must be unique
        val uniqueIds = allItems.map { it.id }.toSet()
        assertEquals(8, uniqueIds.size)

        // All 8 colors must be non-zero and distinct
        val uniqueColors = allItems.map { it.colorHex }.toSet()
        assertEquals(8, uniqueColors.size)

        // Validate descriptive spoken text
        for (item in allItems) {
            assertTrue(item.spokenName.isNotEmpty())
            assertTrue(item.spokenSuccess.isNotEmpty())
            assertTrue(item.hintSpeech.isNotEmpty())
            assertNotNull(item.color)
            assertNotNull(item.darkBorder)
        }
    }
}
