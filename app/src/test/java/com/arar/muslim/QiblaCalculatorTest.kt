package com.arar.muslim

import com.arar.muslim.core.sensor.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class QiblaCalculatorTest {

    @Test
    fun testJakartaQiblaBearing() {
        // Jakarta coordinates: -6.2088, 106.8456
        val bearing = QiblaCalculator.calculateQiblaBearing(-6.2088, 106.8456)
        // Standard expected bearing to Kaaba from Jakarta is ~295.2 degrees
        assertTrue("Jakarta Qibla bearing should be around 295°", abs(bearing - 295.2) < 1.0)
    }

    @Test
    fun testLondonQiblaBearing() {
        // London coordinates: 51.5074, -0.1278
        val bearing = QiblaCalculator.calculateQiblaBearing(51.5074, -0.1278)
        // Expected ~118.9 degrees
        assertTrue("London Qibla bearing should be around 119°", abs(bearing - 118.9) < 1.0)
    }

    @Test
    fun testTokyoQiblaBearing() {
        // Tokyo coordinates: 35.6762, 139.6503
        val bearing = QiblaCalculator.calculateQiblaBearing(35.6762, 139.6503)
        // Expected ~293.0 degrees
        assertTrue("Tokyo Qibla bearing should be around 293°", abs(bearing - 293.0) < 1.0)
    }

    @Test
    fun testNeedleRotationWrapping() {
        // Target: 295, Current Heading: 295 -> Diff should be 0
        assertEquals(0f, QiblaCalculator.calculateNeedleRotation(295.0, 295f), 0.001f)

        // Target: 10, Current Heading: 350 -> Diff should be +20 (clockwise)
        assertEquals(20f, QiblaCalculator.calculateNeedleRotation(10.0, 350f), 0.001f)

        // Target: 350, Current Heading: 10 -> Diff should be -20 (counter-clockwise)
        assertEquals(-20f, QiblaCalculator.calculateNeedleRotation(350.0, 10f), 0.001f)
    }

    @Test
    fun testDistanceToKaaba() {
        // Distance Jakarta to Mecca is around ~7900 km
        val distance = QiblaCalculator.calculateDistanceToKaaba(-6.2088, 106.8456)
        assertTrue("Distance should be approx 7900 km", distance in 7500.0..8200.0)
    }
}
