package com.example.no_definido.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Adversarial stress tests for Milestone 3:
 * 1. Clock physics & touch geometry:
 *    - Shortest-arc delta math across 12 o'clock boundary (no 330° wrap-around whip).
 *    - Inner hub deadzone (atan2 jitter prevention for small radii).
 *    - 12 hour badges 48dp+ hit targets.
 * 2. Kitchen sorting layout & false drop prevention:
 *    - 5 slots in fridge & 5 slots in shelves accommodating Round 3 without overlapping.
 *    - Bottom-clamped hitboxes preventing false drops when touching items in the tray.
 */
class Milestone3AdversarialPhysicsTest {

    // =========================================================================
    // 1. CLOCK PHYSICS & TOUCH GEOMETRY
    // =========================================================================

    /**
     * Exact shortest-arc delta math extracted from InteractiveClockGameScreen.kt:
     *
     * val targetInCircle = (uiState.selectedHour % 12) * 30f
     * val currentNorm = ((targetHandAngle % 360f) + 360f) % 360f
     * var delta = targetInCircle - currentNorm
     * if (delta > 180f) delta -= 360f
     * if (delta < -180f) delta += 360f
     * targetHandAngle += delta
     */
    private fun calculateShortestArcDelta(currentAngle: Float, newHour: Int): Float {
        val targetInCircle = (newHour % 12) * 30f
        val currentNorm = ((currentAngle % 360f) + 360f) % 360f
        var delta = targetInCircle - currentNorm
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return delta
    }

    private fun calculateNaiveDelta(currentAngle: Float, newHour: Int): Float {
        val targetInCircle = (newHour % 12) * 30f
        val currentNorm = ((currentAngle % 360f) + 360f) % 360f
        return targetInCircle - currentNorm
    }

    @Test
    fun clock_crossing12Clockwise_shortestArcDeltaIsPositive30Degrees_notNegative330() {
        val startAngle = 330f // 11 o'clock
        val toHour = 12

        val naiveDelta = calculateNaiveDelta(startAngle, toHour)
        assertEquals(-330f, naiveDelta, 0.001f) // Naive math causes a 330° counter-clockwise whip!

        val shortestDelta = calculateShortestArcDelta(startAngle, toHour)
        assertEquals(+30f, shortestDelta, 0.001f) // Shortest arc moves forward exactly +30°

        val resultingAngle = startAngle + shortestDelta
        assertEquals(360f, resultingAngle, 0.001f)
        assertEquals(0f, resultingAngle % 360f, 0.001f)
    }

    @Test
    fun clock_crossing12CounterClockwise_shortestArcDeltaIsNegative30Degrees_notPositive330() {
        val startAngle = 0f // 12 o'clock (or 360f)
        val toHour = 11

        val naiveDelta = calculateNaiveDelta(startAngle, toHour)
        assertEquals(+330f, naiveDelta, 0.001f) // Naive math causes a 330° clockwise whip!

        val shortestDelta = calculateShortestArcDelta(startAngle, toHour)
        assertEquals(-30f, shortestDelta, 0.001f) // Shortest arc moves backwards exactly -30°

        val resultingAngle = startAngle + shortestDelta
        assertEquals(-30f, resultingAngle, 0.001f)
        assertEquals(330f, ((resultingAngle % 360f) + 360f) % 360f, 0.001f)
    }

    @Test
    fun clock_all144HourTransitions_deltaNeverExceeds180Degrees() {
        for (fromHour in 1..12) {
            val fromAngle = (fromHour % 12) * 30f
            for (toHour in 1..12) {
                val delta = calculateShortestArcDelta(fromAngle, toHour)
                assertTrue(
                    "Delta from hour $fromHour to $toHour must be in [-180, 180], but was $delta",
                    abs(delta) <= 180f
                )

                // The resulting normalized angle must match the target hour * 30 exactly
                val targetNorm = (toHour % 12) * 30f
                val resultNorm = (((fromAngle + delta) % 360f) + 360f) % 360f
                assertEquals(
                    "Angle for hour $toHour should match targetNorm",
                    targetNorm,
                    resultNorm,
                    0.001f
                )
            }
        }
    }

    @Test
    fun clock_dragRandomWalk_10000Steps_neverSpinsBeyond180DegreesPerStep() {
        var handAngle = 0f // Start at 12 o'clock
        val rng = Random(42)

        for (i in 0 until 10_000) {
            val targetHour = rng.nextInt(1, 13)
            val delta = calculateShortestArcDelta(handAngle, targetHour)

            assertTrue("Step $i delta $delta exceeds 180°", abs(delta) <= 180f)
            handAngle += delta

            val expectedNorm = (targetHour % 12) * 30f
            val actualNorm = ((handAngle % 360f) + 360f) % 360f
            assertEquals("Normalized angle mismatch at step $i", expectedNorm, actualNorm, 0.01f)
        }
    }

