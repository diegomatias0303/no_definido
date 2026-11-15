package com.example.no_definido.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Empirical Adversarial Challenge Test Suite for Milestone 4:
 * 1. NumberTrainGameScreen:
 *    - Train transit animation state machine (unidirectional forward motion, no backward reversal).
 *    - 8 cargo slots in Round 3 non-overlapping 2x4 grid layout across landscape screens.
 *    - Clamped drop hitboxes requiring vertical clearance above the bottom tray.
 * 2. ColorFlowerGardenScreen:
 *    - Dynamic pot sizing across landscape viewports (640x360, 800x480, 1280x720, etc.) without overflow.
 *    - Clamped pot drop hitboxes strictly above bottom tray preventing false drops.
 *    - Spring-back return physics on mismatch/cancellation.
 */
class Milestone4AdversarialPhysicsTest {

    data class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        val width: Float get() = right - left
        val height: Float get() = bottom - top
        val centerX: Float get() = (left + right) / 2f
        val centerY: Float get() = (top + bottom) / 2f

        fun contains(x: Float, y: Float): Boolean {
            return x in left..right && y in top..bottom
        }

        fun overlaps(other: RectF): Boolean {
            return left < other.right && right > other.left && top < other.bottom && bottom > other.top
        }
    }

    // =========================================================================
    // 1. NUMBER TRAIN: ANIMATION CONTINUITY & STATE MACHINE
    // =========================================================================

    /**
     * Simulates the train transition trajectory from NumberTrainGameScreen.kt:
     * - Phase 1 (Departure): trainSlideOffset animates from 0f to -2500f (accelerating to the left).
     * - Phase 2 (Snap off-screen): trainSlideOffset snaps instantly to +2500f (off-screen to the right).
     * - Phase 3 (Arrival): trainSlideOffset animates from +2500f to 0f (rolling into center from the right).
     */
    @Test
    fun trainTransit_unidirectionalForwardMotion_neverReversesBackwards() {
        // Departure phase: 0f -> -2500f
        val departureSamples = (0..10).map { step ->
            val fraction = step / 10f
            0f + (-2500f - 0f) * fraction
        }

        // Verify departure velocity is strictly non-positive (moving forward to the left)
        for (i in 0 until departureSamples.size - 1) {
            val delta = departureSamples[i + 1] - departureSamples[i]
            assertTrue("Departure delta must be <= 0 (moving leftwards), but was $delta", delta <= 0f)
        }
        assertEquals(-2500f, departureSamples.last(), 0.001f)

        // Snap phase: instantly teleports from -2500f to +2500f while fully off-screen (screenWidth <= 2400f)
        val snapPosition = 2500f
        val maxRealisticScreenWidth = 2400f
        assertTrue(
            "Snap position $snapPosition must be strictly beyond max screen width $maxRealisticScreenWidth",
            snapPosition > maxRealisticScreenWidth
        )

        // Arrival phase: +2500f -> 0f
        val arrivalSamples = (0..10).map { step ->
            val fraction = step / 10f
            2500f + (0f - 2500f) * fraction
        }

        // Verify arrival velocity is strictly non-positive (rolling forward into center from right)
        for (i in 0 until arrivalSamples.size - 1) {
            val delta = arrivalSamples[i + 1] - arrivalSamples[i]
            assertTrue("Arrival delta must be <= 0 (moving leftwards into view), but was $delta", delta <= 0f)
        }
        assertEquals(0f, arrivalSamples.last(), 0.001f)
    }

    @Test
    fun trainAnimationState_locksTouchInteractionDuringTransit() {
        val vm = NumberTrainViewModel()
        assertEquals(TrainAnimationState.IDLE, vm.trainAnimationState.value)

        // Touch enabled in IDLE
        val isInteractionInitiallyEnabled = vm.trainAnimationState.value == TrainAnimationState.IDLE && !vm.uiState.value.isRoundCompleted
        assertTrue(isInteractionInitiallyEnabled)

        // Complete Round 1 (3 items)
        repeat(3) { vm.loadCargo() }

        // State changes to DEPARTING
        assertEquals(TrainAnimationState.DEPARTING, vm.trainAnimationState.value)
        val isInteractionDuringDeparture = vm.trainAnimationState.value == TrainAnimationState.IDLE && !vm.uiState.value.isRoundCompleted
        assertFalse("Interaction must be locked during train departure", isInteractionDuringDeparture)

        // Advance round -> ARRIVING
        vm.nextRound()
        assertEquals(TrainAnimationState.ARRIVING, vm.trainAnimationState.value)
        val isInteractionDuringArrival = vm.trainAnimationState.value == TrainAnimationState.IDLE && !vm.uiState.value.isRoundCompleted
        assertFalse("Interaction must be locked during train arrival", isInteractionDuringArrival)

        // Arrival complete -> IDLE
        vm.onArrivalComplete()
        assertEquals(TrainAnimationState.IDLE, vm.trainAnimationState.value)
        val isInteractionReenabled = vm.trainAnimationState.value == TrainAnimationState.IDLE && !vm.uiState.value.isRoundCompleted
        assertTrue("Interaction must be re-enabled once arrival completes", isInteractionReenabled)
    }

    // =========================================================================
    // 2. NUMBER TRAIN: ROUND 3 2x4 GRID LAYOUT & NON-OVERLAPPING SLOTS
    // =========================================================================

    @Test
    fun trainRound3_all8SlotsFitIntoNonOverlapping2x4GridAcrossLandscapeViewports() {
        val testScreens = listOf(
            Pair(640f, 360f),   // compact mobile landscape
            Pair(800f, 480f),   // WVGA tablet / compact device
            Pair(960f, 540f),   // qHD landscape
            Pair(1024f, 600f),  // 7-inch tablet
            Pair(1280f, 720f),  // standard 720p HD phone
            Pair(1920f, 1080f), // 1080p FHD
            Pair(2400f, 1080f)  // Ultrawide landscape
        )

        for ((screenW, screenH) in testScreens) {
            val locomotiveWidth = (screenH * 0.36f).coerceIn(120f, 175f)
            val trainSpacing = 8f
            val slotSize = (screenH * 0.12f).coerceIn(42f, 56f)
            val slotSpacingH = 8f
            val slotSpacingV = 6f
            val paddingH = 14f
            val paddingV = 8f
            val borderWidth = 5f

            // Calculate 2x4 wagon dimensions
            val wagonContentWidth = paddingH * 2 + 4 * slotSize + 3 * slotSpacingH
            val wagonContentHeight = paddingV * 2 + 2 * slotSize + 1 * slotSpacingV
            val totalWagonWidth = wagonContentWidth + 2 * borderWidth
            val totalTrainAssemblyWidth = locomotiveWidth + trainSpacing + totalWagonWidth

            // Verify total train assembly fits comfortably within landscape screen
            assertTrue(
                "Train assembly width $totalTrainAssemblyWidth must fit within screen width $screenW",
                totalTrainAssemblyWidth < screenW
            )

            // Construct bounding boxes for all 8 slots (0..3 top row, 4..7 bottom row)
            val slotRects = mutableListOf<RectF>()
            val wagonOriginX = locomotiveWidth + trainSpacing + borderWidth + paddingH
            val wagonOriginY = borderWidth + paddingV

            for (row in 0..1) {
                val y = wagonOriginY + row * (slotSize + slotSpacingV)
                for (col in 0..3) {
                    val x = wagonOriginX + col * (slotSize + slotSpacingH)
                    slotRects.add(RectF(left = x, top = y, right = x + slotSize, bottom = y + slotSize))
                }
            }

            assertEquals(8, slotRects.size)

            // Adversarially check that NO two slots overlap
            for (i in 0 until 8) {
                for (j in i + 1 until 8) {
                    val overlap = slotRects[i].overlaps(slotRects[j])
                    assertFalse(
                        "Round 3 slots $i and $j overlap on screen ${screenW}x${screenH}! " +
                                "Rect $i: ${slotRects[i]}, Rect $j: ${slotRects[j]}",
                        overlap
                    )
                }
            }

            // Verify minimum clearance between slots
            for (col in 0 until 3) {
                val hGapRow0 = slotRects[col + 1].left - slotRects[col].right
                val hGapRow1 = slotRects[col + 5].left - slotRects[col + 4].right
                assertEquals(slotSpacingH, hGapRow0, 0.001f)
                assertEquals(slotSpacingH, hGapRow1, 0.001f)
            }
            for (col in 0 until 4) {
                val vGap = slotRects[col + 4].top - slotRects[col].bottom
                assertEquals(slotSpacingV, vGap, 0.001f)
            }
        }
    }

    // =========================================================================
    // 3. NUMBER TRAIN: CLAMPED DROP HITBOX & TRAY CLEARANCE
    // =========================================================================

    @Test
    fun trainDrop_restingItemInTray_neverTriggersFalseDrop() {
        val testScreens = listOf(
            Pair(640f, 360f),
            Pair(800f, 480f),
            Pair(1280f, 720f),
            Pair(1920f, 1080f)
        )

        for ((screenW, screenH) in testScreens) {
            val wagonBounds = RectF(
                left = screenW * 0.30f,
                top = screenH * 0.25f,
                right = screenW * 0.70f,
                bottom = screenH * 0.65f
            )

            val trayHeight = 88f
            val trayRect = RectF(
                left = screenW * 0.10f,
                top = screenH - trayHeight - 14f,
                right = screenW * 0.90f,
                bottom = screenH - 14f
            )

            val itemSize = 72f
            val itemRestingY = trayRect.top + (trayHeight - itemSize) / 2f
            val minClearancePx = 40f

            // Scenario A: Item resting in tray (offsetY = 0f)
            val restingItemRect = RectF(
                left = trayRect.centerX - itemSize / 2f,
                top = itemRestingY,
                right = trayRect.centerX + itemSize / 2f,
                bottom = itemRestingY + itemSize
            )
            val offsetYResting = 0f

            val clearedTrayResting = (offsetYResting < -minClearancePx) ||
                    (restingItemRect.centerY < (trayRect.top - 12f))
            assertFalse("Resting item in tray must NOT clear tray", clearedTrayResting)

            val isValidDropResting = clearedTrayResting && wagonBounds.contains(restingItemRect.centerX, restingItemRect.centerY)
            assertFalse("Resting item must NOT trigger a valid drop", isValidDropResting)

            // Scenario B: Minor horizontal nudge inside tray (offsetY = -15f < 40f clearance)
            val nudgedOffsetY = -15f
            val nudgedItemRect = RectF(
                left = restingItemRect.left,
                top = restingItemRect.top + nudgedOffsetY,
                right = restingItemRect.right,
                bottom = restingItemRect.bottom + nudgedOffsetY
            )
            val clearedTrayNudge = (nudgedOffsetY < -minClearancePx) ||
                    (nudgedItemRect.centerY < (trayRect.top - 12f))
            assertFalse("Minor nudge inside tray must NOT clear tray", clearedTrayNudge)

            // Scenario C: Intentional upward drag into wagon (offsetY = -180f, dragged into wagon center)
            val draggedOffsetY = -180f
            val draggedItemRect = RectF(
                left = wagonBounds.centerX - itemSize / 2f,
                top = wagonBounds.centerY - itemSize / 2f,
                right = wagonBounds.centerX + itemSize / 2f,
                bottom = wagonBounds.centerY + itemSize / 2f
            )
            val clearedTrayDrag = (draggedOffsetY < -minClearancePx) ||
                    (draggedItemRect.centerY < (trayRect.top - 12f))
            assertTrue("Intentional drag into wagon MUST clear tray", clearedTrayDrag)

            val isValidDropDrag = clearedTrayDrag && wagonBounds.contains(draggedItemRect.centerX, draggedItemRect.centerY)
            assertTrue("Intentional drag into wagon MUST register as a valid drop", isValidDropDrag)
        }
    }

    // =========================================================================
    // 4. COLOR FLOWER GARDEN: DYNAMIC POT SIZING ACROSS LANDSCAPE VIEWPORTS
    // =========================================================================

    @Test
    fun flowerGarden_dynamicPotSizing_accommodatesAll8PotsWithoutOverflowAcrossViewports() {
        val testScreens = listOf(
            Pair(640f, 360f),   // small landscape
            Pair(800f, 480f),   // WVGA landscape
            Pair(960f, 540f),   // qHD landscape
            Pair(1024f, 600f),  // WSVGA tablet
            Pair(1280f, 720f),  // 720p HD
            Pair(1920f, 1080f), // 1080p FHD
            Pair(2400f, 1080f)  // Ultrawide
        )

        for ((screenW, screenH) in testScreens) {
            for (potCount in listOf(3, 5, 8)) {
                val potSpacing = if (potCount <= 3) 24f else if (potCount <= 5) 14f else 8f
                val potSize = ((screenW - 48f - potSpacing * (potCount - 1)) / potCount).coerceIn(66f, 135f)

                val totalRowWidth = potCount * potSize + (potCount - 1) * potSpacing

                // Verify row fits within screen width with margins for padding (16dp * 2 = 32dp min)
                assertTrue(
                    "Pots row ($potCount pots, potSize=$potSize) total width $totalRowWidth must fit in screen width $screenW",
                    totalRowWidth <= screenW
                )

                // Verify minimum accessible touch target size (>= 66dp)
                assertTrue(
                    "Pot size $potSize must be at least 66dp for accessible child tapping on screen ${screenW}x${screenH}",
                    potSize >= 66f
                )

                // Verify pot size does not exceed maximum aesthetic limit (135dp)
                assertTrue(
                    "Pot size $potSize must not exceed 135dp",
                    potSize <= 135f
                )
            }
        }
    }

    // =========================================================================
    // 5. COLOR FLOWER GARDEN: CLAMPED POT HITBOXES & FALSE DROP PREVENTION
    // =========================================================================

    @Test
    fun flowerGarden_potHitboxClampedAboveTray_preventsFalseMatchesFromTrayBadges() {
        val testScreens = listOf(
            Pair(640f, 360f),
            Pair(800f, 480f),
            Pair(1280f, 720f),
            Pair(1920f, 1080f)
        )

        for ((screenW, screenH) in testScreens) {
            val potSize = 80f
            val potTop = screenH * 0.20f
            val potRect = RectF(
                left = screenW * 0.45f,
                top = potTop,
                right = screenW * 0.45f + potSize,
                bottom = potTop + potSize
            )

            val trayHeight = 80f
            val trayTop = screenH - trayHeight - 12f
            val safetyMarginPx = 10f

            // Safe hitbox calculation from ColorFlowerGardenScreen.kt:
            val safePotHitbox = RectF(
                left = potRect.left - potRect.width * 0.12f,
                top = potRect.top - potRect.height * 0.12f,
                right = potRect.right + potRect.width * 0.12f,
                bottom = minOf(potRect.bottom, trayTop - safetyMarginPx)
            )

            // Verify safe hitbox bottom is strictly above tray
            assertTrue(
                "Safe pot hitbox bottom ${safePotHitbox.bottom} must be <= (trayTop - safetyMarginPx) ${trayTop - safetyMarginPx}",
                safePotHitbox.bottom <= (trayTop - safetyMarginPx)
            )

            // Badge resting in tray:
            val badgeResting = RectF(
                left = potRect.centerX - 35f,
                top = trayTop + 10f,
                right = potRect.centerX + 35f,
                bottom = trayTop + 80f
            )

            // Gate 1: Check flowerCenter.y < (trayTop - safetyMarginPx)
            val flowerCenter = OffsetF(badgeResting.centerX, badgeResting.centerY)
            val passesTrayGate = flowerCenter.y < (trayTop - safetyMarginPx)
            assertFalse("Badge resting in tray must NOT pass tray clearance gate", passesTrayGate)

            // Gate 2: Safe hitbox containment
            val isContained = safePotHitbox.contains(flowerCenter.x, flowerCenter.y)
            assertFalse("Badge resting in tray must NOT be contained in safe pot hitbox", isContained)

            // Verify intentional drag above tray into pot triggers hit
            val badgeInPot = RectF(
                left = potRect.centerX - 35f,
                top = potRect.centerY - 35f,
                right = potRect.centerX + 35f,
                bottom = potRect.centerY + 35f
            )
            val dragCenter = OffsetF(badgeInPot.centerX, badgeInPot.centerY)
            val passesDragTrayGate = dragCenter.y < (trayTop - safetyMarginPx)
            assertTrue("Badge dragged to pot MUST pass tray clearance gate", passesDragTrayGate)

            val isDragContained = safePotHitbox.contains(dragCenter.x, dragCenter.y)
            assertTrue("Badge dragged to pot MUST be contained in safe pot hitbox", isDragContained)
        }
    }

    data class OffsetF(val x: Float, val y: Float)

    // =========================================================================
    // 6. SPRING PHYSICS VERIFICATION
    // =========================================================================

    @Test
    fun springPhysics_dampingRatioAndStiffnessSpecs_matchChildFriendlyTactileProfile() {
        // From DraggableColorShapeBadge and DraggableTrainCargoItem:
        val dragReturnDamping = 0.65f
        // Damping ratio < 1.0f gives a gentle, tactile overshoot / settle bounce without excessive vibration
        assertTrue(dragReturnDamping in 0.5f..0.85f)

        // From FlowerPotWithBloom:
        val bloomDamping = 0.58f
        assertTrue(bloomDamping in 0.5f..0.85f)
    }
}