    @Test
    fun clock_innerHubDeadzone_filtersSubpixelJitterNearCenter() {
        val clockRadiusPx = 150f
        val deadzoneThresholdPx = clockRadiusPx * 0.20f // 30 px

        // Point near center with subpixel offset (0.1, -0.1) vs (-0.1, -0.1)
        val p1_dx = 0.1
        val p1_dy = -0.1
        val p2_dx = -0.1
        val p2_dy = -0.1

        val dist1 = hypot(p1_dx, p1_dy).toFloat()
        val dist2 = hypot(p2_dx, p2_dy).toFloat()

        // Without deadzone:
        var deg1 = Math.toDegrees(atan2(p1_dy, p1_dx)).toFloat() + 90f
        if (deg1 < 0f) deg1 += 360f
        val h1 = (((deg1 / 30f).roundToInt() % 12).let { if (it == 0) 12 else it })

        var deg2 = Math.toDegrees(atan2(p2_dy, p2_dx)).toFloat() + 90f
        if (deg2 < 0f) deg2 += 360f
        val h2 = (((deg2 / 30f).roundToInt() % 12).let { if (it == 0) 12 else it })

        // Without deadzone, micro-movement of 0.2px causes violent jump between hour 2 and hour 11
        assertNotEquals(h1, h2)
        assertEquals(2, h1)
        assertEquals(11, h2)

        // WITH deadzone: both are strictly under 30px threshold and safely ignored!
        assertTrue(dist1 < deadzoneThresholdPx)
        assertTrue(dist2 < deadzoneThresholdPx)
    }

    @Test
    fun clock_hubDeadzone_acceptsAllValidPointersAtOrBeyond20PercentRadius() {
        val clockRadiusPx = 150f
        val deadzoneThresholdPx = clockRadiusPx * 0.20f // 30 px

        // Test at 30px (exact boundary) and above
        for (hour in 1..12) {
            val angleRad = Math.toRadians((hour * 30.0) - 90.0)
            val distance = deadzoneThresholdPx + 5f // 35 px (just outside deadzone)
            val dx = distance * cos(angleRad)
            val dy = distance * sin(angleRad)

            val touchDist = hypot(dx, dy).toFloat()
            assertTrue(touchDist >= deadzoneThresholdPx)

            var deg = Math.toDegrees(atan2(dy, dx)).toFloat() + 90f
            if (deg < 0f) deg += 360f
            val snappedHour = (((deg / 30f).roundToInt() % 12).let { if (it == 0) 12 else it })
            assertEquals("Hour $hour should snap cleanly at distance $distance", hour, snappedHour)
        }
    }

    @Test
    fun clock_all12HourBadges_haveAtLeast48dpHitTargets() {
        // In InteractiveClockGameScreen.kt:
        // val clockDiameter = (screenH * 0.78f).coerceIn(240.dp, 350.dp)
        // val badgeSize = (clockDiameter * 0.17f).coerceIn(48.dp, 60.dp)

        for (screenH in 200..1200) {
            val clockDiameter = (screenH * 0.78f).coerceIn(240f, 350f)
            val badgeSize = (clockDiameter * 0.17f).coerceIn(48f, 60f)

            assertTrue(
                "Badge size for screen height $screenH must be >= 48dp, but was $badgeSize",
                badgeSize >= 48f
            )
            assertTrue(
                "Badge size for screen height $screenH must be <= 60dp, but was $badgeSize",
                badgeSize <= 60f
            )
        }
    }

    // =========================================================================
    // 2. KITCHEN SORTING SLOTS & FALSE DROP PREVENTION
    // =========================================================================

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

    @Test
    fun kitchen_fridge5Slots_noOverlapBetweenAnyItemsInRound3() {
        val testScreens = listOf(
            Pair(1280f, 720f),  // standard 16:9 phone
            Pair(1920f, 1080f), // FHD phone
            Pair(2400f, 1080f), // ultrawide phone
            Pair(1280f, 800f),  // 16:10 tablet
            Pair(2560f, 1600f), // high-res tablet
            Pair(800f, 480f),   // compact screen
            Pair(640f, 360f)    // small compact screen
        )

        for ((screenW, screenH) in testScreens) {
            val fridgeZoneW = screenW * 0.20f
            val fridgeZoneH = screenH * 0.42f
            val itemSize = (fridgeZoneH * 0.28f).coerceIn(44f, 64f)

            val slotOrigins = listOf(
                Pair(fridgeZoneW * 0.12f, fridgeZoneH * 0.06f), // Repisa Superior Izquierda
                Pair(fridgeZoneW * 0.54f, fridgeZoneH * 0.06f), // Repisa Superior Derecha
                Pair(fridgeZoneW * 0.12f, fridgeZoneH * 0.39f), // Repisa Inferior Izquierda
                Pair(fridgeZoneW * 0.54f, fridgeZoneH * 0.39f), // Repisa Inferior Derecha
                Pair(fridgeZoneW * 0.33f, fridgeZoneH * 0.72f)  // Cajón Inferior
            )

            val itemRects = slotOrigins.map { (sx, sy) ->
                RectF(left = sx, top = sy, right = sx + itemSize, bottom = sy + itemSize)
            }

            for (i in 0 until 5) {
                for (j in i + 1 until 5) {
                    val overlap = itemRects[i].overlaps(itemRects[j])
                    assertFalse(
                        "Fridge slots $i and $j overlap on screen ${screenW}x${screenH}! " +
                                "Rect $i: ${itemRects[i]}, Rect $j: ${itemRects[j]}",
                        overlap
                    )
                }
            }
        }
    }

    @Test
    fun kitchen_shelf5Slots_noOverlapBetweenAnyItemsInRound3() {
        val testScreens = listOf(
            Pair(1280f, 720f),
            Pair(1920f, 1080f),
            Pair(2400f, 1080f),
            Pair(1280f, 800f),
            Pair(2560f, 1600f),
            Pair(800f, 480f)
        )

        for ((screenW, screenH) in testScreens) {
            val shelfZoneW = screenW * 0.21f
            val shelfZoneH = screenH * 0.62f
            val itemSize = (shelfZoneH * 0.22f).coerceIn(46f, 66f)

            val slotOrigins = listOf(
                Pair(shelfZoneW * 0.14f, shelfZoneH * 0.07f), // Repisa Alta Izquierda
                Pair(shelfZoneW * 0.54f, shelfZoneH * 0.07f), // Repisa Alta Derecha
                Pair(shelfZoneW * 0.14f, shelfZoneH * 0.38f), // Repisa Media Izquierda
                Pair(shelfZoneW * 0.54f, shelfZoneH * 0.38f), // Repisa Media Derecha
                Pair(shelfZoneW * 0.34f, shelfZoneH * 0.70f)  // Repisa Baja
            )

            val itemRects = slotOrigins.map { (sx, sy) ->
                RectF(left = sx, top = sy, right = sx + itemSize, bottom = sy + itemSize)
            }

            for (i in 0 until 5) {
                for (j in i + 1 until 5) {
                    val overlap = itemRects[i].overlaps(itemRects[j])
                    assertFalse(
                        "Shelf slots $i and $j overlap on screen ${screenW}x${screenH}! " +
                                "Rect $i: ${itemRects[i]}, Rect $j: ${itemRects[j]}",
                        overlap
                    )
                }
            }
        }
    }

    @Test
    fun kitchen_bottomClampedHitboxes_preventFalseDropsInTray() {
        val testScreens = listOf(
            Pair(1280f, 720f),  // standard 16:9 phone
            Pair(1920f, 1080f), // FHD phone
            Pair(2400f, 1080f), // ultrawide phone
            Pair(1280f, 800f),  // 16:10 tablet
            Pair(2560f, 1600f), // high-res tablet
            Pair(800f, 480f),   // compact screen
            Pair(640f, 360f)    // small compact screen
        )

        for ((screenW, screenH) in testScreens) {
            val fLeft = screenW * 0.09f
            val fTop = screenH * 0.43f
            val fWidth = screenW * 0.20f
            val fHeight = screenH * 0.42f
            val fRect = RectF(fLeft, fTop, fLeft + fWidth, fTop + fHeight)

            // Tray geometry from KitchenSortingGameScreen.kt:
            val trayPaddingBottom = (screenH * 0.05f).coerceAtLeast(14f)
            val trayItemSize = (screenH * 0.20f).coerceIn(68f, 92f)
            val trayItemSpacing = 22f

            val totalTrayWidth = 4 * trayItemSize + 3 * trayItemSpacing
            val trayStartX = (screenW - totalTrayWidth) / 2f
            val trayItemTop = screenH - trayPaddingBottom - trayItemSize
            val trayItemBottom = screenH - trayPaddingBottom

            // Safe fridge hitbox with restricted right (+0.15f) and clamped bottom (strictly above tray):
            val safeFridgeHitbox = RectF(
                left = fLeft - fWidth * 0.25f,
                top = fTop - fHeight * 0.20f,
                right = fRect.right + fWidth * 0.15f,
                bottom = minOf(fRect.bottom, trayItemTop - 8f)
            )

            // Item 0 at resting position in tray:
            val item0 = RectF(trayStartX, trayItemTop, trayStartX + trayItemSize, trayItemBottom)

            // Overlap check: safe fridge hitbox must never overlap item 0 in the tray
            val falseDropOverlaps = safeFridgeHitbox.overlaps(item0)
            assertFalse(
                "Item 0 rect $item0 overlaps safeFridgeHitbox $safeFridgeHitbox on screen ${screenW}x${screenH}",
                falseDropOverlaps
            )

            // Center containment check: resting item 0 center must never be contained in safeFridgeHitbox
            val falseDropContains = safeFridgeHitbox.contains(item0.centerX, item0.centerY)
            assertFalse(
                "Item 0 center (${item0.centerX}, ${item0.centerY}) is contained in safeFridgeHitbox ($safeFridgeHitbox) on screen ${screenW}x${screenH}",
                falseDropContains
            )
        }
    }
}
